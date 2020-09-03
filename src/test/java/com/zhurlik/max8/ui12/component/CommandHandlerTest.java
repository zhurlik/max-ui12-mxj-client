package com.zhurlik.max8.ui12.component;


import com.cycling74.max.Atom;
import com.zhurlik.max8.ui12.client.Ui12WebSocket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommandHandlerTest {

    @InjectMocks
    private CommandHandler test;

    @Spy
    private UrlHandler urlHandler;

    @Mock
    private Ui12WebSocket ui12WebSocket;

    @Mock
    private Consumer<String[]> outlet;

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
            verify(outlet).accept(eq(new String[]{"STATUS: " + status}));
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
            verify(outlet).accept(new String[]{"STATUS: NOT_CONNECTED_YET"});
        }
    }

    @Test
    void testSetUrlAndSend() throws Exception {
        // Given
        final String url = "localhost:1234";

        // When
        // set url
        test.action("url", new Atom[]{Atom.newAtom(url)});
        // start
        test.action(1);

        // Then
        assertTrue(urlHandler.isValidUrl());
        assertEquals("ws://localhost:1234/socket.io/1/websocket/", urlHandler.getURI().toString());
        verify(outlet).accept(new String[]{"STATUS: NETWORK_DOWN"});
    }
}
