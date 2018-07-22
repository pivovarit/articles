package com.pivovarit.lazy;

import java.util.function.Supplier;

public class Lazy<T> {

    private volatile T value;

    private Supplier<T> supplier;

    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        if (value == null) {
            synchronized (this) {
                if (value == null) {
                    value = supplier.get();
                    supplier = null;
                }
            }
        }
        return value;
    }
}
