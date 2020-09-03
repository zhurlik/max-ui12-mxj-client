package com.zhurlik.max8.ui12.component;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @author zhurlik@gmail.com
 */
@ExtendWith(MockitoExtension.class)
class MessageHandlerTest {
    @InjectMocks
    private MessageHandler test;

    @Mock
    private Consumer<String[]> outlet;

    @Test
    void testNull() {
        test.accept(null);
        verify(outlet, never()).accept(any());
        assertNotNull(test.getOutlet());
    }

    @Test
    void testBlank() {
        test.accept("");
        verify(outlet).accept(new String[]{""});
    }

    @Test
    void testSed() {
        test.accept("^SED^");
        verify(outlet).accept(new String[]{" SED \" \""});
    }

    @Test
    void testArgs() {
        test.accept("^SED^ 1 2");
        verify(outlet).accept(new String[]{" SED  1 2"});
    }

    @Test
    void testMultiple() {
        test.accept("^SED^ 1 2\nSED ");
        verify(outlet).accept(new String[]{" SED  1 2"});
        verify(outlet).accept(new String[]{"SED \" \""});
    }
}
