package com.zhurlik.max8.ui12.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.zhurlik.max8.ui12.client.NetworkScanner.TEN;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author zhurlik@gmail.com
 */
@ExtendWith(MockitoExtension.class)
class NetworkScannerTest {
    @Mock
    private Consumer<String[]> outlet;

    @Mock
    private InetSocketAddress address;

    @InjectMocks
    private NetworkScanner test;

    @Test
    void testConstructorWithNulls() {
        assertThrows(NullPointerException.class, () -> new NetworkScanner(null, null));
    }

    @Test
    void testIsReachable() {
        assertFalse(test.isReachable());
    }

    @Test
    void testPingAndStop() throws Exception {
        test.ping();
        TimeUnit.SECONDS.sleep(TEN);
        test.stopPing();
        verify(outlet, times(2)).accept(new String[]{"STATUS: NETWORK_DOWN"});
    }

    @Test
    void testIsHostAvailable() {
        assertFalse(test.isHostAvailable());
        verify(outlet).accept(new String[]{"STATUS: NETWORK_DOWN"});
    }
}
