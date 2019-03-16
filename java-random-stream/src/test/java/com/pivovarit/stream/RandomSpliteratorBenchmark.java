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

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

/*
Benchmark      (limit)  (size)   Mode  Cnt     Score     Error  Units
eager                1  100000  thrpt    5   449.943 ±  16.706  ops/s
eager               10  100000  thrpt    5   447.505 ±  29.861  ops/s
eager              100  100000  thrpt    5   449.688 ±  13.782  ops/s
eager             1000  100000  thrpt    5   443.310 ±  10.854  ops/s
eager            10000  100000  thrpt    5   430.474 ±  24.297  ops/s
eager           100000  100000  thrpt    5   328.347 ±   3.828  ops/s
lazy                 1  100000  thrpt    5  1589.508 ±  17.446  ops/s
lazy                10  100000  thrpt    5  1424.915 ±  34.953  ops/s
lazy               100  100000  thrpt    5   890.914 ±   4.042  ops/s
lazy              1000  100000  thrpt    5   185.651 ±   9.161  ops/s
lazy             10000  100000  thrpt    5    22.373 ±   0.927  ops/s
lazy            100000  100000  thrpt    5     4.567 ±   0.226  ops/s
lazy_improved        1  100000  thrpt    5  1512.144 ±  90.778  ops/s
lazy_improved       10  100000  thrpt    5  1470.838 ±  46.537  ops/s
lazy_improved      100  100000  thrpt    5  1621.704 ± 125.341  ops/s
lazy_improved     1000  100000  thrpt    5  1486.026 ±  31.225  ops/s
lazy_improved    10000  100000  thrpt    5  1123.391 ±  69.165  ops/s
lazy_improved   100000  100000  thrpt    5   383.457 ±  98.229  ops/s

Benchmark                                 (limit)  (size)   Mode  Cnt     Score     Error  Units
RandomSpliteratorBenchmark.eager                1  100000  thrpt    5   432.621 ±   9.114  ops/s
RandomSpliteratorBenchmark.eager               10  100000  thrpt    5   490.174 ±   9.263  ops/s
RandomSpliteratorBenchmark.eager              100  100000  thrpt    5   444.178 ±  11.865  ops/s
RandomSpliteratorBenchmark.eager             1000  100000  thrpt    5   437.328 ±  16.875  ops/s
RandomSpliteratorBenchmark.eager            10000  100000  thrpt    5   418.864 ±   4.895  ops/s
RandomSpliteratorBenchmark.eager           100000  100000  thrpt    5   288.879 ±   1.780  ops/s
RandomSpliteratorBenchmark.lazy                 1  100000  thrpt    5  1463.682 ±  26.422  ops/s
RandomSpliteratorBenchmark.lazy                10  100000  thrpt    5  1364.556 ± 114.212  ops/s
RandomSpliteratorBenchmark.lazy               100  100000  thrpt    5   856.734 ±  37.250  ops/s
RandomSpliteratorBenchmark.lazy              1000  100000  thrpt    5   184.142 ±   3.037  ops/s
RandomSpliteratorBenchmark.lazy             10000  100000  thrpt    5    22.101 ±   0.547  ops/s
RandomSpliteratorBenchmark.lazy            100000  100000  thrpt    5     4.533 ±   0.250  ops/s
RandomSpliteratorBenchmark.lazy_improved        1  100000  thrpt    5  2285.904 ±  88.239  ops/s
RandomSpliteratorBenchmark.lazy_improved       10  100000  thrpt    5  1366.590 ± 524.230  ops/s
RandomSpliteratorBenchmark.lazy_improved      100  100000  thrpt    5  2241.723 ±  88.484  ops/s
RandomSpliteratorBenchmark.lazy_improved     1000  100000  thrpt    5  2143.909 ±  66.272  ops/s
RandomSpliteratorBenchmark.lazy_improved    10000  100000  thrpt    5  1034.969 ±  38.865  ops/s
RandomSpliteratorBenchmark.lazy_improved   100000  100000  thrpt    5   307.509 ± 168.192  ops/s
 */
@State(Scope.Benchmark)
public class RandomSpliteratorBenchmark {

    private List<Integer> source;

    @Param({"1", "10", "100", "1000", "10000", "100000"})
    public int limit;

    @Param({"100000"})
    public int size;

    @Setup(Level.Iteration)
    public void setUp() {
        source = IntStream.range(0, size)
          .boxed().collect(Collectors.toList());
    }

    @Benchmark
    public List<Integer> eager() {
        return source.stream()
          .collect(RandomCollectors.toEagerShuffledStream())
          .limit(limit)
          .collect(Collectors.toCollection(LinkedList::new));
    }

    @Benchmark
    public List<Integer> lazy() {
        return source.stream()
          .collect(RandomCollectors.toLazyShuffledStream())
          .limit(limit)
          .collect(Collectors.toCollection(LinkedList::new));
    }

    @Benchmark
    public List<Integer> lazy_improved() {
        return source.stream()
          .collect(RandomCollectors.toImprovedLazyShuffledStream())
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