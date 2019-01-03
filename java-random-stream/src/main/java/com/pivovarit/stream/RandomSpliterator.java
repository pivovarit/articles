package com.pivovarit.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RandomSpliterator<T> implements Spliterator<T> {

    private final Random random;
    private final List<T> source;

    RandomSpliterator(List<T> source, Supplier<? extends Random> random) {
        if (source.isEmpty()) {
            throw new IllegalArgumentException("RandomSpliterator can't be initialized with an empty collection");
        }
        this.source = source instanceof RandomAccess ? source : new ArrayList<>(source);
        this.random = random.get();
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        int remaining = source.size();
        action.accept(source.remove(random.nextInt(remaining)));
        return remaining - 1 > 0;
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
