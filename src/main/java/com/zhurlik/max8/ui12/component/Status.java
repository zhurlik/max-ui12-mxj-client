package com.zhurlik.max8.ui12.component;

/**
 * Internal statuses about the WebSocket connection and the network state.
 *
 * @author zhurlik@gmail.com
 */
enum Status {
    // websocket
    /**
     * When we still haven't up the WebSocket client.
     */
    NOT_CONNECTED_YET,
    /**
     * When the connection has been completed.
     */
    CONNECTED,
    /**
     * When the connection has been closed.
     */
    CLOSED,
    /**
     * When the connection has been reconnected.
     */
    RECONNECTED;

    /**
     * That's needed for sending to the outlet.
     * See {@link com.cycling74.max.MaxObject#outlet(int, String[])}
     *
     * @return required format for the outlet
     */
    String[] convert() {
        return new String[]{
                String.format("STATUS: %s", name())
        };
    }
}
