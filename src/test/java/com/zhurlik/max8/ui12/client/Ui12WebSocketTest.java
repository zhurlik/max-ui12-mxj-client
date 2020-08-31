package com.zhurlik.max8.ui12.client;

import com.zhurlik.max8.ui12.component.MessageHandler;
import org.java_websocket.handshake.HandshakeImpl1Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A simple test to check {@link Ui12WebSocket} client.
 *
 * @author zhurlik@gmail.com
 */
class Ui12WebSocketTest {

    private Ui12WebSocket testClient;
    private MessageHandler handler;
    private String[] outletMessage;

    @BeforeEach
    void setUp() throws Exception {
        handler = new MessageHandler((strings) -> outletMessage = strings);
        testClient = new Ui12WebSocket(new URI(String.format("ws://localhost:%d", 1)), handler);
    }

    @Test
    void testHandleMessage() throws Exception {
        testClient.onMessage("test");
        assertArrayEquals(new String[]{"test"}, outletMessage);
    }

    @Test
    void testOnError() {
        testClient.onError(null);
        assertArrayEquals(new String[]{"STATUS: CLOSED"}, outletMessage);
    }

    @Test
    void testOnOpen() {
        assertThrows(NullPointerException.class, () -> testClient.onOpen(null));
        assertNull(outletMessage);
    }

    @Test
    void testOnOpenEmpty() {
        testClient.onOpen(new HandshakeImpl1Server());
        assertArrayEquals(new String[]{"STATUS: CONNECTED"}, outletMessage);
    }

    @Test
    void testOnClose() {
        testClient.onClose(1, "test", true);
        assertArrayEquals(new String[]{"STATUS: CLOSED"}, outletMessage);
    }
}
