package com.zhurlik.max8.ui12.component;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        assertEquals("SET_URL,SEND_MESSAGE,UNDEFINED",
                Arrays.stream(Command.values()).map(Enum::name).collect(Collectors.joining(",")));
    }

    @DisplayName("Return a command by a string")
    @ParameterizedTest(name = "{index} ==> the string ''{0}'' the command ''{1}''")
    @MethodSource("commandsProvider")
    void testFindByString(final String str, final Command expected) {
        assertSame(expected, Command.findBy(str));
    }

    private static Stream<Arguments> commandsProvider() {
        return Stream.of(
                Arguments.arguments("", Command.UNDEFINED),
                Arguments.arguments(null, Command.UNDEFINED),
                Arguments.arguments("url", Command.SET_URL),
                Arguments.arguments("uRl", Command.SET_URL),
                Arguments.arguments("msg", Command.SEND_MESSAGE),
                Arguments.arguments("msG", Command.SEND_MESSAGE)
        );
    }
}
