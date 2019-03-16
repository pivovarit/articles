package com.pivovarit.stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toCollection;

public final class RandomCollectors {

    private RandomCollectors() {
    }

    public static <T> Collector<T, ?, Stream<T>> toImprovedLazyShuffledStream() {
        return Collectors.collectingAndThen(
            toCollection(ArrayList::new),
            list -> StreamSupport.stream(new ImprovedRandomSpliterator<>(list, Random::new), false));
    }

    public static <T> Collector<T, ?, Stream<T>> toLazyShuffledStream() {
        return Collectors.collectingAndThen(
            toCollection(ArrayList::new),
            list -> StreamSupport.stream(new RandomSpliterator<>(list, Random::new), false));
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
