package com.zhurlik.max8.ui12.component;

import com.zhurlik.max8.ui12.client.Ui12WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * An interface to extract the common constants and methods.
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
     * Internal statuses about the WebSocket connection and the network state.
     */
    enum Status {
        // websocket
        NOT_CONNECTED_YET,
        CONNECTED,
        CLOSED,
        RECONNECTED,
        // network
        NETWORK_UP,
        NETWORK_DOWN;
    }
}
