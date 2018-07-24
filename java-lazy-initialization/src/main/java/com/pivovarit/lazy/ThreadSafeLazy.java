package com.pivovarit.lazy;

import java.util.function.Supplier;

public class ThreadSafeLazy<T> {
    private final Supplier<T> supplier;
    private volatile T value;

    public ThreadSafeLazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        if (value == null) {
            synchronized (this) {
                if (value == null) {
                    value = supplier.get();
                }
            }
        }
        return value;
    }
}
