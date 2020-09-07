package com.zhurlik.max8.ui12.component;

import com.cycling74.max.Atom;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.HandshakeImpl1Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private Outlets outlets;

    @BeforeEach
    void setUp() throws Exception {
        testClient = new Ui12WebSocket(new URI(String.format("ws://localhost:%d", 1)), new MessageHandler(outlets));
    }

    @Test
    void testHandleMessage() throws Exception {
        testClient.onMessage("test");
        verify(outlets).toMainOutlet(new String[]{"test"});
    }

    @Test
    void testOnError() {
        testClient.onError(null);
        verify(outlets).toNetworkOutlet(new String[]{"STATUS: CLOSED"});
    }

    @Test
    void testOnOpen() {
        assertThrows(NullPointerException.class, () -> testClient.onOpen(null));
        verify(outlets, never()).toNetworkOutlet(any());
    }

    @Test
    void testOnOpenEmpty() {
        testClient.onOpen(new HandshakeImpl1Server());
        verify(outlets).toNetworkOutlet(new String[]{"STATUS: CONNECTED"});
    }

    @Test
    void testOnClose() {
        testClient.onClose(1, "test", false);
        verify(outlets).toNetworkOutlet(new String[]{"STATUS: CLOSED"});
        verify(outlets).debug(eq(">> WebSocket connection has been closed."));
        verify(outlets).debug(eq(">> Server response: code = {}, reason = {}, remote = {}"), any());
        verify(outlets).debug(eq(">> Session time: {}"), any());
    }

    @Test
    void testOnCloseRemote() {
        testClient.onClose(1, "test", true);
        verify(outlets).toNetworkOutlet(new String[]{"STATUS: CLOSED"});
        verify(outlets).debug(eq(">> WebSocket connection has been closed."));
        verify(outlets).debug(eq(">> Server response: code = {}, reason = {}, remote = {}"), any());
        verify(outlets).debug(eq(">> Session time: {}"), any());
    }

    @Test
    void testUpWhenNoNetwork() {
        //when(network.isHostAvailable()).thenReturn(false);
        //testClient.up();
        //verify(network, never()).ping();
        verify(outlets, never()).toNetworkOutlet(any());
    }

    @Test
    void testDownWhenNoNetwork() {
        //when(network.isHostAvailable()).thenReturn(false);
        //testClient.down();
        //verify(network, never()).stopPing();
        verify(outlets, never()).toNetworkOutlet(any());
    }

    @Test
    void testUpWithNetwork() {
        //when(network.isHostAvailable()).thenReturn(true);
        //testClient.up();
        //verify(network).ping();
        //verify(outlets.get(1), times(2)).accept(new String[]{"STATUS: CLOSED"});
    }

    @Test
    void testDownWithNetwork() {
        //when(network.isHostAvailable()).thenReturn(true);
        //testClient.down();
        //verify(network).stopPing();
        //verify(outlets.get(1)).accept(new String[]{"STATUS: CLOSED"});
    }

    @Test
    void testToUi12DeviceNoNetwork() {
        //when(network.isHostAvailable()).thenReturn(false);
        testClient.toUi12Device(null);
    }

    @Test
    void testToUi12DeviceWithNetwork() {
        //when(network.isHostAvailable()).thenReturn(true);
        assertThrows(WebsocketNotConnectedException.class, () -> testClient.toUi12Device(
                new Atom[]{Atom.newAtom("test")}));
    }

    @Test
    void testToUi12DeviceNullWithNetwork() {
        //when(network.isHostAvailable()).thenReturn(true);
        testClient.toUi12Device(null);
    }
}
