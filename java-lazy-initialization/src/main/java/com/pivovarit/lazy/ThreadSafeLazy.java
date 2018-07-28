package com.pivovarit.lazy;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
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

    public <R> ThreadSafeLazy<R> map(Function<T, R> mapper) {
        return new ThreadSafeLazy<>(() -> mapper.apply(this.get()));
    }

    public ThreadSafeLazy<Optional<T>> filter(Predicate<T> predicate) {
        return new ThreadSafeLazy<>(() -> Optional.of(this.get()).filter(predicate));
    }
}


