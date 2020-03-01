package com.pivovarit.thread;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;

class OnSpinWaitThreadYieldFallbackJDK8Adapter {
    private static final MethodHandle ON_SPIN_WAIT_HANDLE = resolve();

    private OnSpinWaitThreadYieldFallbackJDK8Adapter() {
    }

    private static MethodHandle resolve() {
        try {
            return MethodHandles.lookup().findStatic(Thread.class, "onSpinWait", methodType(void.class));
        } catch (Exception ignore) {
        }

        try {
            return MethodHandles.lookup().findStatic(Thread.class, "yield", methodType(void.class));
        } catch (Exception ignore) {
        }

        return null;
    }

    static void onSpinWaitOrYield() {
        if (ON_SPIN_WAIT_HANDLE != null) {
            try {
                ON_SPIN_WAIT_HANDLE.invokeExact();
            } catch (Throwable ignore) {
            }
        }
    }
}
