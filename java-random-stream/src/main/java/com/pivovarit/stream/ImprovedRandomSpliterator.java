package com.pivovarit.stream;

import java.util.List;
import java.util.Random;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ImprovedRandomSpliterator<T> implements Spliterator<T> {

    private final Random random;
    private final T[] source;
    private int size;

    ImprovedRandomSpliterator(List<T> source, Supplier<? extends Random> random) {
        if (source.isEmpty()) {
            throw new IllegalArgumentException("RandomSpliterator can't be initialized with an empty collection");
        }
        this.source = (T[]) source.toArray();
        this.random = random.get();
        this.size = this.source.length;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        int nextIdx = random.nextInt(size);
        int lastIdx = size - 1;

        action.accept(source[nextIdx]);
        source[nextIdx] = source[lastIdx];
        source[lastIdx] = null; // let object be GCed
        return --size > 0;
    }

    @Override
    public Spliterator<T> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return source.length;
    }

    @Override
    public int characteristics() {
        return SIZED;
    }
}
