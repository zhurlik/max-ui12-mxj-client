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
 * Note: this class is not covered by the unit tests because there is no way to init {@link MaxObject}.
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

    /**
     * Contains the main logic.
     */
    private final CommandHandler commandHandler;

    /**
     * To have the references to the outlets of this {@link MaxObject}.
     */
    private final Outlets outlets;

    /**
     * Builds the Max8 component.
     */
    public Ui12Proxy() {
        declareIO(1, OUTLET_ASSIST.length);
        setInletAssist(INLET_ASSIST);
        setOutletAssist(OUTLET_ASSIST);

        // to be able to use inside others classes
        final List<Consumer<String[]>> list = new ArrayList<>(OUTLET_ASSIST.length);
        list.add((strings) -> outlet(0, strings));
        list.add((strings) -> outlet(1, strings));
        list.add((strings) -> outlet(2, strings));

        outlets = new Outlets(list);
        commandHandler = new CommandHandler(outlets, new UrlHandler(outlets));
        outlets.info(">> Max8 component for working with Ui12 device via WebSocket has been loaded");
    }

    /**
     * Redirects the messages from the inlet to the {@link CommandHandler}.
     *
     * @param message expected values: either 'url' or 'msg'
     * @param args    a body of the message
     */
    @Override
    protected void anything(final String message, final Atom[] args) {
        outlets.debug(">> Max8 signal: message = {}, args = {}", message, Atom.toDebugString(args));
        commandHandler.action(message, args);
    }
}
