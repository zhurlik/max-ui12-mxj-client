package com.zhurlik.max8.ui12.component;

import com.cycling74.max.Atom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandHandlerTest {

    @InjectMocks
    private CommandHandler test;

    @Mock
    private UrlHandler urlHandler;

    @Mock
    private Ui12WebSocket ui12WebSocket;

    @Mock
    private Outlets outlets;

    @DisplayName("Method action(final int value)")
    @ParameterizedTest(name = "{index} ==> command: ''{0}''")
    @CsvSource({
            "1, 'NOT_CONNECTED_YET'",
            "0, 'NOT_CONNECTED_YET'",
            "10, "})
    void testAction(final int code, final String status) {
        // Given
        // When
        test.action(code);

        // Then
        if (status != null) {
            verify(outlets).toNetworkOutlet(new String[]{"STATUS: " + status});
        }
    }

    @DisplayName("Method action(final String message, final Atom[] args)")
    @ParameterizedTest(name = "{index} ==> command: ''{0}'', parameter: ''{1}''")
    @CsvSource({
            "url, 'localhost:123', true, 'ws://localhost:123/socket.io/1/websocket/'",
            "url, 'localhost123', false, ''",
            "msg, 'test message', false, ''",
            "unsupported, '', false, ''"})
    void testAction(final String command, final String arg, final boolean valid, final String url) throws Exception {
        // Given
        // When
        test.action(command, new Atom[]{Atom.newAtom(arg)});

        // Then
        if ("url".equals(command)) {
            verify(urlHandler).parse(new Atom[]{Atom.newAtom(arg)});
        }
        if ("msg".equals(command)) {
            verify(outlets).toNetworkOutlet(new String[]{"STATUS: NOT_CONNECTED_YET"});
        }
    }

    @Test
    void testSetUrlAndSend() throws Exception {
        // Given
        final String url = "localhost:1234";
        ReflectionTestUtils.setField(test, "ui12WebSocket", ui12WebSocket);

        // When
        // set url
        test.action("url", new Atom[]{Atom.newAtom(url)});
        // start
        test.action("msg", new Atom[]{Atom.newAtom("test")});

        // Then
        verify(outlets).debug(">> Command:{}", "SET_URL");
        verify(urlHandler).parse(new Atom[]{Atom.newAtom(url)});
        verify(urlHandler).getInetSocketAddress();
        verify(outlets).debug(">> Command:{}", "SEND_MESSAGE");
        verify(ui12WebSocket).toUi12Device(new Atom[]{Atom.newAtom("test")});
    }

    @Test
    void testStartThenSend() throws Exception {
        // Given
        final String url = "localhost:1234";
        ReflectionTestUtils.setField(test, "ui12WebSocket", ui12WebSocket);
        when(urlHandler.isValidUrl()).thenReturn(true);

        // When
        test.action("url", new Atom[]{Atom.newAtom(url)});
        test.action(1);
        test.action("msg", new Atom[]{Atom.newAtom("test message")});

        // Then
        verify(urlHandler).parse(new Atom[]{Atom.newAtom(url)});
        verify(urlHandler).getInetSocketAddress();
        verify(outlets).debug(">> Command:{}", "SET_URL");
        verify(outlets).debug(">> Command:{}", "START");
        verify(outlets).debug(">> Starting Job...");
        verify(outlets).debug(">> Command:{}", "SEND_MESSAGE");
        verify(ui12WebSocket).toUi12Device(new Atom[]{Atom.newAtom("test message")});
    }

    @Test
    void testStartThenStop() throws Exception {
        // Given
        final String url = "localhost:1234";
        ReflectionTestUtils.setField(test, "ui12WebSocket", ui12WebSocket);
        when(urlHandler.isValidUrl()).thenReturn(true);

        // When
        test.action("url", new Atom[]{Atom.newAtom(url)});
        test.action(1);
        test.action(0);

        // Then
        verify(outlets).debug(">> Command:{}", "SET_URL");
        verify(urlHandler).parse(new Atom[]{Atom.newAtom(url)});
        verify(urlHandler).getInetSocketAddress();
        verify(outlets).debug(">> Command:{}", "START");
        verify(outlets).debug(">> Starting Job...");
        verify(outlets).debug(">> Command:{}", "STOP");
        verify(outlets).debug(">> Stopping Job...");
    }
}
