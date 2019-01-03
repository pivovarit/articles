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

/*
       (limit)  (size)   Mode  Cnt     Score     Error  Units
eager        1  100000  thrpt    5   419.862 ±  65.420  ops/s
eager       10  100000  thrpt    5   467.011 ±  39.829  ops/s
eager      100  100000  thrpt    5   464.761 ±  38.811  ops/s
eager     1000  100000  thrpt    5   429.169 ±  45.307  ops/s
eager    10000  100000  thrpt    5   423.869 ±  73.094  ops/s
eager   100000  100000  thrpt    5   342.603 ±   9.571  ops/s
lazy         1  100000  thrpt    5  1572.274 ±  71.003  ops/s
lazy        10  100000  thrpt    5  1449.313 ±  48.895  ops/s
lazy       100  100000  thrpt    5   800.054 ± 144.649  ops/s
lazy      1000  100000  thrpt    5   172.125 ±  34.346  ops/s
lazy     10000  100000  thrpt    5    20.726 ±   2.487  ops/s
lazy    100000  100000  thrpt    5     3.909 ±   0.807  ops/s

       (limit)    (size)   Mode  Cnt  Score   Error  Units
eager        1  10000000  thrpt    2  0.915          ops/s
eager       10  10000000  thrpt    2  0.783          ops/s
eager      100  10000000  thrpt    2  0.965          ops/s
eager     1000  10000000  thrpt    2  0.936          ops/s
eager    10000  10000000  thrpt    2  0.860          ops/s
lazy         1  10000000  thrpt    2  4.338          ops/s
lazy        10  10000000  thrpt    2  3.149          ops/s
lazy       100  10000000  thrpt    2  2.060          ops/s
lazy      1000  10000000  thrpt    2  0.370          ops/s
lazy     10000  10000000  thrpt    2  0.054          ops/s

       (limit)    (size)   Mode  Cnt       Score   Error  Units
eager        2     128    thrpt    2  246439.459          ops/s
eager        4     128    thrpt    2  333866.936          ops/s
eager        8     128    thrpt    2  340296.188          ops/s
eager       16     128    thrpt    2  345533.673          ops/s
eager       32     128    thrpt    2  231725.156          ops/s
eager       64     128    thrpt    2  314324.265          ops/s
eager      128     128    thrpt    2  270451.992          ops/s
lazy         2     128    thrpt    2  765989.718          ops/s
lazy         4     128    thrpt    2  659421.041          ops/s
lazy         8     128    thrpt    2  652685.515          ops/s
lazy        16     128    thrpt    2  470346.570          ops/s
lazy        32     128    thrpt    2  324174.691          ops/s
lazy        64     128    thrpt    2  186472.090          ops/s
lazy       128     128    thrpt    2  108105.699          ops/s

               (limit)  (size)   Mode  Cnt     Score     Error  Units
eager                1  100000  thrpt    3   456.811 ±  20.585  ops/s
eager               10  100000  thrpt    3   469.635 ±  23.281  ops/s
eager              100  100000  thrpt    3   466.486 ±  68.820  ops/s
eager             1000  100000  thrpt    3   454.459 ±  13.103  ops/s
eager            10000  100000  thrpt    3   443.640 ±  96.929  ops/s
eager           100000  100000  thrpt    3   335.134 ±  21.944  ops/s
lazy                 1  100000  thrpt    3  1587.536 ± 389.128  ops/s
lazy                10  100000  thrpt    3  1452.855 ± 406.879  ops/s
lazy               100  100000  thrpt    3   814.978 ± 242.077  ops/s
lazy              1000  100000  thrpt    3   167.825 ± 129.559  ops/s
lazy             10000  100000  thrpt    3    19.782 ±   8.596  ops/s
lazy            100000  100000  thrpt    3     3.970 ±   0.408  ops/s
lazy_improved        1  100000  thrpt    3  1509.264 ± 170.423  ops/s
lazy_improved       10  100000  thrpt    3  1512.150 ± 143.927  ops/s
lazy_improved      100  100000  thrpt    3  1463.093 ± 593.370  ops/s
lazy_improved     1000  100000  thrpt    3  1451.007 ±  58.948  ops/s
lazy_improved    10000  100000  thrpt    3  1148.581 ± 232.218  ops/s
lazy_improved   100000  100000  thrpt    3   383.022 ±  97.082  ops/s
 */
@State(Scope.Benchmark)
public class RandomSpliteratorBenchmark {

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
          .collect(RandomCollectors.toEagerShuffledStream())
          .limit(limit)
          .collect(Collectors.toList());
    }

    @Benchmark
    public List<String> lazy() {
        return source.stream()
          .collect(RandomCollectors.toLazyShuffledStream())
          .limit(limit)
          .collect(Collectors.toList());
    }

    @Benchmark
    public List<String> lazy_improved() {
        return source.stream()
          .collect(RandomCollectors.toImprovedLazyShuffledStream())
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