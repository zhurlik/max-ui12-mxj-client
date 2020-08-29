package com.zhurlik.max8.ui12.component;

/**
 * Internal statuses about the WebSocket connection and the network state.
 *
 * @author zhurlik@gmail.com
 */
public enum Status {
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
    RECONNECTED,
    // network
    /**
     * When the network is up.
     */
    NETWORK_UP,
    /**
     * When the network is down.
     */
    NETWORK_DOWN;
}
