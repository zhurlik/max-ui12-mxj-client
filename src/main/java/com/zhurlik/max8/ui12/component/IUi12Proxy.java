package com.zhurlik.max8.ui12.component;

import com.cycling74.max.Atom;
import com.zhurlik.max8.ui12.client.Ui12WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static com.zhurlik.max8.ui12.component.Scanner.FIVE;

/**
 * An interface to extract the common constants and default methods.
 * The interface contains a lot of the default methods, because there is no way to create MaxObject during testing.
 *
 * @author zhurlik
 */
public interface IUi12Proxy {
    /**
     * Logger.
     */
    Logger LOG = LoggerFactory.getLogger(IUi12Proxy.class);

    /**
     * Inlet label.
     */
    String[] INLET_ASSIST = new String[]{"Input"};

    /**
     * Outlet label.
     */
    String[] OUTLET_ASSIST = new String[]{"Output"};

    /**
     * Sends to the outlet the message about WebSocket/network connections status.
     *
     * @param status one of the {@link Status}
     */
    default void sendStatus(final Status status) {
        // do nothing because the outlet is not available here. see the real implementation.
    }


    /**
     * Getter to return the current WebSocket client.
     *
     * @return the instance of the {@link Ui12WebSocket}.
     */
    Ui12WebSocket getUi12WebSocket();

    /**
     * Getter to return the {@link Scanner} that has reference to the url.
     *
     * @return an instance of UrlExtractor
     */
    Scanner getServerScanner();

    /**
     * Executes one of the following actions: START or STOP.
     *
     * @param value expected signal either 0 or 1
     */
    default void action(final int value) {
        switch (Command.findBy(value)) {
            case START:
                start();
                break;
            case STOP:
                stop();
                break;
            default:
        }
    }

    /**
     * Executes one of the following actions: START or STOP.
     *
     * @param message either url or msg
     * @param args
     */
    default void action(final String message, final Atom[] args) {
        switch (Command.findBy(message)) {
            case SET_URL:
                defineUrl(args);
                break;
            case SEND_MESSAGE:
                toUi12Device(args);
                break;
            default:
        }

    }

    /**
     * Checks the current connection, makes reconnection when it's needed and sends a message to Ui12 via WebSocket.
     *
     * @param args incoming from Max8
     */
    default void toUi12Device(final Atom[] args) {
        if (getUi12WebSocket() == null) {
            sendStatus(Status.NOT_CONNECTED_YET);
            return;
        }

        // to check network
        if (!getServerScanner().isHostAvailable(getUi12WebSocket())) {
            return;
        }

        if (args != null && args.length == 1) {
            final String message = args[0].getString();

            // try make reconnection when it's possible
            if (getUi12WebSocket().isClosed() || getUi12WebSocket().isClosing()) {
                LOG.warn(">> Reconnecting...");
                try {
                    getUi12WebSocket().reconnectBlocking();
                    sendStatus(Status.RECONNECTED);
                } catch (InterruptedException e) {
                    LOG.error(">> Error:", e);
                    sendStatus(Status.CLOSED);
                    // something is a wrong
                    setUi12WebSocket(null);
                    return;
                }
            }
            LOG.debug(">> Sending: {}", message);
            getUi12WebSocket().send(message);
        }
    }

    /**
     * Gets a single string and sets it into the url field.
     *
     * @param args incoming from Max8
     */
    default void defineUrl(final Atom[] args) {
        final Scanner scanner = new Scanner(args, this::sendStatus);
        if (scanner.isValidUrl()) {
            LOG.info(">> Url: {}", scanner);
            // starting a ping process in the background
            getServerScanner().ping(this::getUi12WebSocket);
        }
    }

    /**
     * Makes a connection with Ui12 via WebSocket and binds the handler for reading incoming messages.
     */
    default void start() {
        if (getServerScanner() != null && !getServerScanner().isHostAvailable(getUi12WebSocket())) {
            return;
        }

        if (getUi12WebSocket() != null && getUi12WebSocket().isOpen()) {
            LOG.warn(">> The connection has been already opened");
            try {
                getUi12WebSocket().closeBlocking();
                sendStatus(Status.CLOSED);
            } catch (InterruptedException e) {
                LOG.error(">> Error:", e);
                sendStatus(Status.CLOSED);
                // something is a wrong
                setUi12WebSocket(null);
            }
        }

        try {
            setUi12WebSocket(buildWebSocketClient());
            getUi12WebSocket().connectBlocking();
            sendStatus(Status.CONNECTED);
        } catch (Exception e) {
            LOG.error(">> Error:", e);
            // something is a wrong
            setUi12WebSocket(null);
            sendStatus(Status.NOT_CONNECTED_YET);
        }
    }

    /**
     * Makes and returns the WebSocket client for working with Ui12 device.
     *
     * @return a real WebSocket client
     * @throws Exception any error
     */
    Ui12WebSocket buildWebSocketClient() throws Exception;

    /**
     * Closes the current WebSocket connection.
     */
    default void stop() {
        if (getServerScanner() != null && !getServerScanner().isHostAvailable(getUi12WebSocket())) {
            // something is a wrong
            setUi12WebSocket(null);
        }

        if (getUi12WebSocket() != null && !(getUi12WebSocket().isClosed() || getUi12WebSocket().isClosing())) {
            try {
                getUi12WebSocket().close();
                TimeUnit.SECONDS.sleep(FIVE);
            } catch (InterruptedException e) {
                LOG.error(">> Error:", e);
            }
            sendStatus(Status.CLOSED);
            // something is a wrong
            setUi12WebSocket(null);
        }

        sendStatus(Status.NOT_CONNECTED_YET);
    }

    /**
     * Setter for the WebSocket client.
     *
     * @param ui12WebSocket an instace of the {@link Ui12WebSocket}
     */
    void setUi12WebSocket(Ui12WebSocket ui12WebSocket);
}
