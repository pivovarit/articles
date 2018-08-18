package com.pivovarit.sealed_package_private;

import java.util.function.Supplier;

public abstract class Option<T> {

    abstract T getOrElse(Supplier<T> other);

    Option() {
    }
}



