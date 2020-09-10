package com.zhurlik.max8.ui12.component;

import com.cycling74.max.Atom;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

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
class Ui12WebSocket extends WebSocketClient {
    private static final int CONNECT_TIMEOUT = 20000;
    private Instant startSession = Instant.now();
    private final MessageHandler handler;
    private final Outlets outlets;

    /**
     * Constructor by URI.
     *
     * @param serverUri WebSocket server
     * @param handler for handling the incoming messages
     */
    Ui12WebSocket(final URI serverUri, final MessageHandler handler) {
        super(serverUri,  new Draft_6455(), null, CONNECT_TIMEOUT);
        this.handler = handler;
        this.outlets = handler.getOutlets();
    }

    /**
     * Just to print info about connection and to start the session time.
     *
     * @param handshakedata incoming data from the server
     */
    @Override
    public void onOpen(final ServerHandshake handshakedata) {
        outlets.info(">> WebSocket connection has been opened.");
        startSession = Instant.now();
        final List<String> headers = new ArrayList<>();
        handshakedata.iterateHttpFields().forEachRemaining(name -> headers.add(
                String.format("%s = %s", name, handshakedata.getFieldValue(name))));
        final String content = handshakedata.getContent() != null ? new String(handshakedata.getContent()) : "";
        outlets.debug(">> Server handshake: status = {}, status message = {}, content = {},\nheaders = {}",
                handshakedata.getHttpStatus(),
                handshakedata.getHttpStatusMessage(),
                content,
                headers.toString()
        );

        // connected
        handler.getOutlets().toNetworkOutlet(new String[]{"connect 1"});
    }

    @Override
    public void onMessage(final String message) {
        //outlets.debug(">> Incoming message: {}", message);
        handler.accept(message);
    }

    /**
     * @param code   an error code, see {@link org.java_websocket.framing.CloseFrame}
     * @param reason some additional details
     * @param remote Returns whether or not the closing of the connection was initiated by the remote host.
     */
    @Override
    public void onClose(final int code, final String reason, final boolean remote) {
        outlets.debug(">> WebSocket connection has been closed.");
        outlets.debug(">> Server response: code = {}, reason = {}, remote = {}", code, reason, remote);
        outlets.debug(">> Session time: {}", Duration.between(startSession, Instant.now()).toMillis());

        // disconnected
        handler.getOutlets().toNetworkOutlet(new String[]{"connect 0"});
    }

    /**
     * Just to print the exception details.
     *
     * @param ex any exception
     */
    @Override
    public void onError(final Exception ex) {
        outlets.error(ex);
        // disconnected
        handler.getOutlets().toNetworkOutlet(Status.CLOSED.convert());
    }

    /**
     * Send the Max8 message to Ui12 device.
     *
     * @param args see {@link com.cycling74.max.MaxObject#anything(String, Atom[])}
     */
    void toUi12Device(final Atom[] args) {
        if (args != null && args.length == 1) {
            final String message = args[0].getString();
            outlets.debug(">> Sending: {}", message);
            send(message);
        }
    }
}
