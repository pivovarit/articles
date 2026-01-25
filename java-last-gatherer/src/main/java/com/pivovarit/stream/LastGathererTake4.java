package com.pivovarit.stream;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

record LastGathererTake4<T>(
  long n) implements Gatherer<T, LastGathererTake4.AppendOnlyCircularBuffer<T>, T> {

    LastGathererTake4 {
        if (n <= 0) {
            throw new IllegalArgumentException("number of elements can't be lower than one");
        }
    }

    @Override
    public Supplier<AppendOnlyCircularBuffer<T>> initializer() {
        return () -> new AppendOnlyCircularBuffer<>((int) n);
    }

    @Override
    public Integrator<AppendOnlyCircularBuffer<T>, T, T> integrator() {
        return Integrator.ofGreedy((state, element, _) -> {
            state.add(element);
            return true;
        });
    }

    @Override
    public BiConsumer<AppendOnlyCircularBuffer<T>, Downstream<? super T>> finisher() {
        return (state, downstream) -> {
            if (!downstream.isRejecting()) {
                state.forEach(downstream::push);
            }
        };
    }

    static class AppendOnlyCircularBuffer<T> {
        private final T[] buffer;
        private int endIdx = 0;
        private int size = 0;

        public AppendOnlyCircularBuffer(int size) {
            this.buffer = (T[]) new Object[size];
        }

        public void add(T element) {
            buffer[endIdx++ % buffer.length] = element;
            if (size < buffer.length) {
                size++;
            }
        }

        public void forEach(Consumer<T> consumer) {
            int startIdx = (endIdx - size + buffer.length) % buffer.length;
            for (int i = 0; i < size; i++) {
                consumer.accept(buffer[(startIdx + i) % buffer.length]);
            }
        }
    }
}
