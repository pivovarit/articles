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

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/*
           (limit)  (size)   Mode  Cnt     Score     Error  Units
eager            1  100000  thrpt    5   454.396 ±  11.738  ops/s
eager           10  100000  thrpt    5   441.602 ±  40.503  ops/s
eager          100  100000  thrpt    5   456.167 ±  11.420  ops/s
eager         1000  100000  thrpt    5   443.149 ±   7.590  ops/s
eager        10000  100000  thrpt    5   431.375 ±  12.116  ops/s
eager       100000  100000  thrpt    5   328.376 ±   4.156  ops/s
lazy             1  100000  thrpt    5  1419.514 ±  58.778  ops/s
lazy            10  100000  thrpt    5  1336.452 ±  34.525  ops/s
lazy           100  100000  thrpt    5   926.438 ±  65.923  ops/s
lazy          1000  100000  thrpt    5   165.967 ±  17.135  ops/s
lazy         10000  100000  thrpt    5    19.673 ±   0.375  ops/s
lazy        100000  100000  thrpt    5     4.002 ±   0.305  ops/s
optimized        1  100000  thrpt    5  1478.069 ±  32.923  ops/s
optimized       10  100000  thrpt    5  1477.618 ±  72.917  ops/s
optimized      100  100000  thrpt    5  1448.584 ±  42.205  ops/s
optimized     1000  100000  thrpt    5  1435.818 ±  38.505  ops/s
optimized    10000  100000  thrpt    5   1060.88 ±  15.238  ops/s
optimized   100000  100000  thrpt    5   332.096 ±   7.071  ops/s
 */
@State(Scope.Benchmark)
public class RandomSpliteratorBenchmark {

    private static final Collector<Integer, ?, Stream<Integer>> OPTIMIZED = RandomCollectors.toOptimizedLazyShuffledStream();
    private static final Collector<Integer, ?, Stream<Integer>> LAZY = RandomCollectors.toLazyShuffledStream();
    private static final Collector<Integer, ?, Stream<Integer>> EAGER = RandomCollectors.toEagerShuffledStream();

    private List<Integer> source;

    @Param({/*"1", */ "1" /*"100", "1000", "10000", "100000"*/})
    public int limit;

    @Param({"100000"})
    public int size;

    @Setup(Level.Trial)
    public void setUp() {
        source = IntStream.range(0, size)
          .boxed().collect(Collectors.toList());
    }

//    @Benchmark
    public List<Integer> eager() {
        return collectWith(EAGER, source, limit);
    }

    @Benchmark
    public List<Integer> lazy() {
        return collectWith(LAZY, source, limit);
    }

    @Benchmark
    public List<Integer> optimized() {
        return collectWith(OPTIMIZED, source, limit);
    }

    private static List<Integer> collectWith(Collector<Integer, ?, Stream<Integer>> integerStreamCollector, List<Integer> source, long limit) {
        return source.stream()
          .collect(integerStreamCollector)
          .limit(limit)
          .collect(Collectors.toCollection(LinkedList::new));
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