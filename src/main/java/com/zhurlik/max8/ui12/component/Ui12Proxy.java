package com.zhurlik.max8.ui12.component;

import com.cycling74.max.Atom;
import com.cycling74.max.MaxObject;
import com.zhurlik.max8.ui12.client.Ui12WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.net.InetAddress.getByName;

/**
 * Max8 component for working with Ui12 device via WebSocket connection.
 * The main idea to read/send messages and to check connection with WebSocket Server on the Ui12 device.
 *
 * @author zhurlik@gmail.com
 */
public class Ui12Proxy extends MaxObject {
    private static final Logger LOG = LoggerFactory.getLogger(Ui12Proxy.class);
    private static final String[] INLET_ASSIST = new String[]{"Input"};
    private static final String[] OUTLET_ASSIST = new String[]{"Output"};

    // a single thread to check network status.
    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    // the client for connecting to the WebSocket server on the Ui12 device.
    private Ui12WebSocket ui12WebSocket;

    // expected <server:port>
    private String url;

    /**
     * Builds the Max8 component.
     */
    public Ui12Proxy() {
        LOG.info(">> Max8 component for working with Ui12 device via WebSocket");
        declareIO(1, 1);
        setInletAssist(INLET_ASSIST);
        setOutletAssist(OUTLET_ASSIST);
    }

    /**
     * Reads the messages from the inlet of the Max8 component.
     *
     * @param message either 'url' or 'msg'
     * @param args    a body of the message
     */
    @Override
    protected void anything(String message, Atom[] args) {
        LOG.debug(">> Max8 signal: message = {}, args = {}", message, Atom.toDebugString(args));
        switch (message) {
            case "url":
                setUrl(args);
                break;
            case "msg":
                toUi12Device(args);
                break;
            default: {

            }
        }
    }

    /**
     * Checks the current connection, makes reconnection when it's needed and sends a message to Ui12 via WebSocket.
     *
     * @param args incoming from Max8
     */
    private void toUi12Device(final Atom[] args) {
        if (ui12WebSocket == null) {
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
            if (ui12WebSocket.isClosed() || ui12WebSocket.isClosing()) {
                LOG.warn(">> Reconnecting...");
                try {
                    ui12WebSocket.reconnectBlocking();
                    sendStatus(Status.RECONNECTED);
                } catch (InterruptedException e) {
                    LOG.error(">> Error:", e);
                    sendStatus(Status.CLOSED);
                    // something is a wrong
                    ui12WebSocket = null;
                    return;
                }
            }
            LOG.debug(">> Sending: {}", message);
            ui12WebSocket.send(message);
        }
    }

    /**
     * Gets a single string and sets it into the url field.
     *
     * @param args incoming from Max8
     */
    private void setUrl(final Atom[] args) {
        if (args != null && args.length == 1) {
            url = args[0].getString();
            LOG.info(">> Url: {}", url);
            ping();
        }
    }

    /**
     * Reads 2 signals: 1- start and 0 - stop.
     *
     * @param value expected 1 or 2
     */
    @Override
    protected void inlet(int value) {
        LOG.debug(">> Max8 signal: value = {}", value);
        LOG.debug(">> Inlet:{}", getInlet());

        switch (value) {
            case 1:
                start();
                break;
            case 0:
                stop();
                break;
            default: {
            }
        }
    }

    /**
     * Makes a connection with Ui12 via WebSocket and binds the handler for reading incoming messages.
     */
    private void start() {
        if (!isHostAvailable()) {
            return;
        }

        if (ui12WebSocket != null && ui12WebSocket.isOpen()) {
            LOG.warn(">> The connection has been already opened");
            try {
                ui12WebSocket.closeBlocking();
                sendStatus(Status.CLOSED);
            } catch (InterruptedException e) {
                LOG.error(">> Error:", e);
                sendStatus(Status.CLOSED);
                // something is a wrong
                ui12WebSocket = null;
            }
        }

        try {
            final String endpoint = String.format("ws://%s/socket.io/1/websocket/", url);
            LOG.info(">> Endpoint: {}", endpoint);
            ui12WebSocket = new Ui12WebSocket(new URI(endpoint)) {

                /**
                 * Note that multi-line message will be split into  multiple messages.
                 *
                 * @param message incoming message via WebSocket
                 */
                @Override
                protected void handle(final String message) {
                    // split by '\n'
                    Arrays.stream(message.split("\n"))
                            // replace '^' -> ' '
                            .map(s -> s.replaceAll("\\^", " "))
                            // sending to the corresponded outlet
                            .forEach(s -> outlet(0, new String[]{s}));
                }
            };
            ui12WebSocket.connectBlocking();
            sendStatus(Status.CONNECTED);
        } catch (Exception e) {
            LOG.error(">> Error:", e);
            // something is a wrong
            ui12WebSocket = null;
            sendStatus(Status.NOT_CONNECTED_YET);
        }
    }

    /**
     * Begins the process in the background for checking the host and the network.
     */
    private void ping() {
        EXECUTOR.execute(() -> {
            while (url != null) {
                final boolean reachable = isHostAvailable();
                try {
                    TimeUnit.SECONDS.sleep((reachable) ? 10 : 5);
                } catch (InterruptedException ignored) {
                }
            }
        });
    }

    /**
     * Checks if the host is available in the network.
     *
     * @return true when Ui12 Device is available in the network
     */
    private boolean isHostAvailable() {
        final InetAddress address = getInetAddress();
        if (address != null) {
            try {
                final boolean reachable = address.isReachable(5000);
                if (!reachable) { // no network
                    sendStatus(ui12WebSocket == null ? Status.NOT_CONNECTED_YET : Status.NETWORK_DOWN);
                } else { // has network
                    sendStatus(ui12WebSocket == null ? Status.NOT_CONNECTED_YET : Status.NETWORK_UP);
                }
                return reachable;
            } catch (IOException e) {
                LOG.debug(">> Error:", e);
            }
        }
        return false;
    }

    /**
     * We need to have InetAddress to be able to check the network connection.
     *
     * @return either null or Ui12 Device inet address
     */
    private InetAddress getInetAddress() {
        if (url == null) {
            LOG.warn(">> Enter please url in format <server:port> to be able to get Ui12 device");
            sendStatus(Status.NOT_CONNECTED_YET);
            return null;
        }

        try {
            final String host = url.split(":")[0];
            return getByName(host);
        } catch (Exception e) {
            LOG.error(">> Error:", e);
            sendStatus(Status.CLOSED);
        }

        return null;
    }

    /**
     * Sends to the outlet the message about WebSocket/network connections status.
     *
     * @param status one of the {@link Status}
     */
    private void sendStatus(final Status status) {
        outlet(0, new String[]{
                String.format("STATUS: %s", status.name())
        });
    }

    /**
     * Closes the current WebSocket connection.
     */
    private void stop() {
        if (!isHostAvailable()) {
            // something is a wrong
            ui12WebSocket = null;
        }

        if (ui12WebSocket != null) {
            try {
                ui12WebSocket.closeBlocking();
            } catch (InterruptedException e) {
                LOG.error(">> Error:", e);
            }
            sendStatus(Status.CLOSED);
            // something is a wrong
            ui12WebSocket = null;
        }

        sendStatus(Status.NOT_CONNECTED_YET);
    }

    /**
     * Internal statuses about the WebSocket connection and the network state.
     */
    private enum Status {
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
