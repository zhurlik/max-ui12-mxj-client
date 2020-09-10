package com.zhurlik.max8.ui12.component;

import com.cycling74.max.Atom;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

/**
 * To parse the url and to extract the {@link InetSocketAddress}.
 *
 * @author zhurlik@gmail.com
 */
class UrlHandler {
    private String host = "";
    private int port = -1;
    private final Outlets outlets;

    /**
     * Default constructor.
     * @param outlets
     */
    UrlHandler(final Outlets outlets) {
        this.outlets = outlets;
    }

    /**
     * Extracts a host and a port from the url <host:port>.
     *
     * @param args a single from Max8 component via inlet.
     */
    void parse(final Atom[] args) {
        if (args != null && args.length == 1) {
            final String url = args[0].getString();
            if (isExpectedFormat(url)) {
                final String[] parts = url.split(":");
                this.host = Optional.ofNullable(parts[0])
                        .map(String::trim)
                        .orElse("");
                this.port = getPort(parts[1]);
            }
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

    URI getURI() throws URISyntaxException {
        final String url = isValidUrl() ? String.format("ws://%s:%d/socket.io/1/websocket/", host, port) : "";
        outlets.info(">> Endpoint: {}", url);
        return new URI(url);
    }

    /**
     * Retruns {@link InetSocketAddress} for checking the network status.
     *
     * @return InetSocketAddress
     */
    InetSocketAddress getInetSocketAddress() {
        try {
            return new InetSocketAddress(getURI().getHost(), getURI().getPort());
        } catch (Exception e) {
            outlets.error(e);
            return null;
        }
    }
}
