package com.zhurlik.max8.ui12.component;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A simple unit tests for {@link Status}.
 *
 * @author zhurlik@gmail.com
 */
class StatusTest {
    @Test
    void testStatus() {
        assertEquals("NOT_CONNECTED_YET,CONNECTED,CLOSED,RECONNECTED,NETWORK_UP,NETWORK_DOWN",
                Arrays.stream(Status.values()).map(Enum::name).collect(Collectors.joining(",")));
    }

    @ParameterizedTest
    @EnumSource(Status.class)
    void testConvert(final Status status) {
        assertArrayEquals(new String[]{"STATUS: " + status.name()}, status.convert());
    }
}
