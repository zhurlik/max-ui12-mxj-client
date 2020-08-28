package com.zhurlik.max8.ui12.component;

import com.cycling74.max.Atom;
import com.cycling74.max.MaxObject;
import com.zhurlik.max8.ui12.client.Ui12WebSocket;

import java.net.URI;
import java.util.Arrays;

/**
 * Max8 component for working with Ui12 device via WebSocket connection.
 * The main idea to read/send messages and to check connection with WebSocket Server on the Ui12 device.
 *
 * @author zhurlik@gmail.com
 */
public class Ui12Proxy extends MaxObject implements IUi12Proxy {

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
    protected void anything(final String message, final Atom[] args) {
        LOG.debug(">> Max8 signal: message = {}, args = {}", message, Atom.toDebugString(args));
        switch (message) {
            case "url":
                setUrl(args);
                break;
            case "msg":
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
    protected void inlet(final int value) {
        LOG.debug(">> Max8 signal: value = {}", value);
        LOG.debug(">> Inlet:{}", getInlet());

        switch (value) {
            case 1:
                start();
                break;
            case 0:
                stop();
                break;
            default:
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
                 * A multi-line message will be split into multiple messages.
                 *
                 * NOTE: there are a few custom replacements of the original message.
                 *
                 * @param message incoming message via WebSocket
                 */
                @Override
                protected void handle(final String message) {
                    // split by '\n'
                    Arrays.stream(message.split("\n"))
                            // replace '^' -> ' '
                            .map(s -> s.replaceAll("\\^", " "))
                            // trick with last space
                            .map(s -> s.replaceAll(" $", " \" \""))
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

    @Override
    public void sendStatus(final Status status) {
        outlet(0, new String[]{
                String.format("STATUS: %s", status.name())
        });
    }

    @Override
    public Ui12WebSocket getUi12WebSocket() {
        return ui12WebSocket;
    }

    @Override
    public String getUrl() {
        return url;
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
}
