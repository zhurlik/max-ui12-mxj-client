package com.zhurlik.max8.ui12.client;

import com.cycling74.max.Atom;
import com.zhurlik.max8.ui12.component.MessageHandler;
import com.zhurlik.max8.ui12.component.Status;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.zhurlik.max8.ui12.client.NetworkScanner.FIVE;

/**
 * A simple wrapper of WebSocket client for communicating with UI12 device.
 *
 * @author zhurlik@gmail.com
 */
public final class Ui12WebSocket extends WebSocketClient {
    private static final Logger LOG = LoggerFactory.getLogger("Ui12Proxy");
    private Instant startSession = Instant.now();
    private final MessageHandler handler;
    private final NetworkScanner network;

    /**
     * Constructor by URI.
     *
     * @param serverUri WebSocket server
     * @param handler for handling the incoming messages
     */
    public Ui12WebSocket(final URI serverUri, final MessageHandler handler) {
        super(serverUri);
        this.handler = (handler == null) ? new MessageHandler((strings -> {

        })) : handler;
        network = new NetworkScanner(new InetSocketAddress(serverUri.getHost(), serverUri.getPort()),
                this.handler.getOutlet());
    }

    /**
     * Just to print info about connection and to start the session time.
     *
     * @param handshakedata incoming data from the server
     */
    @Override
    public void onOpen(final ServerHandshake handshakedata) {
        LOG.info(">> WebSocket connection has been opened.");
        startSession = Instant.now();
        if (LOG.isDebugEnabled()) {
            final List<String> headers = new ArrayList<>();
            handshakedata.iterateHttpFields().forEachRemaining(name -> headers.add(
                    String.format("%s = %s", name, handshakedata.getFieldValue(name))));
            final String content = handshakedata.getContent() != null ? new String(handshakedata.getContent()) : "";
            LOG.debug(">> Server handshake: status = {}, status message = {}, content = {},\nheaders = {}",
                    handshakedata.getHttpStatus(),
                    handshakedata.getHttpStatusMessage(),
                    content,
                    headers.toString()
            );
        }

        // connected
        handler.getOutlet()
                .accept(Status.CONNECTED.convert());
    }

    @Override
    public void onMessage(final String message) {
        LOG.debug(">> Incoming message: {}", message);
        handler.accept(message);
    }

    /**
     * @param code   an error code, see {@link org.java_websocket.framing.CloseFrame}
     * @param reason some additional details
     * @param remote Returns whether or not the closing of the connection was initiated by the remote host.
     */
    @Override
    public void onClose(final int code, final String reason, final boolean remote) {
        LOG.warn(">> WebSocket connection has been closed.");
        LOG.debug(">> Server response: code = {}, reason = {}, remote = {}", code, reason, remote);
        LOG.debug(">> Session time: {}", Duration.between(startSession, Instant.now()).toMillis());

        // disconnected
        handler.getOutlet()
                .accept(Status.CLOSED.convert());

        if (network != null) {
            network.stopPing();
        }
    }

    /**
     * Just to print the exception details.
     *
     * @param ex any exception
     */
    @Override
    public void onError(final Exception ex) {
        LOG.error(">> WebSocket error:", ex);
        // disconnected
        handler.getOutlet()
                .accept(Status.CLOSED.convert());

    }

    /**
     * Makes the connection with Ui12 via WebSocket and binds the handler for reading incoming messages.
     */
    public void up() {
        if (!network.isHostAvailable()) {
            return;
        }

        try {
            connectBlocking();
            network.ping();
        } catch (Exception e) {
            LOG.error(">> Error:", e);
            handler.getOutlet().accept(Status.NOT_CONNECTED_YET.convert());
        }
    }

    /**
     * Closes the connection with Ui12 device.
     */
    public void down() {
        if (!network.isHostAvailable()) {
            return;
        }

        try {
            close();
            TimeUnit.SECONDS.sleep(FIVE);
            network.stopPing();
        } catch (InterruptedException e) {
            LOG.error(">> Error:", e);
        }
        handler.getOutlet().accept(Status.CLOSED.convert());
    }


    /**
     *
     * @param args
     */
    public void toUi12Device(final Atom[] args) {
        if (!network.isHostAvailable()) {
            return;
        }

        if (args != null && args.length == 1) {
            final String message = args[0].getString();
            LOG.debug(">> Sending: {}", message);
            send(message);
        }
    }
}
