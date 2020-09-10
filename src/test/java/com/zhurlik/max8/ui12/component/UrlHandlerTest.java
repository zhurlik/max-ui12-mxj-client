package com.zhurlik.max8.ui12.component;

import com.cycling74.max.Atom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * @author zhurlik@gmail.com
 */
@ExtendWith(MockitoExtension.class)
class UrlHandlerTest {

    @InjectMocks
    private UrlHandler test;

    @Mock
    private Outlets outlets;

    @BeforeEach
    void setUp() {
        test = new UrlHandler(outlets);
    }

    @Test
    void testNull() throws Exception {
        test.parse(null);
        assertNotNull(test);
        assertFalse(test.isValidUrl());
        assertEquals("", test.getURI().toString());
    }

    @Test
    void testDefault() throws Exception {
        assertFalse(test.isValidUrl());
        assertEquals("", test.getURI().toString());
    }

    @DisplayName("Not valid cases")
    @ParameterizedTest(name = "{index} ==> the url is ''{0}''")
    @EmptySource
    @ValueSource(strings = {"123", ":", "host", "host:", ":1234", "host:port"})
    void testParseBadValues(final String url) throws Exception {
        // Given
        final Atom[] args = new Atom[] {Atom.newAtom(url)};

        // When
        test.parse(args);

        // Then
        assertFalse(test.isValidUrl());
        assertEquals("", test.getURI().toString());
    }

    @DisplayName("Valid cases")
    @ParameterizedTest(name = "{index} ==> the url is ''{0}'' -> ''{1}''")
    @CsvSource({
            "host:123, ws://host:123/socket.io/1/websocket/",
            "localhost:1256, ws://localhost:1256/socket.io/1/websocket/",
            " localhost : 2345, ws://localhost:2345/socket.io/1/websocket/"
    })
    void testParseValidValues(final String url, final String expected) throws Exception {
        // Given
        final Atom[] args = new Atom[] {Atom.newAtom(url)};

        // When
        test.parse(args);

        // Then
        assertTrue(test.isValidUrl());
        assertEquals(expected, test.getURI().toString());
    }

    @Test
    void testGetInetSocketAddress() {
        assertNull(test.getInetSocketAddress());
        verify(outlets).error(any(IllegalArgumentException.class));
    }

    @Test
    void testGetInetSocketAddressWithValidUrl() {
        final Atom[] args = new Atom[] {Atom.newAtom("localhost:1234")};
        test.parse(args);
        assertNotNull(test.getInetSocketAddress());
    }

    @Test
    void testGetInetSocketAddressWithInvalidUrl() {
        final Atom[] args = new Atom[] {Atom.newAtom("not valid")};
        test.parse(args);
        assertNull(test.getInetSocketAddress());
    }
}
