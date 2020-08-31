package com.zhurlik.max8.ui12.component;

import com.cycling74.max.Atom;
import com.zhurlik.max8.ui12.client.Ui12WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * @author zhurlik@gmail.com
 */
public final class CommandHandler {
    private static final Logger LOG = LoggerFactory.getLogger("Ui12Proxy");

    /**
     * For forwarding to the outlet.
     * See {@link com.cycling74.max.MaxObject#outlet(int, String[])}
     */
    private final Consumer<String[]> toOutlet;
    private final UrlHandler urlHandler;

    private Ui12WebSocket ui12WebSocket;

    CommandHandler(final Consumer<String[]> toOutlet, final UrlHandler urlHandler) {
        this.toOutlet = toOutlet;
        this.urlHandler = urlHandler;
    }

    /**
     * Executes one of the following actions: START or STOP.
     *
     * @param value expected signal either 0 or 1
     */
    void action(final int value) {
        final Command command = Command.findBy(value);
        LOG.debug(">> Command:{}", command.name());
        switch (command) {
            case START:
                startJob();
                break;
            case STOP:
                stopJob();
                break;
            default:
        }
    }

    /**
     * Opens the WebSocket connection.
     */
    private void startJob() {
        LOG.debug(">> Starting Job...");

        if (!urlHandler.isValidUrl()) {
            LOG.warn(">> Please enter valid url of the Ui12 Device: <server:port>");
            toOutlet.accept(Status.NOT_CONNECTED_YET.convert());
            return;
        }
        try {
            ui12WebSocket = new Ui12WebSocket(urlHandler.getURI(), new MessageHandler(toOutlet));
            ui12WebSocket.up();
        } catch (Exception e) {
            LOG.debug("ERROR:", e);
        }
    }

    /**
     * Closes the current WebSocket connection.
     */
    private void stopJob() {
        LOG.debug(">> Stopping Job...");
        if (ui12WebSocket == null) {
            toOutlet.accept(Status.NOT_CONNECTED_YET.convert());
            return;
        }

        if (!(ui12WebSocket.isClosed() || ui12WebSocket.isClosing())) {
            ui12WebSocket.down();
            // something is a wrong
            ui12WebSocket = null;
        }
    }

    /**
     * Executes one of the following actions: START or STOP.
     *
     * @param message either url or msg
     * @param args
     */
    void action(final String message, final Atom[] args) {
        final Command command = Command.findBy(message);
        LOG.debug(">> Command:{}", command.name());
        switch (command) {
            case SET_URL:
                urlHandler.parse(args);
                break;
            case SEND_MESSAGE:
                send(args);
                break;
            default:
        }

    }

    /**
     * Checks the current connection, makes reconnection when it's needed and sends a message to Ui12 via WebSocket.
     *
     * @param args incoming from Max8
     */
    private void send(final Atom[] args) {
        if (ui12WebSocket == null) {
            toOutlet.accept(Status.NOT_CONNECTED_YET.convert());
            return;
        }

        // try make reconnection when it's possible
        try {
            if (ui12WebSocket.isClosed() || ui12WebSocket.isClosing()) {
                LOG.warn(">> Reconnecting...");

                ui12WebSocket.reconnectBlocking();
                toOutlet.accept(Status.RECONNECTED.convert());
            }
            ui12WebSocket.toUi12Device(args);
        } catch (WebsocketNotConnectedException e) {
            LOG.error(">> Error:", e);
            toOutlet.accept(Status.NOT_CONNECTED_YET.convert());
        } catch (InterruptedException e) {
            LOG.error(">> Error:", e);
            toOutlet.accept(Status.CLOSED.convert());
        }
    }
}
