package com.zhurlik.max8.ui12.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static com.zhurlik.max8.ui12.component.NetworkScanner.TEN;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;

/**
 * @author zhurlik@gmail.com
 */
@ExtendWith(MockitoExtension.class)
class NetworkScannerTest {
    private final int port = 1234;
    @Mock
    private Outlets outlets;

    @Spy
    private InetSocketAddress address = new InetSocketAddress("localhost", port);

    @InjectMocks
    private NetworkScanner test;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(test, "address", address);
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
    }

    @Test
    void testIsHostAvailable() {
        assertFalse(test.isHostAvailable());
        verify(outlets).toNetworkOutlet(new String[]{"127.0.0.1:1234 offline"});
    }
}
