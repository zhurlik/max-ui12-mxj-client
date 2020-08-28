package com.zhurlik.max8.ui12.component;

import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * A little bit tests for checking {@link IUi12Proxy}.
 *
 * @author zhurlik@gmail.com
 */
class IUi12ProxyTest {

    private IUi12Proxy test = new IUi12Proxy() {
    };

    @Test
    void testIsReachableWhenNull() {
        assertFalse(test.isReachable(null));
    }

    @Test
    void testIsReachableLocalHost() {
        assertFalse(test.isReachable(new InetSocketAddress("fake-host", 1)));
    }

    @Test
    void testStatus() {
        assertEquals("NOT_CONNECTED_YET,CONNECTED,CLOSED,RECONNECTED,NETWORK_UP,NETWORK_DOWN",
                Arrays.stream(IUi12Proxy.Status.values()).map(Enum::name).collect(Collectors.joining(",")));
    }
}
