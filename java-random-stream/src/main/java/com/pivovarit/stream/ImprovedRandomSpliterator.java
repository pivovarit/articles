package com.pivovarit.stream;

import java.util.List;
import java.util.Random;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

class ImprovedRandomSpliterator<T> implements Spliterator<T> {

    private final Random random;
    private final List<T> source;
    private int size;

    /**
     * To be used only with {@link List} implementations supporting O(1) index access
     */
    ImprovedRandomSpliterator(List<T> source, Supplier<? extends Random> random) {
        if (source.isEmpty()) {
            throw new IllegalArgumentException("RandomSpliterator can't be initialized with an empty collection");
        }

        this.source = source;
        this.random = random.get();
        this.size = this.source.size();
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if (size > 0) {
            int nextIdx = random.nextInt(size);
            int lastIdx = size - 1;

            action.accept(source.set(nextIdx, source.set(lastIdx, null)));
            size--;
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
