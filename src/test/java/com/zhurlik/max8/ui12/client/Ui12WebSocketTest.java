package com.zhurlik.max8.ui12.client;

import com.zhurlik.max8.ui12.component.MessageHandler;
import org.java_websocket.handshake.HandshakeImpl1Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * A simple test to check {@link Ui12WebSocket} client.
 *
 * @author zhurlik@gmail.com
 */
@ExtendWith(MockitoExtension.class)
class Ui12WebSocketTest {

    private Ui12WebSocket testClient;

    @Mock
    private Consumer<String[]> outlet;

    @BeforeEach
    void setUp() throws Exception {
        testClient = new Ui12WebSocket(new URI(String.format("ws://localhost:%d", 1)), new MessageHandler(outlet));
    }

    @Test
    void testHandleMessage() throws Exception {
        testClient.onMessage("test");
        verify(outlet).accept(new String[]{"test"});
    }

    @Test
    void testOnError() {
        testClient.onError(null);
        verify(outlet).accept(new String[]{"STATUS: CLOSED"});
    }

    @Test
    void testOnOpen() {
        assertThrows(NullPointerException.class, () -> testClient.onOpen(null));
        verify(outlet, never()).accept(any());
    }

    @Test
    void testOnOpenEmpty() {
        testClient.onOpen(new HandshakeImpl1Server());
        verify(outlet).accept(new String[]{"STATUS: CONNECTED"});
    }

    @Test
    void testOnClose() {
        testClient.onClose(1, "test", true);
        verify(outlet).accept(new String[]{"STATUS: CLOSED"});
    }
}
