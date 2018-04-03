package com.pivovarit.stream;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

class WindowStream {

    static <T> Stream<List<T>> windowed(Stream<T> stream, int windowSize) {
        return StreamSupport.stream(new WindowSpliterator<>(stream, windowSize), false);
    }

    static class WindowSpliterator<T> implements Spliterator<List<T>> {

        private final Queue<T> buffer;

        private final Iterator<T> streamIterator;
        private final int windowSize;

        WindowSpliterator(Stream<T> stream, int windowSize) {
            Objects.requireNonNull(stream);

            this.buffer = new ArrayDeque<>(windowSize);
            this.streamIterator = stream.iterator();
            this.windowSize = windowSize;
        }

        @Override
        public boolean tryAdvance(Consumer<? super List<T>> action) {
            if (windowSize < 1) {
                return false;
            }

            while (streamIterator.hasNext()) {
                T next = streamIterator.next();
                buffer.add(next);

                if (buffer.size() == windowSize) {
                    action.accept(new ArrayList<>(buffer));
                    buffer.poll();
                    return streamIterator.hasNext();
                }
            }

            if (!buffer.isEmpty()) {
                action.accept(new ArrayList<>(buffer));
            }

            return false;
        }

        @Override
        public Spliterator<List<T>> trySplit() {
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
}
