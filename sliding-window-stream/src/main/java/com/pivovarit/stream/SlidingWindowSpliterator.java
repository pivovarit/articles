package com.pivovarit.stream;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SlidingWindowSpliterator<T> implements Spliterator<Stream<T>> {

    static <T> Stream<Stream<T>> windowed(Collection<T> stream, int windowSize) {
        return StreamSupport.stream(new SlidingWindowSpliterator<>(stream, windowSize), false);
    }

    private final Queue<T> buffer;

    private final Iterator<T> sourceIterator;
    private final int windowSize;

    private SlidingWindowSpliterator(Collection<T> stream, int windowSize) {
        this.buffer = new ArrayDeque<>(windowSize);
        this.sourceIterator = Objects.requireNonNull(stream).iterator();
        this.windowSize = windowSize;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Stream<T>> action) {
        if (windowSize < 1) {
            return false;
        }

        while (sourceIterator.hasNext()) {
            buffer.add(sourceIterator.next());

            if (buffer.size() == windowSize) {
                action.accept(Arrays.stream((T[]) buffer.toArray(new Object[0])));
                buffer.poll();
                return sourceIterator.hasNext();
            }
        }

        if (!buffer.isEmpty()) {
            action.accept(buffer.stream());
        }

        return false;
    }

    @Override
    public Spliterator<Stream<T>> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return ORDERED | NONNULL;
    }
}
