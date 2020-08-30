package com.zhurlik.max8.ui12.component;

import com.cycling74.max.Atom;
import com.zhurlik.max8.ui12.client.Ui12WebSocket;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.zhurlik.max8.ui12.component.IUi12Proxy.LOG;

/**
 * This class is doing a lot of with checking the network in general and connection with Ui12 device.
 *
 * @author zhurlik@gamil.com
 */
public final class Scanner {

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

    private String host = "";
    private int port = -1;

    /**
     * Reference to Max8 outlet for sending statuses via the outlet.
     */
    private final Consumer<Status> sender;

    /**
     * The constructor that extract the server url from incoming Max8 signal.
     *
     * @param args Max8 signal from the inlet
     * @param sender reference to function for sending messages via the outlet.
     */
    Scanner(final Atom[] args, final Consumer<Status> sender) {
        this.sender = sender;
        if (args != null && args.length == 1) {
            final String url = args[0].getString();
            parse(url);
        }
    }

    /**
     * Extracts a host and a port from the url <host:port>.
     *
     * @param url <server:port>
     */
    private void parse(final String url) {
        if (isExpectedFormat(url)) {
            final String[] parts = url.split(":");
            this.host = Optional.ofNullable(parts[0])
                    .map(String::trim)
                    .orElse("");
            this.port = getPort(parts[1]);
        }
    }

    /**
     * Check that the url is in the expected format.
     *
     * @param url <server:port>
     * @return true when <server:port>
     */
    private boolean isExpectedFormat(final String url) {
        return url != null && url.split(":").length == 2;
    }

    /**
     * Converts a string to the int.
     *
     * @param str any string
     * @return -1 when something is wrong
     */
    private int getPort(final String str) {
        return Optional.ofNullable(str)
                .map(String::trim)
                .map(s -> {
                    try {
                        return Integer.parseInt(s);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .orElse(-1);
    }

    /**
     * Checks that the url is correct.
     *
     * @return true that host is not blank and the port is not -1
     */
    boolean isValidUrl() {
        return !host.isEmpty() && port != -1;
    }

    @Override
    public String toString() {
        return isValidUrl() ? String.format("%s:%d", host, port) : "";
    }

    /**
     * Returns the InetSocketAddress to be able to check the network connection with Ui12 device.
     *
     * @return instance of {@link InetSocketAddress}
     */
    InetSocketAddress toInetSocketAddress() {
        return isValidUrl() ? new InetSocketAddress(host, port) : null;
    }

    /**
     * We need to have InetAddress to be able to check the network connection.
     *
     * @return either null or Ui12 Device inet address
     */
    InetSocketAddress getInetSocketAddress() {
        if (!isValidUrl()) {
            LOG.warn(">> Enter please url in format <server:port> to be able to get Ui12 device");
            sender.accept(Status.NOT_CONNECTED_YET);
            return null;
        }

        try {
            return toInetSocketAddress();
        } catch (Exception e) {
            LOG.error(">> Error:", e);
            sender.accept(Status.CLOSED);
        }

        return null;
    }

    /**
     * Checks if the host is available in the network.
     *
     * @param ui12WebSocket a reference to {@link Ui12WebSocket}
     * @return true when Ui12 Device is available in the network
     */
    boolean isHostAvailable(final Ui12WebSocket ui12WebSocket) {
        final InetSocketAddress address = getInetSocketAddress();
        if (address != null) {
            final boolean reachable = isReachable(address);
            if (!reachable) { // no network
                sender.accept(ui12WebSocket == null ? Status.NOT_CONNECTED_YET : Status.NETWORK_DOWN);
            } else { // has network
                sender.accept(ui12WebSocket == null ? Status.NOT_CONNECTED_YET : Status.NETWORK_UP);
            }
            return reachable;
        }
        return false;
    }

    /**
     * Platform independent ping.
     *
     * @param inetSocketAddress has a host and a port
     * @return true when the host pings
     */
    boolean isReachable(final InetSocketAddress inetSocketAddress) {
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
     *
     * @param ui12WebSocket getter for having a reference to {@link Ui12WebSocket}
     */
    void ping(final Supplier<Ui12WebSocket> ui12WebSocket) {
        EXECUTOR.execute(() -> {
            while (isValidUrl()) {
                final boolean reachable = isHostAvailable(ui12WebSocket.get());
                try {
                    TimeUnit.SECONDS.sleep((reachable) ? TEN : FIVE);
                } catch (InterruptedException ignored) {
                }
            }
        });
    }
}
