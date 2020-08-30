package com.zhurlik.max8.ui12.component;

import com.cycling74.max.Atom;
import com.zhurlik.max8.ui12.client.Ui12WebSocket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * A little bit tests for checking {@link IUi12Proxy}.
 *
 * @author zhurlik@gmail.com
 */
class IUi12ProxyTest {

    private Ui12WebSocket ui12WebSocket;
    private Scanner scanner;

    private IUi12Proxy test = spy(new IUi12Proxy() {
        @Override
        public Ui12WebSocket getUi12WebSocket() {
            return ui12WebSocket;
        }

        @Override
        public Scanner getServerScanner() {
            return scanner;
        }

        @Override
        public Ui12WebSocket buildWebSocketClient() throws URISyntaxException {
            return null;
        }

        @Override
        public void setUi12WebSocket(final Ui12WebSocket ui12WebSocket) {

        }
    });

    @BeforeEach
    void setUp() {
        ui12WebSocket = null;
    }

    @AfterEach
    void tearDown() {
        reset(test);
    }


    @Test
    void testActionIntNothing() {
        test.action(-1);
        verify(test, never()).sendStatus(any(Status.class));
    }

    @Test
    void testActionIntStopNoUrl() {
        // Given
        // When
        test.action(0);

        // Then
        assertNull(test.getUi12WebSocket());
        verify(test).sendStatus(Status.NOT_CONNECTED_YET);
    }

    @Test
    void testActionIntStartNoUrl() {
        // Given
        scanner = null;
        // When
        test.action(1);

        // Then
        assertNull(test.getUi12WebSocket());
        verify(test).sendStatus(Status.NOT_CONNECTED_YET);
    }

    @Test
    void testActionIntStopWrongUrl() {
        // Given
        scanner = new Scanner(new Atom[]{Atom.newAtom("bad:port")}, test::sendStatus);

        // When
        test.action(0);

        // Then
        assertNull(test.getUi12WebSocket());
        verify(test, times(2)).sendStatus(Status.NOT_CONNECTED_YET);
    }

    @Test
    void testActionIntStartWrongUrl() {
        // Given
        scanner = new Scanner(new Atom[]{Atom.newAtom("bad:port")}, test::sendStatus);

        // When
        test.action(1);

        // Then
        assertNull(test.getUi12WebSocket());
        verify(test).sendStatus(Status.NOT_CONNECTED_YET);
    }

    @Test
    void testActionIntStopLocalHost() {
        // Given
        scanner = new Scanner(new Atom[]{Atom.newAtom("localhost:1234")}, test::sendStatus);

        // When
        test.action(0);

        // Then
        assertNull(test.getUi12WebSocket());
        verify(test, times(2)).sendStatus(Status.NOT_CONNECTED_YET);
    }

    @Test
    void testActionIntStartLocalHost() {
        // Given
        scanner = new Scanner(new Atom[]{Atom.newAtom("localhost:1234")}, test::sendStatus);

        // When
        test.action(1);

        // Then
        assertNull(test.getUi12WebSocket());
        verify(test).sendStatus(any(Status.class));
    }

    @Test
    void testActionIntStopWebSocket() throws Exception {
        // Given
        scanner = new Scanner(new Atom[]{Atom.newAtom("localhost:1234")}, test::sendStatus);
        ui12WebSocket = new Ui12WebSocket(new URI("ws://localhost:1234")) {
            @Override
            protected void handle(final String message) {

            }
        };

        // When
        test.action(0);

        // Then
        assertNotNull(test.getUi12WebSocket());
        verify(test).sendStatus(Status.NOT_CONNECTED_YET);
    }

    @Test
    void testActionIntStopOpenWebSocket() throws Exception {
        // Given
        scanner = new Scanner(new Atom[]{Atom.newAtom("localhost:1234")}, test::sendStatus);
        ui12WebSocket = new Ui12WebSocket(new URI("ws://localhost:1234")) {
            @Override
            protected void handle(final String message) {

            }
        };

        // When
        ui12WebSocket.connect();
        test.action(0);

        // Then
        assertNotNull(test.getUi12WebSocket());
        verify(test).sendStatus(Status.NOT_CONNECTED_YET);
        verify(test).sendStatus(Status.CLOSED);
    }

    @Test
    void testDefineUrlNull() {
        test.defineUrl(null);
    }
}
