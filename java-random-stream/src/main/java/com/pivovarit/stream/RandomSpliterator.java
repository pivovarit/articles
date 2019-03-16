package com.pivovarit.stream;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

class RandomSpliterator<T, LIST extends RandomAccess & List<T>> implements Spliterator<T> {

    private final Random random;
    private final List<T> source;

    RandomSpliterator(LIST source, Supplier<? extends Random> random) {
        Objects.requireNonNull(source, "source can't be null");
        Objects.requireNonNull(random, "random can't be null");

        this.source = source;
        this.random = random.get();
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        int remaining = source.size();
        if (remaining > 0) {
            action.accept(source.remove(random.nextInt(remaining)));
            return true;
        } else {
            return false;
        }

    }

    @Override
    public Spliterator<T> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return source.size();
    }

    @Override
    public int characteristics() {
        return SIZED;
    }
}
