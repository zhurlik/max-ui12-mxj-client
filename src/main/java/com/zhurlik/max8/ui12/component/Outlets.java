package com.zhurlik.max8.ui12.component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.function.Consumer;

/**
 * That's kind of the wrapper for the {@link com.cycling74.max.MaxObject#outlet(int, String[])} methods.
 * Contains a set of the outlets that are used inside {@link Ui12Proxy}.
 * There are 3 defined outlets:
 * 1. main - for printing all incoming messages from Ui12 device
 * 2. network - to check connection status
 * 3. debug - for printing all debug info
 *
 * @author zhurlik@gmail.com
 */
class Outlets {
    private final List<Consumer<String[]>> outlets;

    /**
     * Default constructor that requires a list of the functions for working with outlets.
     *
     * @param outlets expected 3 elements
     */
    Outlets(final List<Consumer<String[]>> outlets) {
        this.outlets = outlets;
    }

    /**
     * Sends an array of the strings into the Network outlet.
     *
     * @param data strings
     */
    void toNetworkOutlet(final String[] data) {
        getNetworkOutlet().accept(data);
    }

    /**
     * Sends an array of the strings into the Main outlet.
     *
     * @param data strings
     */
    void toMainOutlet(final String[] data) {
        getMainOutlet().accept(data);
    }

    /**
     * Sends an array of the strings into the Debug outlet.
     *
     * @param data strings
     */
    void toDebugOutlet(final String[] data) {
        getDebugOutlet().accept(data);
    }

    /**
     * Log a message in the Debug Outlet.
     *
     * @param msg the message string to be logged
     */
    void info(final String msg) {
        toDebugOutlet(new String[]{String.format("INFO: %s", msg)});
    }

    /**
     * Log a message in the Debug Outlet.
     *
     * @param msg the message string to be logged
     */
    void debug(final String msg) {
        toDebugOutlet(new String[]{String.format("DEBUG: %s", msg)});
    }

    /**
     * Log a message in the Debug Outlet.
     *
     * @param msg the message string to be logged
     */
    void warn(final String msg) {
        toDebugOutlet(new String[]{String.format("WARN: %s", msg)});
    }

    /**
     * Log a message in the Debug Outlet.
     *
     * @param msg the message string to be logged
     * @param throwable any exception
     */
    void error(final String msg, final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        toDebugOutlet(new String[]{String.format("ERROR: %s", sw.toString())});
    }

    /**
     * Log a message in the Debug Outlet.
     *
     * @param log4jFormat a valid pattern for slf4j
     * @param arguments   a list for arguments
     */
    void info(final String log4jFormat, final Object... arguments) {
        final String filledStr = applyArguments(log4jFormat, arguments);
        toDebugOutlet(new String[]{String.format("INFO: %s", filledStr)});
    }

    /**
     * Log a message in the Debug Outlet.
     *
     * @param log4jFormat
     * @param arguments
     */
    void debug(final String log4jFormat, final Object... arguments) {
        final String filledStr = applyArguments(log4jFormat, arguments);
        toDebugOutlet(new String[]{String.format("DEBUG: %s", filledStr)});
    }

    private String applyArguments(final String log4jFormat, final Object[] arguments) {
        final String format = log4jFormat.replaceAll("\\{\\}", "%s");
        final String filledStr = String.format(format, arguments);
        return filledStr;
    }

    private Consumer<String[]> getNetworkOutlet() {
        return outlets.get(1);
    }

    private Consumer<String[]> getMainOutlet() {
        return outlets.get(0);
    }

    private Consumer<String[]> getDebugOutlet() {
        return outlets.get(2);
    }
}
