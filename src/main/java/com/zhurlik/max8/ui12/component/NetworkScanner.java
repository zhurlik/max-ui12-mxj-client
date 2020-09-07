package com.zhurlik.max8.ui12.component;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is doing a lot of with checking the network in general and connection with Ui12 device.
 *
 * @author zhurlik@gamil.com
 */
class NetworkScanner {
    /**
     * A single thread to check network status.
     */
    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    // Timeouts
    /**
     * Waiting time to next ping when the network is down.
     */
    public static final int FIVE = 5;
    /**
     * Waiting time to next ping when the network is up.
     */
    public static final int TEN = 10;

    /**
     * Waiting time to ping a host in the network.
     */
    private static final int PING_TIMEOUT = 3000;

    /**
     * Reference to Max8 outlets: main, network and debug.
     */
    private final Outlets outlets;
    private final InetSocketAddress address;
    private final AtomicBoolean stop = new AtomicBoolean(false);

    /**
     * The default constructor for binding the fields.
     *
     * @param address {@link InetSocketAddress} for checking the network
     * @param outlets reference to {@link com.cycling74.max.MaxObject#outlet(int, String[])}
     */
    NetworkScanner(final InetSocketAddress address, final Outlets outlets) {
        this.outlets = outlets;
        this.address = address;
    }

    /**
     * Checks if the host is available in the network.
     *
     * @return true when Ui12 Device is available in the network
     */
    boolean isHostAvailable() {
        if (address != null) {
            final boolean reachable = isReachable();
            final String host = address.getAddress().toString().split("/")[1];
            final int port = address.getPort();
            final String status = reachable ? "online" : "offline";
            outlets.toNetworkOutlet(new String[] {String.format("%s:%d %s", host, port, status)});

            return reachable;
        }
        return false;
    }

    /**
     * Platform independent ping.
     *
     * @return true when the host pings
     */
    boolean isReachable() {
        try {
            try (Socket soc = new Socket()) {
                soc.connect(address, PING_TIMEOUT);
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * NOTE: that's a background process.
     */
    void ping() {
        EXECUTOR.execute(() -> {
            while (!stop.get()) {
                final boolean reachable = isHostAvailable();
                try {
                    TimeUnit.SECONDS.sleep((reachable) ? TEN : FIVE);
                } catch (InterruptedException ignored) {
                }
            }
        });
    }

    /**
     * Stops the process in the background for checking the network.
     */
    void stopPing() {
        stop.set(false);
    }
}
