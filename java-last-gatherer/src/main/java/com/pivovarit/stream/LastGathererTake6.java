package com.pivovarit.stream;

import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

record LastGathererTake6<T>(int n) implements Gatherer<T, LastGathererTake6.AppendOnlyCircularBuffer<T>, T> {

    public LastGathererTake6 {
        if (n <= 0) {
            throw new IllegalArgumentException("number of elements can't be lower than one");
        }
    }

    @Override
    public Supplier<AppendOnlyCircularBuffer<T>> initializer() {
        return () -> new AppendOnlyCircularBuffer<>(n);
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
        return AppendOnlyCircularBuffer::pushAll;
    }

    static final class AppendOnlyCircularBuffer<T> {
        private final Object[] buffer;
        private final int limit;

        private long count;

        AppendOnlyCircularBuffer(int limit) {
            this.limit = Math.max(0, limit);
            this.buffer = new Object[nextPowerOfTwo(Math.max(1, this.limit))];
        }

        void add(T e) {
            buffer[(int) count & (buffer.length - 1)] = e;
            count++;
        }

        void pushAll(Gatherer.Downstream<? super T> ds) {
            Object[] b = buffer;
            int mask = b.length - 1;
            int size = (int) Math.min(count, limit);
            int start = (int) ((count - size) & mask);

            for (int i = 0; i < size && !ds.isRejecting(); i++) {
                if (!ds.push((T) b[(start + i) & mask])) {
                    break;
                }
            }
        }

        private static int nextPowerOfTwo(int x) {
            int highest = Integer.highestOneBit(x);
            return (x == highest) ? x : (highest << 1);
        }
    }
}
