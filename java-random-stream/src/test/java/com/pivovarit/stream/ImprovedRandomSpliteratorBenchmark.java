package com.pivovarit.stream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@State(Scope.Benchmark)
public class ImprovedRandomSpliteratorBenchmark {

    private List<String> source;

    @Param({"1", "10", "100", "1000", "10000", "100000"})
    public int limit;

    @Param({"100000"})
    public int size;


    @Setup(Level.Iteration)
    public void setUp() {
        source = IntStream.range(0, size)
          .boxed()
          .map(Object::toString)
          .collect(Collectors.toList());
    }

    @Benchmark
    public List<String> eager() {
        return source.stream()
          .collect(ImprovedRandomSpliterator.toEagerShuffledStream())
          .limit(limit)
          .collect(Collectors.toList());
    }

    @Benchmark
    public List<String> lazy() {
        return source.stream()
          .collect(ImprovedRandomSpliterator.toLazyShuffledStream())
          .limit(limit)
          .collect(Collectors.toList());
    }

    public static void main(String[] args) throws RunnerException {
        var result = new Runner(
          new OptionsBuilder()
            .include(ImprovedRandomSpliteratorBenchmark.class.getSimpleName())
            .warmupIterations(3)
            .measurementIterations(2)
            .forks(1)
            .build()).run();
    }
}