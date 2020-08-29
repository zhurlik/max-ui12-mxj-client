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
 * Note: most of the logic has been moved to the default methods in the {@link IUi12Proxy}.
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
        action(message, args);
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
        action(value);
    }

    @Override
    public void sendStatus(final Status status) {
        outlet(0, new String[]{
                String.format("STATUS: %s", status.name())
        });
    }

    @Override
    public Ui12WebSocket buildWebSocketClient() throws Exception {
        final String endpoint = String.format("ws://%s/socket.io/1/websocket/", getUrl());
        LOG.info(">> Endpoint: {}", endpoint);

        return new Ui12WebSocket(new URI(endpoint)) {

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
    }

    @Override
    public Ui12WebSocket getUi12WebSocket() {
        return ui12WebSocket;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUi12WebSocket(final Ui12WebSocket ui12WebSocket) {
        this.ui12WebSocket = ui12WebSocket;
    }

    @Override
    public void setUrl(final String url) {
        this.url = url;
    }
}
