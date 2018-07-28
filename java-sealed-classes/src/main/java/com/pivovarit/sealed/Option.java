package com.pivovarit.sealed;

import java.util.function.Supplier;

public abstract class Option<T> {

    abstract T getOrElse(Supplier<T> other);

    private Option() {
    }

    public final static class Some<T> extends Option<T> {

        private final T value;

        public Some(T value) {
            this.value = value;
        }

        @Override
        T getOrElse(Supplier<T> other) {
            return value;
        }
    }

    public final static class None<T> extends Option<T> {
        @Override
        T getOrElse(Supplier<T> other) {
            return other.get();
        }
    }
}



