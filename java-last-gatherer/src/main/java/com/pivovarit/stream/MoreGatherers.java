package com.pivovarit.stream;

import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

class MoreGatherers {
    static <T> Gatherer<T, ?, T> last(int n) {
        return switch (n) {
            case 1 -> new SingleElementLastGatherer<>();
            default -> new CircularBufferLastGatherer<>(n);
        };
    }

    record CircularBufferLastGatherer<T>(int n) implements Gatherer<T, CircularBufferLastGatherer.AppendOnlyCircularBuffer<T>, T> {

        public CircularBufferLastGatherer {
            if (n <= 0) {
                throw new IllegalArgumentException("number of elements can't be lower than one");
            }
        }

        @Override
        public Supplier<CircularBufferLastGatherer.AppendOnlyCircularBuffer<T>> initializer() {
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
            private final int mask;
            private final int limit;

            private int size;
            private int writeIdx;

            AppendOnlyCircularBuffer(int limit) {
                this.limit = Math.max(0, limit);
                int capacity = nextPowerOfTwo(Math.max(1, this.limit));
                this.buffer = new Object[capacity];
                this.mask = capacity - 1;
            }

            void add(T e) {
                buffer[writeIdx & mask] = e;
                writeIdx++;
                if (size < limit) {
                    size++;
                }
            }

            T get(int index, int start) {
                return (T) buffer[(start + index) & mask];
            }

            void pushAll(Gatherer.Downstream<? super T> ds) {
                int start = (writeIdx - size) & mask;

                for (int i = 0; i < size && !ds.isRejecting(); i++) {
                    if (!ds.push(get(i, start))) {
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
}
