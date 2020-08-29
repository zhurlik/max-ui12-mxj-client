package com.zhurlik.max8.ui12.component;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * A simple unit tests for {@link Command}.
 *
 * @author zhurlik@gmail.com
 */
class CommandTest {
    @Test
    void testStatus() {
        assertEquals("START,STOP,SET_URL,SEND_MESSAGE,UNDEFINED",
                Arrays.stream(Command.values()).map(Enum::name).collect(Collectors.joining(",")));
    }

    @Test
    void testFindByIntNull() {
        assertSame(Command.UNDEFINED, Command.findBy(null));
    }

    @Test
    void testFindByIntNegative() {
        assertSame(Command.UNDEFINED, Command.findBy(-1));
    }

    @Test
    void testFindByIntStart() {
        assertSame(Command.START, Command.findBy(1));
    }

    @Test
    void testFindByIntStop() {
        assertSame(Command.STOP, Command.findBy(0));
    }

    @Test
    void testFindByStringNull() {
        assertSame(Command.UNDEFINED, Command.findBy((String) null));
    }

    @Test
    void testFindByStringEmpty() {
        assertSame(Command.UNDEFINED, Command.findBy(""));
    }

    @Test
    void testFindByStringUrl() {
        assertSame(Command.SET_URL, Command.findBy("url"));
    }

    @Test
    void testFindByStringMessage() {
        assertSame(Command.SEND_MESSAGE, Command.findBy("msg"));
    }
}
