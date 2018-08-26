package com.pivovarit.grouping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomUnmodifiableListCollector<T> implements Collector<T, ArrayList<T>, List<T>> {

    private CustomUnmodifiableListCollector() {
    }

    public static <T> Collector<T, ?, List<T>> toUnmodifiableList() {
        return new CustomUnmodifiableListCollector<>();
    }

    @Override
    public Supplier<ArrayList<T>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<ArrayList<T>, T> accumulator() {
        return ArrayList::add;
    }

    @Override
    public BinaryOperator<ArrayList<T>> combiner() {
        return (ts, ts2) -> Stream.concat(ts.stream(), ts2.stream()).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Function<ArrayList<T>, List<T>> finisher() {
        return Collections::unmodifiableList;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of();
    }
}
