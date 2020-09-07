package com.zhurlik.max8.ui12.component;

import com.cycling74.max.Atom;

import java.util.concurrent.TimeUnit;

import static com.zhurlik.max8.ui12.component.NetworkScanner.FIVE;

/**
 * @author zhurlik@gmail.com
 */
class CommandHandler {
    /**
     * For forwarding to the outlets.
     * Note: 0 - for messages, 1 - for network status
     * See {@link com.cycling74.max.MaxObject#outlet(int, String[])}
     */
    private final Outlets outlets;
    private final UrlHandler urlHandler;
    private NetworkScanner networkScanner;
    private Ui12WebSocket ui12WebSocket;

    CommandHandler(final Outlets outlets, final UrlHandler urlHandler) {
        this.outlets = outlets;
        this.urlHandler = urlHandler;
    }

    /**
     * Executes one of the following actions: START or STOP.
     * See {@link com.cycling74.max.MaxObject#inlet(int)}
     *
     * @param value expected signal either 0 or 1
     */
    void action(final int value) {
        final Command command = Command.findBy(value);
        outlets.debug(">> Command:{}", command.name());
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
        outlets.debug(">> Starting Job...");

        if (!urlHandler.isValidUrl()) {
            outlets.warn(">> Please enter valid url of the Ui12 Device: <server:port>");
            sendStatus(Status.NOT_CONNECTED_YET);
            return;
        }
        try {
            if (!networkScanner.isHostAvailable()) {
                return;
            }
            ui12WebSocket = new Ui12WebSocket(urlHandler.getURI(), new MessageHandler(outlets));
            ui12WebSocket.connectBlocking();
        } catch (Exception e) {
            outlets.error("ERROR:", e);
            sendStatus(Status.NOT_CONNECTED_YET);
        }
    }

    /**
     * Closes the current WebSocket connection.
     */
    private void stopJob() {
        outlets.debug(">> Stopping Job...");
        if (ui12WebSocket == null) {
            sendStatus(Status.NOT_CONNECTED_YET);
            return;
        }

        if (!(ui12WebSocket.isClosed() || ui12WebSocket.isClosing())) {
            ui12WebSocket.close();
            sleep();
            sendStatus(Status.CLOSED);

            // something is a wrong
            ui12WebSocket = null;
        }
    }

    private void sleep() {
        try {
            TimeUnit.SECONDS.sleep(FIVE);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Executes one of the following actions: START or STOP.
     *
     * @param message either url or msg
     * @param args see {@link com.cycling74.max.MaxObject#anything(String, Atom[])}
     */
    void action(final String message, final Atom[] args) {
        final Command command = Command.findBy(message);
        outlets.debug(">> Command:{}", command.name());
        switch (command) {
            case SET_URL:
                startNetworkScan(args);
                break;
            case SEND_MESSAGE:
                send(args);
                break;
            default:
        }
    }

    private void startNetworkScan(final Atom[] args) {
        try {
            urlHandler.parse(args);
            networkScanner = new NetworkScanner(urlHandler.getInetSocketAddress(), outlets);
            networkScanner.ping();
        } catch (Exception e) {
            outlets.error(">> Error:", e);
        }
    }

    /**
     * Checks the current connection, makes reconnection when it's needed and sends a message to Ui12 via WebSocket.
     *
     * @param args incoming from Max8
     */
    private void send(final Atom[] args) {
        if (ui12WebSocket == null) {
            sendStatus(Status.NOT_CONNECTED_YET);
            return;
        }

        // try make reconnection when it's possible
        try {
            ui12WebSocket.toUi12Device(args);
        } catch (Exception e) {
            outlets.error(">> Error:", e);
            sendStatus(Status.CLOSED);
        }
    }

    private void sendStatus(final Status status) {
        outlets.toNetworkOutlet(status.convert());
    }
}
