package com.zhurlik.max8.ui12.component;

import com.zhurlik.max8.ui12.client.Ui12WebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.zhurlik.max8.ui12.component.IUi12Proxy.FIVE;
import static com.zhurlik.max8.ui12.component.IUi12Proxy.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * A little bit tests for checking {@link IUi12Proxy}.
 *
 * @author zhurlik@gmail.com
 */
class IUi12ProxyTest {

    private String url;
    private Ui12WebSocket ui12WebSocket;

    private IUi12Proxy test = spy(new IUi12Proxy() {
        @Override
        public Ui12WebSocket getUi12WebSocket() {
            return ui12WebSocket;
        }

        @Override
        public String getUrl() {
            return url;
        }
    });

    @BeforeEach
    void setUp() {
        url = null;
        ui12WebSocket = null;
    }

    @Test
    void testGetInetSocketAddress() {
        // Given
        url = null;

        // When
        final InetSocketAddress address = test.getInetSocketAddress();

        // Then
        assertNull(address);
        verify(test).sendStatus(IUi12Proxy.Status.NOT_CONNECTED_YET);
    }

    @Test
    void testGetInetSocketAddressNoPort() {
        // Given
        url = "host";

        // When
        final InetSocketAddress address = test.getInetSocketAddress();

        // Then
        assertNull(address);
        verify(test).sendStatus(IUi12Proxy.Status.NOT_CONNECTED_YET);
    }

    @Test
    void testGetInetSocketAddressBlankPort() {
        // Given
        url = "host:";

        // When
        final InetSocketAddress address = test.getInetSocketAddress();

        // Then
        assertNull(address);
        verify(test).sendStatus(IUi12Proxy.Status.NOT_CONNECTED_YET);
    }

    @Test
    void testGetInetSocketAddressBothBlankHostAndPort() {
        // Given
        url = ":";

        // When
        final InetSocketAddress address = test.getInetSocketAddress();

        // Then
        assertNull(address);
        verify(test).sendStatus(IUi12Proxy.Status.NOT_CONNECTED_YET);
    }

    @Test
    void testGetInetSocketAddressEmpty() {
        // Given
        url = "";

        // When
        final InetSocketAddress address = test.getInetSocketAddress();

        // Then
        assertNull(address);
        verify(test).sendStatus(IUi12Proxy.Status.NOT_CONNECTED_YET);
    }

    @Test
    void testGetInetSocketAddressHostAndWrongPort() {
        // Given
        url = "localhost:wrong";

        // When
        final InetSocketAddress address = test.getInetSocketAddress();

        // Then
        assertNull(address);
        verify(test).sendStatus(IUi12Proxy.Status.CLOSED);
    }

    @Test
    void testGetInetSocketAddressHostAndPort() {
        // Given
        url = "localhost:1234";

        // When
        final InetSocketAddress address = test.getInetSocketAddress();

        // Then
        assertNotNull(address);
        assertEquals("localhost/127.0.0.1:1234", address.toString());
        verify(test, never()).sendStatus(any(IUi12Proxy.Status.class));
    }

    @Test
    void testIsHostAvailableNotConnectedYet() {
        // Given
        url = "localhost:1234";

        // When
        final boolean res = test.isHostAvailable();

        // Then
        assertFalse(res);
        verify(test).sendStatus(IUi12Proxy.Status.NOT_CONNECTED_YET);
    }

    @Test
    void testIsHostAvailable() throws Exception {
        // Given
        url = "localhost:1234";
        ui12WebSocket = new Ui12WebSocket(new URI("ws://localhost:1234")) {
            @Override
            protected void handle(final String message) {
            }
        };

        // When
        final boolean res = test.isHostAvailable();

        // Then
        assertFalse(res);
        verify(test).sendStatus(IUi12Proxy.Status.NETWORK_DOWN);
    }

    @Test
    void testIsReachableWhenNull() {
        assertFalse(test.isReachable(null));
    }

    @Test
    void testIsReachableFakeHost() {
        assertFalse(test.isReachable(new InetSocketAddress("fake-host", 1)));
    }

    @Test
    void testIsReachableFakePort() {
        assertFalse(test.isReachable(new InetSocketAddress("localhost", 1)));
    }

    @Test
    void testStatus() {
        assertEquals("NOT_CONNECTED_YET,CONNECTED,CLOSED,RECONNECTED,NETWORK_UP,NETWORK_DOWN",
                Arrays.stream(IUi12Proxy.Status.values()).map(Enum::name).collect(Collectors.joining(",")));
    }

    @Test
    void testPingNothing() throws Exception {
        test.ping();
        TimeUnit.SECONDS.sleep(FIVE);

        verify(test, never()).sendStatus(any(IUi12Proxy.Status.class));
    }

    @Test
    void testPingLocalHost() throws Exception {
        reset(test);
        url = "localhost:1234";
        test.ping();
        TimeUnit.SECONDS.sleep(TEN);

        verify(test, atLeast(2)).sendStatus(IUi12Proxy.Status.NOT_CONNECTED_YET);
    }
}
