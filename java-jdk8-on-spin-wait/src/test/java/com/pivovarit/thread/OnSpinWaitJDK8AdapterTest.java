package com.pivovarit.thread;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

class OnSpinWaitJDK8AdapterTest {

    private static final AtomicBoolean flag = new AtomicBoolean(false);

    @Test
    void example_1() throws Exception {
        while (!flag.compareAndSet(false, true)) {
            OnSpinWaitJDK8Adapter.onSpinWaitOrNothing();
        }
    }

    @Test
    void example_2() throws Exception {
        while (!flag.compareAndSet(false, true)) {
            OnSpinWaitThreadYieldFallbackJDK8Adapter.onSpinWaitOrYield();
        }
    }

    @AfterEach
    void reset() {
        flag.set(false);
    }
}