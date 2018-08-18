package com.pivovarit.sealed_package_private;

import java.util.function.Supplier;

public final class Some<T> extends Option<T> {

    private final T value;

    public Some(T value) {
        this.value = value;
    }

    @Override
    T getOrElse(Supplier<T> other) {
        return value;
    }
}
