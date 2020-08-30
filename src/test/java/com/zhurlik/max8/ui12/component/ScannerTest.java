package com.zhurlik.max8.ui12.component;


import com.cycling74.max.Atom;
import com.zhurlik.max8.ui12.client.Ui12WebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.zhurlik.max8.ui12.component.Scanner.FIVE;
import static com.zhurlik.max8.ui12.component.Scanner.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author zhurlik@gmail.com
 */
class ScannerTest {
    private Scanner test;
    private List<Status> updates = new LinkedList<>();
    private Consumer<Status> sender = updates::add;

    @BeforeEach
    void setUp() {
        updates.clear();
    }

    @Test
    void testWithNull() {
        test = new Scanner(null, sender);
        assertEquals("", test.toString());
        assertFalse(test.isValidUrl());
        assertNull(test.toInetSocketAddress());
    }

    @Test
    void testWithEmpty() {
        test = new Scanner(new Atom[0], sender);
        assertEquals("", test.toString());
        assertFalse(test.isValidUrl());
        assertNull(test.toInetSocketAddress());
    }

    @Test
    void testWhenWrongFormat() {
        test = new Scanner(new Atom[]{Atom.newAtom("wrong")}, sender);
        assertEquals("", test.toString());
        assertFalse(test.isValidUrl());
        assertNull(test.toInetSocketAddress());
    }

    @Test
    void testWhenNoPort() {
        test = new Scanner(new Atom[]{Atom.newAtom("fake-host:")}, sender);
        assertEquals("", test.toString());
        assertFalse(test.isValidUrl());
        assertNull(test.toInetSocketAddress());
    }

    @Test
    void testWhenBadPort() {
        test = new Scanner(new Atom[]{Atom.newAtom("fake-host:ad")}, sender);
        assertEquals("", test.toString());
        assertFalse(test.isValidUrl());
        assertNull(test.toInetSocketAddress());
    }

    @Test
    void testWhenNoHostAndPort() {
        test = new Scanner(new Atom[]{Atom.newAtom(":1234")}, sender);
        assertEquals("", test.toString());
        assertFalse(test.isValidUrl());
        assertNull(test.toInetSocketAddress());
    }

    @Test
    void testWhenHostAndPort() {
        test = new Scanner(new Atom[]{Atom.newAtom("fake-host:1234")}, sender);
        assertEquals("fake-host:1234", test.toString());
        assertTrue(test.isValidUrl());
        assertNotNull(test.toInetSocketAddress());
    }

    @Test
    void testIsReachableWhenNull() {
        test = new Scanner(null, sender);
        assertFalse(test.isReachable(null));
    }

    @Test
    void testIsReachableFakeHost() {
        test = new Scanner(null, sender);
        assertFalse(test.isReachable(new InetSocketAddress("fake-host", 1)));
    }

    @Test
    void testIsReachableFakePort() {
        test = new Scanner(null, sender);
        assertFalse(test.isReachable(new InetSocketAddress("localhost", 1)));
    }

    @Test
    void testGetInetSocketAddress() {
        // Given
        test = new Scanner(null, sender);
        // When
        final InetSocketAddress address = test.getInetSocketAddress();

        // Then
        assertNull(address);
        assertEquals(1, updates.size());
        assertSame(Status.NOT_CONNECTED_YET, updates.get(0));
    }

    @Test
    void testGetInetSocketAddressNoPort() {
        // Given
        test = new Scanner(new Atom[]{Atom.newAtom("host")}, sender);

        // When
        final InetSocketAddress address = test.getInetSocketAddress();

        // Then
        assertNull(address);
        assertEquals(1, updates.size());
        assertSame(Status.NOT_CONNECTED_YET, updates.get(0));
    }

    @Test
    void testGetInetSocketAddressHostAndPort() {
        // Given
        test = new Scanner(new Atom[]{Atom.newAtom("localhost:1234")}, sender);

        // When
        final InetSocketAddress address = test.getInetSocketAddress();

        // Then
        assertNotNull(address);
        assertEquals("localhost/127.0.0.1:1234", address.toString());
        assertTrue(updates.isEmpty());
    }

    @Test
    void testIsHostAvailableNotConnectedYet() {
        // Given
        test = new Scanner(new Atom[]{Atom.newAtom("localhost:1234")}, sender);

        // When
        final boolean res = test.isHostAvailable(null);

        // Then
        assertFalse(res);
        assertEquals(1, updates.size());
        assertSame(Status.NOT_CONNECTED_YET, updates.get(0));
    }

    @Test
    void testIsHostAvailable() throws Exception {
        // Given
        test = new Scanner(new Atom[]{Atom.newAtom("localhost:1234")}, sender);
        final Ui12WebSocket ui12WebSocket = new Ui12WebSocket(new URI("ws://localhost:1234")) {
            @Override
            protected void handle(final String message) {
            }
        };

        // When
        final boolean res = test.isHostAvailable(ui12WebSocket);

        // Then
        assertFalse(res);
        assertEquals(1, updates.size());
        assertSame(Status.NETWORK_DOWN, updates.get(0));
    }

    @Test
    void testPingNothing() throws Exception {
        test = new Scanner(null, sender);
        test.ping(() -> null);
        TimeUnit.SECONDS.sleep(FIVE);

        assertTrue(updates.isEmpty());
    }

    @Test
    void testPingLocalHost() throws Exception {
        test = new Scanner(new Atom[]{Atom.newAtom("localhost:1234")}, sender);
        test.ping(() -> null);
        TimeUnit.SECONDS.sleep(TEN);

        assertEquals(2, updates.size());
        assertSame(Status.NOT_CONNECTED_YET, updates.get(0));
        assertSame(Status.NOT_CONNECTED_YET, updates.get(1));
    }
}
