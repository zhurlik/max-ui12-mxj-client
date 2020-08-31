package com.zhurlik.max8.ui12.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageHandlerTest {
    private MessageHandler test;

    private final List<String[]> output = new LinkedList<>();
    private final Consumer<String[]> outlet = strings -> output.add(strings);

    @BeforeEach
    void setUp() {
        test = new MessageHandler(outlet);
        output.clear();
    }

    @Test
    void testNull() {
        test.accept(null);
        assertTrue(output.isEmpty());
    }

    @Test
    void testBlank() {
        test.accept("");
        assertEquals(1, output.size());
        assertArrayEquals(new String[]{""}, output.get(0));
    }

    @Test
    void testSed() {
        test.accept("^SED^");
        assertEquals(1, output.size());
        assertArrayEquals(new String[]{" SED \" \""}, output.get(0));
    }

    @Test
    void testArgs() {
        test.accept("^SED^ 1 2");
        assertEquals(1, output.size());
        assertArrayEquals(new String[]{" SED  1 2"}, output.get(0));
    }

    @Test
    void testMultiple() {
        test.accept("^SED^ 1 2\nSED ");
        assertEquals(2, output.size());
        assertArrayEquals(new String[]{" SED  1 2"}, output.get(0));
        assertArrayEquals(new String[]{"SED \" \""}, output.get(1));
    }
}
