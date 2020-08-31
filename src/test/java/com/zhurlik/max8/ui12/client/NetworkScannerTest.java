package com.zhurlik.max8.ui12.client;

import com.zhurlik.max8.ui12.component.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;


/**
 * @author zhurlik@gmail.com
 */
class NetworkScannerTest {
    private NetworkScanner test;
    private List<Status> updates = new LinkedList<>();
    private Consumer<Status> sender = updates::add;

    @BeforeEach
    void setUp() {
        updates.clear();
    }

    @Test
    void name() {

    }
}
