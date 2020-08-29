package com.zhurlik.max8.ui12.component;

import com.cycling74.max.Atom;
import com.zhurlik.max8.ui12.client.Ui12WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
     * A single thread to check network status.
     */
    Executor EXECUTOR = Executors.newSingleThreadExecutor();

    // Timeouts
    /**
     * Waiting time to ping a host in the network.
     */
    int PING_TIMEOUT = 3000;
    /**
     * Waiting time to next ping when the network is down.
     */
    int FIVE = 5;
    /**
     * Waiting time to next ping when the network is up.
     */
    int TEN = 10;

    /**
     * Sends to the outlet the message about WebSocket/network connections status.
     *
     * @param status one of the {@link Status}
     */
    default void sendStatus(final Status status) {
        // do nothing because the outlet is not available here. see the real implementation.
    }

    /**
     * Checks if the host is available in the network.
     *
     * @return true when Ui12 Device is available in the network
     */
    default boolean isHostAvailable() {
        final InetSocketAddress address = getInetSocketAddress();
        if (address != null) {
            final boolean reachable = isReachable(address);
            if (!reachable) { // no network
                sendStatus(getUi12WebSocket() == null ? Status.NOT_CONNECTED_YET : Status.NETWORK_DOWN);
            } else { // has network
                sendStatus(getUi12WebSocket() == null ? Status.NOT_CONNECTED_YET : Status.NETWORK_UP);
            }
            return reachable;
        }
        return false;
    }

    /**
     * Getter to return the current WebSocket client.
     *
     * @return the instance of the {@link Ui12WebSocket}.
     */
    Ui12WebSocket getUi12WebSocket();

    /**
     * We need to have InetAddress to be able to check the network connection.
     *
     * @return either null or Ui12 Device inet address
     */
    default InetSocketAddress getInetSocketAddress() {
        final String url = getUrl();
        if (url == null || !(url.split(":").length == 2)) {
            LOG.warn(">> Enter please url in format <server:port> to be able to get Ui12 device");
            sendStatus(Status.NOT_CONNECTED_YET);
            return null;
        }

        try {
            final String host = url.split(":")[0];
            final int port = Integer.parseInt(url.split(":")[1]);
            return new InetSocketAddress(host, port);
        } catch (Exception e) {
            LOG.error(">> Error:", e);
            sendStatus(Status.CLOSED);
        }

        return null;
    }

    /**
     * Getter for the url of the WebSocket server on the Ui12 device.
     *
     * @return <server:port>
     */
    String getUrl();

    /**
     * Platform independent ping.
     *
     * @param inetSocketAddress has a host and a port
     * @return true when the host pings
     */
    default boolean isReachable(final InetSocketAddress inetSocketAddress) {
        try {
            try (Socket soc = new Socket()) {
                soc.connect(inetSocketAddress, PING_TIMEOUT);
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Begins the process in the background for checking the host and the network.
     */
    default void ping() {
        EXECUTOR.execute(() -> {
            while (getUrl() != null) {
                final boolean reachable = isHostAvailable();
                try {
                    TimeUnit.SECONDS.sleep((reachable) ? TEN : FIVE);
                } catch (InterruptedException ignored) {
                }
            }
        });
    }

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
                setUrl(args);
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
        if (!isHostAvailable()) {
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
    default void setUrl(final Atom[] args) {
        if (args != null && args.length == 1) {
            setUrl(args[0].getString());
            LOG.info(">> Url: {}", getUrl());
            ping();
        }
    }

    /**
     * Makes a connection with Ui12 via WebSocket and binds the handler for reading incoming messages.
     */
    default void start() {
        if (!isHostAvailable()) {
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
        if (!isHostAvailable()) {
            // something is a wrong
            setUi12WebSocket(null);
        }

        if (getUi12WebSocket() != null) {
            try {
                getUi12WebSocket().closeBlocking();
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

    /**
     * Setter for the url of the Ui12 device.
     *
     * @param url in format <host:port>
     */
    void setUrl(String url);
}
