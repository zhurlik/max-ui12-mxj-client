package com.zhurlik.max8.ui12.component;

import com.cycling74.max.Atom;

/**
 * This class implements main behavior: start/stop connection with Ui12, sending the messages and scanning the network.
 *
 * @author zhurlik@gmail.com
 */
class CommandHandler {
    /**
     * 0 - for messages, 1 - for network status, 2 - debug console.
     */
    private final Outlets outlets;
    private final UrlHandler urlHandler;

    /**
     * Pings that the UI12 device is available in the network.
     */
    private NetworkScanner networkScanner;

    /**
     * The real connection between Ui12 device.
     */
    private Ui12WebSocket ui12WebSocket;

    CommandHandler(final Outlets outlets, final UrlHandler urlHandler) {
        this.outlets = outlets;
        this.urlHandler = urlHandler;
    }

    /**
     * Opens the WebSocket connection.
     *
     * @param args incoming from the inlet
     */
    private void startJob(final Atom[] args) {
        outlets.debug(">> Starting Job...");
        urlHandler.parse(args);

        if (!urlHandler.isValidUrl()) {
            outlets.warn(">> Please enter valid url of the Ui12 Device: <server:port>");
            sendStatus(Status.NOT_CONNECTED_YET);
            return;
        }

        try {
            // when the connection was created before
            if (ui12WebSocket != null) {
                ui12WebSocket.closeBlocking();
                networkScanner.stopPing();
            }

            // we have to recreate the connection and restart network scanning
            networkScanner = new NetworkScanner(urlHandler.getInetSocketAddress(), outlets);
            ui12WebSocket = new Ui12WebSocket(urlHandler.getURI(), new MessageHandler(outlets));
            ui12WebSocket.connectBlocking();
            networkScanner.ping(ui12WebSocket);
        } catch (Exception e) {
            outlets.error(e);
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

        try {
            ui12WebSocket.closeBlocking();
            ui12WebSocket = null;
        } catch (Exception e) {
            outlets.error(e);
        }
    }

    /**
     * Based on the command invokes the corresponded methods.
     *
     * @param message either url or msg
     * @param args see {@link com.cycling74.max.MaxObject#anything(String, Atom[])}
     */
    void action(final String message, final Atom[] args) {
        final Command command = Command.findBy(message);
        outlets.debug(">> Command:{}", command.name());
        switch (command) {
            case SET_URL:
                if (args.length == 0) { // no url
                    stopJob();
                } else { // url is defined
                    startJob(args);
                }
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
            sendStatus(Status.NOT_CONNECTED_YET);
            return;
        }

        try {
            // try make reconnection when it's possible
            if (ui12WebSocket.isClosed()) {
                ui12WebSocket.closeBlocking();
                ui12WebSocket = new Ui12WebSocket(urlHandler.getURI(), new MessageHandler(outlets));
                ui12WebSocket.connectBlocking();
            }
            ui12WebSocket.toUi12Device(args);
        } catch (Exception e) {
            outlets.error(e);
            sendStatus(Status.CLOSED);
        }
    }

    private void sendStatus(final Status status) {
        outlets.toNetworkOutlet(status.convert());
    }
}
