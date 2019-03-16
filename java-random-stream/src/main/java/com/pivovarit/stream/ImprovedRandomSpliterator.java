package com.pivovarit.stream;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

class ImprovedRandomSpliterator<T, LIST extends RandomAccess & List<T>> implements Spliterator<T> {

    private final Random random;
    private final List<T> source;
    private int size;

    ImprovedRandomSpliterator(LIST source, Supplier<? extends Random> random) {
        Objects.requireNonNull(source, "source can't be null");
        Objects.requireNonNull(random, "random can't be null");

        this.source = source;
        this.random = random.get();
        this.size = this.source.size();
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if (size > 0) {
            int nextIdx = random.nextInt(size);
            int lastIdx = --size;

            T last = source.set(lastIdx, null);
            T elem = source.set(nextIdx, last);
            action.accept(elem);
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
