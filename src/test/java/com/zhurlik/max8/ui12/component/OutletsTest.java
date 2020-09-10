package com.zhurlik.max8.ui12.component;

import com.cycling74.max.Atom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutletsTest {
    @InjectMocks
    private Outlets test;

    @Spy
    private List<Consumer<String[]>> outlets = new ArrayList<>();

    @Mock
    private Consumer<String[]> mainOutlet;
    @Mock
    private Consumer<String[]> networkOutlet;
    @Mock
    private Consumer<String[]> debugOutlet;

    @BeforeEach
    void setUp() {
        outlets.add(mainOutlet);
        outlets.add(networkOutlet);
        outlets.add(debugOutlet);
    }

    @Test
    void testToMain() {
        final String[] data = {"test main"};
        test.toMainOutlet(data);
        verify(outlets).get(0);
        verify(mainOutlet).accept(data);
    }

    @Test
    void testToNetwork() {
        final String[] data = {"test network"};
        test.toNetworkOutlet(data);
        verify(outlets).get(1);
        verify(networkOutlet).accept(data);
    }

    @Test
    void testToDebug() {
        final String[] data = {"test debug"};
        test.toDebugOutlet(data);
        verify(outlets).get(2);
        verify(debugOutlet).accept(data);
    }

    @Test
    void testInfo() {
        final String msg = "test";
        test.info(msg);
        verify(outlets).get(2);
        verify(debugOutlet).accept(new String[] {"INFO: test"});
    }

    @Test
    void testWarn() {
        final String msg = "test";
        test.warn(msg);
        verify(outlets).get(2);
        verify(debugOutlet).accept(new String[] {"WARN: test"});
    }

    @Test
    void testError() {
        test.error(new RuntimeException("test error"));
        verify(outlets).get(2);
        verify(debugOutlet).accept(argThat(argument -> argument[0]
                .startsWith("ERROR: java.lang.RuntimeException: test error")));
    }

    @Test
    void testInfoWithParam() {
        final String msg = "test {}";
        test.info(msg, "bla-bla");
        verify(outlets).get(2);
        verify(debugOutlet).accept(new String[] {"INFO: test bla-bla"});
    }

    @Test
    void testDebug() {
        final String msg = "test param1: {}, param2: {}";
        test.debug(msg, "value1",
                Atom.toDebugString(new Atom[] {Atom.newAtom(1), Atom.newAtom("hello")}));
        verify(outlets).get(2);
        verify(debugOutlet).accept(new String[] {"DEBUG: test param1: value1, param2: Atom[2]={1:I}{hello:S}"});
    }

    @Test
    void testDebugStr() {
        final String msg = "test";
        test.debug(msg);
        verify(outlets).get(2);
        verify(debugOutlet).accept(new String[] {"DEBUG: test"});
    }
}
