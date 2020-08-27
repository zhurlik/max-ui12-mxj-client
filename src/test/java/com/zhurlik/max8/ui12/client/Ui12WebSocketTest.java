package com.zhurlik.max8.ui12.client;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A simple test to check {@link Ui12WebSocket} client.
 *
 * @author zhurlik@gmail.com
 */
class Ui12WebSocketTest {
    private static final Logger LOG = LoggerFactory.getLogger(Ui12WebSocketTest.class);
    private static final int PORT = 9012;

    private final LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<>();
    private Ui12WebSocket testClient;
    private boolean stop;
    private final TestServer server = new TestServer();

    @BeforeEach
    void setUp() throws Exception {
        server.start();
        testClient = new Ui12WebSocket(new URI(String.format("ws://localhost:%d", PORT))) {
            @Override
            protected void handle(final String message) {
                try {
                    messages.put(message);
                } catch (InterruptedException e) {
                    LOG.error("ERROR:", e);
                }
            }
        };
        testClient.connect();
        stop = false;
        while (!stop) {
            stop = testClient.isOpen();
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        server.stop();
        // important delay
        TimeUnit.SECONDS.sleep(2);
        testClient.close();
        stop = false;
        while (!stop) {
            stop = testClient.isClosed();
        }
    }

    @Test
    void testHandleMessage() throws Exception {
        server.broadcast("test");
        assertEquals("test", messages.take());
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
    void testOnClose() {
        testClient.onClose(1, "test", true);
    }

    /**
     * A simple WebSocket server just for testing.
     */
    private final class TestServer extends WebSocketServer {
        private TestServer() {
            super(new InetSocketAddress(PORT));
        }

        @Override
        public void onOpen(final WebSocket conn, final ClientHandshake handshake) {
        }

        @Override
        public void onClose(final WebSocket conn, final int code, final String reason, final boolean remote) {
        }

        @Override
        public void onMessage(final WebSocket conn, final String message) {
        }

        @Override
        public void onError(final WebSocket conn, final Exception ex) {
        }

        @Override
        public void onStart() {
        }
    }
}
