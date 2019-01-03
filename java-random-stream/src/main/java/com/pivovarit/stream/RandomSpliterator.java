package com.pivovarit.stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toCollection;

public class RandomSpliterator<T> implements Spliterator<T> {

    private final Random random;
    private final T[] source;
    private int size;

    private RandomSpliterator(List<T> source) {
        this(source, Random::new);
    }

    private RandomSpliterator(List<T> source, Supplier<? extends Random> random) {
        if (source.isEmpty()) {
            throw new IllegalArgumentException("RandomSpliterator can't be initialized with an empty collection");
        }
        this.source = (T[]) source.toArray(Object[]::new);
        this.random = random.get();
        this.size = this.source.length;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        int next = random.nextInt(size);
        action.accept(source[next]);
        source[next] = source[size - 1];
        size = size - 1;
        return size > 0;
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

    public static <T> Collector<T, ?, Stream<T>> toLazyShuffledStream() {
        return Collectors.collectingAndThen(
          toCollection(ArrayList::new),
          list -> !list.isEmpty()
            ? StreamSupport.stream(new RandomSpliterator<>(list), false)
            : Stream.empty());
    }

    public static <T> Collector<T, ?, Stream<T>> toEagerShuffledStream() {
        return Collectors.collectingAndThen(
          toCollection(ArrayList::new),
          list -> {
              Collections.shuffle(list);
              return list.stream();
          });
    }
}
