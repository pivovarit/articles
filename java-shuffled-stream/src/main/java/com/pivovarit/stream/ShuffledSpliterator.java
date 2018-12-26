package com.pivovarit.stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toCollection;

public class ShuffledSpliterator<T> implements Spliterator<T> {

    private final Random random = new Random();

    private final List<T> source;

    private ShuffledSpliterator(List<T> source) {
        if (!(source instanceof RandomAccess)) {
            throw new IllegalArgumentException("passed list needs to provide random access!");
        }
        this.source = source;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        int remaining = source.size();
        if (remaining == 0) {
            return false;
        }
        int nextIndex = random.nextInt(remaining);
        action.accept(source.remove(nextIndex));
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

    public static <T> Collector<T, ?, Stream<T>> shuffledStream() {
        return Collectors.collectingAndThen(
          toCollection(ArrayList::new),
          list -> StreamSupport.stream(new ShuffledSpliterator<>(list), false));
    }

    public static <T> Collector<T, ?, Stream<T>> naiveShuffledStream() {
        return Collectors.collectingAndThen(
          toCollection(ArrayList::new),
          list -> {
              Collections.shuffle(list);
              return list.stream();
          });
    }
}
