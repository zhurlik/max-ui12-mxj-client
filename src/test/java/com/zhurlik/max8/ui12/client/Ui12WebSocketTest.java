package com.zhurlik.max8.ui12.client;

import org.java_websocket.handshake.HandshakeImpl1Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A simple test to check {@link Ui12WebSocket} client.
 *
 * @author zhurlik@gmail.com
 */
class Ui12WebSocketTest {
    private static final Logger LOG = LoggerFactory.getLogger(Ui12WebSocketTest.class);

    private Ui12WebSocket testClient;

    @BeforeEach
    void setUp() throws Exception {
        testClient = new Ui12WebSocket(new URI(String.format("ws://localhost:%d", 1))) {
            @Override
            protected void handle(final String message) {
                assertEquals("test", message);
            }
        };
    }

    @Test
    void testHandleMessage() throws Exception {
        testClient.onMessage("test");
    }

    @Test
    void testOnError() {
        testClient.onError(null);
    }

    @Test
    void testOnOpen() {
        assertThrows(NullPointerException.class, () -> testClient.onOpen(null));
    }

    @Test
    void testOnOpenEmpty() {
        testClient.onOpen(new HandshakeImpl1Server());
    }

    @Test
    void testOnClose() {
        testClient.onClose(1, "test", true);
    }
}
