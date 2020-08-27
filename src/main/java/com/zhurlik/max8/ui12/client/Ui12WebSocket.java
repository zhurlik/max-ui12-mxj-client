package com.zhurlik.max8.ui12.client;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple wrapper of WebSocket client for communicating with UI12 device.
 *
 * @author zhurlik@gmail.com
 */
public abstract class Ui12WebSocket extends WebSocketClient {
    private static final Logger LOG = LoggerFactory.getLogger(Ui12WebSocket.class);
    private Instant startSession = Instant.now();

    /**
     * Constructor by URI.
     *
     * @param serverUri WebSocket server
     */
    public Ui12WebSocket(final URI serverUri) {
        super(serverUri);
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
    }

    @Override
    public void onMessage(final String message) {
        LOG.debug(">> Incoming message: {}", message);
        handle(message);
    }

    /**
     * To be able to pass incomeing message to Max8 component.
     *
     * @param message a websocket message
     */
    protected abstract void handle(String message);

    /**
     *
     * @param code an error code, see {@link org.java_websocket.framing.CloseFrame}
     * @param reason some additional details
     * @param remote Returns whether or not the closing of the connection was initiated by the remote host.
     */
    @Override
    public void onClose(final int code, final String reason, final boolean remote) {
        LOG.warn(">> WebSocket connection has been closed.");
        LOG.debug(">> Server response: code = {}, reason = {}, remote = {}", code, reason, remote);
        LOG.debug(">> Session time: {}", Duration.between(startSession, Instant.now()).toMillis());
    }

    /**
     * Just to print the exception details.
     *
     * @param ex any exception
     */
    @Override
    public void onError(final Exception ex) {
        LOG.error(">> WebSocket error:", ex);
    }
}
