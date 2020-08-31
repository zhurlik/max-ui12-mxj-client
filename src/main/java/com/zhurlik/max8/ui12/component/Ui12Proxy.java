package com.zhurlik.max8.ui12.component;

import com.cycling74.max.Atom;
import com.cycling74.max.MaxObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Max8 component for working with Ui12 device via WebSocket connection.
 * The main idea to read/send messages and to check connection with WebSocket Server on the Ui12 device.
 *
 * @author zhurlik@gmail.com
 */
public final class Ui12Proxy extends MaxObject {
    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger("Ui12Proxy");

    /**
     * Inlet label.
     */
    private static final String[] INLET_ASSIST = new String[]{"Input"};

    /**
     * Outlet label.
     */
    private static final String[] OUTLET_ASSIST = new String[]{"Output"};

    private final CommandHandler commandHandler;

    /**
     * Builds the Max8 component.
     */
    public Ui12Proxy() {
        LOG.info(">> Max8 component for working with Ui12 device via WebSocket");
        declareIO(1, 1);
        setInletAssist(INLET_ASSIST);
        setOutletAssist(OUTLET_ASSIST);
        commandHandler = new CommandHandler((strings) -> outlet(0, strings), new UrlHandler());
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
        commandHandler.action(message, args);
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
        commandHandler.action(value);
    }
}
