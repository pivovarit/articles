package com.pivovarit.stream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/*
                (limit)   Mode  Cnt     Score     Error  Units
    eager             1  thrpt    5   467.796 ±   9.074  ops/s
    eager            10  thrpt    5   467.694 ±  17.166  ops/s
    eager           100  thrpt    5   459.765 ±   8.048  ops/s
    eager          1000  thrpt    5   467.934 ±  43.095  ops/s
    eager         10000  thrpt    5   449.471 ±   5.549  ops/s
    eager        100000  thrpt    5   331.111 ±   5.626  ops/s
    lazy              1  thrpt    5  1530.763 ±  72.096  ops/s
    lazy             10  thrpt    5  1462.305 ±  23.860  ops/s
    lazy            100  thrpt    5   823.212 ± 119.771  ops/s
    lazy           1000  thrpt    5   166.786 ±  16.306  ops/s
    lazy          10000  thrpt    5    19.475 ±   4.052  ops/s
    lazy         100000  thrpt    5     4.097 ±   0.416  ops/s
 */
@State(Scope.Benchmark)
public class RandomSpliteratorBenchmark {

    @Param({"1", "10", "100", "1000", "10000", "100000"})
    public int limit;

    private final List<String> source = IntStream.range(0, 100_000)
      .boxed()
      .map(Object::toString)
      .collect(Collectors.toList());

    @Benchmark
    public List<String> baseline() {
        return source.stream()
          .limit(limit)
          .collect(Collectors.toList());
    }

    @Benchmark
    public List<String> eager() {
        return source.stream()
          .collect(RandomSpliterator.eagerShuffledStream())
          .limit(limit)
          .collect(Collectors.toList());
    }

    @Benchmark
    public List<String> lazy() {
        return source.stream()
          .collect(RandomSpliterator.lazyShuffledStream())
          .limit(limit)
          .collect(Collectors.toList());
    }

    public static void main(String[] args) throws RunnerException {
        var result = new Runner(
          new OptionsBuilder()
            .include(RandomSpliteratorBenchmark.class.getSimpleName())
            .warmupIterations(5)
            .measurementIterations(5)
            .forks(1)
            .build()).run();
    }
}