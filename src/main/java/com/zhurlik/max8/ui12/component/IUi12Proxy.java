package com.zhurlik.max8.ui12.component;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * An interface to extract the common constants and methods.
 *
 * @author zhurlik
 */
public interface IUi12Proxy {
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
