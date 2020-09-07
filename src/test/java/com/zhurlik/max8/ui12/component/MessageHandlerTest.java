package com.zhurlik.max8.ui12.component;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private Outlets outlets;

    @Test
    void testNull() {
        test.accept(null);
        verify(outlets, never()).toMainOutlet(any());
        verify(outlets, never()).toNetworkOutlet(any());
        verify(outlets, never()).toDebugOutlet(any());
        assertNotNull(test.getOutlets());
    }

    @Test
    void testBlank() {
        test.accept("");
        verify(outlets).toMainOutlet(new String[]{""});
    }

    @Test
    void testSed() {
        test.accept("^SED^");
        verify(outlets).toMainOutlet(new String[]{" SED \" \""});
    }

    @Test
    void testArgs() {
        test.accept("^SED^ 1 2");
        verify(outlets).toMainOutlet(new String[]{" SED  1 2"});
    }

    @Test
    void testMultiple() {
        test.accept("^SED^ 1 2\nSED ");
        verify(outlets).toMainOutlet(new String[]{" SED  1 2"});
        verify(outlets).toMainOutlet(new String[]{"SED \" \""});
    }
}
