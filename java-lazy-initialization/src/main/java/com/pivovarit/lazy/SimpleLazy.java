package com.pivovarit.lazy;

import java.util.function.Supplier;

public class SimpleLazy<T> {
    private final Supplier<T> supplier;
    private T value;

    public SimpleLazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        if (value == null) {
            value = supplier.get();
        }
        return value;
    }
}
