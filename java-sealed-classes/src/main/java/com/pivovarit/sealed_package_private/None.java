package com.pivovarit.sealed_package_private;

import java.util.function.Supplier;

public final class None<T> extends Option<T> {
    @Override
    T getOrElse(Supplier<T> other) {
        return other.get();
    }
}
