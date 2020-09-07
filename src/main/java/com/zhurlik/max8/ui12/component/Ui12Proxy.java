package com.zhurlik.max8.ui12.component;

import com.cycling74.max.Atom;
import com.cycling74.max.MaxObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Max8 component for working with Ui12 device via WebSocket connection.
 * The main idea to read/send messages and to check connection with WebSocket Server on the Ui12 device.
 *
 * @author zhurlik@gmail.com
 */
public final class Ui12Proxy extends MaxObject {
    /**
     * Inlet label.
     */
    private static final String[] INLET_ASSIST = new String[]{"Input"};

    /**
     * Outlet label.
     */
    private static final String[] OUTLET_ASSIST = new String[]{"Main Output", "Network Status", "Debug Console"};

    private final CommandHandler commandHandler;
    private final Outlets outlets;

    /**
     * Builds the Max8 component.
     */
    public Ui12Proxy() {
        declareIO(1, OUTLET_ASSIST.length);
        setInletAssist(INLET_ASSIST);
        setOutletAssist(OUTLET_ASSIST);

        final List<Consumer<String[]>> list = new ArrayList<>(OUTLET_ASSIST.length);
        list.add((strings) -> outlet(0, strings));
        list.add((strings) -> outlet(1, strings));
        list.add((strings) -> outlet(2, strings));

        outlets = new Outlets(list);
        commandHandler = new CommandHandler(outlets, new UrlHandler(outlets));
        outlets.info(">> Max8 component for working with Ui12 device via WebSocket has been loaded");
    }

    /**
     * Reads the messages from the inlet of the Max8 component.
     *
     * @param message either 'url' or 'msg'
     * @param args    a body of the message
     */
    @Override
    protected void anything(final String message, final Atom[] args) {
        outlets.debug(">> Max8 signal: message = {}, args = {}", message, Atom.toDebugString(args));
        commandHandler.action(message, args);
    }

    /**
     * Reads 2 signals: 1- start and 0 - stop.
     *
     * @param value expected 1 or 2
     */
    @Override
    protected void inlet(final int value) {
        outlets.debug(">> Max8 signal: value = {}", value);
        outlets.debug(">> Inlet:{}", getInlet());
        commandHandler.action(value);
    }
}
