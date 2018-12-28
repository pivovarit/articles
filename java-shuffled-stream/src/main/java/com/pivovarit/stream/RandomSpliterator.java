package com.pivovarit.stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.RandomAccess;
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
    private final List<T> source;

    private RandomSpliterator(List<T> source) {
        this(source, Random::new);
    }

    private RandomSpliterator(List<T> source, Supplier<? extends Random> random) {
        this.source = source instanceof RandomAccess ? source : new ArrayList<>(source);
        this.random = random.get();
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        int remaining = source.size();

        if (remaining == 0) {
            return false;
        }

        action.accept(source.remove(random.nextInt(remaining)));
        return remaining > 1;
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

    public static <T> Collector<T, ?, Stream<T>> lazyShuffledStream() {
        return Collectors.collectingAndThen(
          toCollection(ArrayList::new),
          list -> StreamSupport.stream(new RandomSpliterator<>(list), false));
    }

    public static <T> Collector<T, ?, Stream<T>> eagerShuffledStream() {
        return Collectors.collectingAndThen(
          toCollection(ArrayList::new),
          list -> {
              Collections.shuffle(list);
              return list.stream();
          });
    }
}
