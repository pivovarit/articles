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


/*
RandomSpliteratorBenchmark.eager        1      10  thrpt    3  3088788.851 ± 776219.338  ops/s
RandomSpliteratorBenchmark.eager        1     100  thrpt    3   457729.043 ±  62472.238  ops/s
RandomSpliteratorBenchmark.eager        1    1000  thrpt    3    56154.793 ±   1794.738  ops/s
RandomSpliteratorBenchmark.eager        1   10000  thrpt    3     5100.502 ±    358.469  ops/s
RandomSpliteratorBenchmark.eager        1  100000  thrpt    3      479.814 ±     38.217  ops/s
RandomSpliteratorBenchmark.lazy         1      10  thrpt    3  3443751.580 ± 735571.026  ops/s
RandomSpliteratorBenchmark.lazy         1     100  thrpt    3  1110333.616 ± 903763.469  ops/s
RandomSpliteratorBenchmark.lazy         1    1000  thrpt    3   219199.808 ±  29025.663  ops/s
RandomSpliteratorBenchmark.lazy         1   10000  thrpt    3    15148.461 ±   8724.768  ops/s
RandomSpliteratorBenchmark.lazy         1  100000  thrpt    3     1647.194 ±    354.923  ops/s
 */
/*
RandomSpliteratorBenchmark.eager       10      10  thrpt    3  2338792.984 ± 300547.939  ops/s
RandomSpliteratorBenchmark.eager       10     100  thrpt    3   346181.596 ±  38852.295  ops/s
RandomSpliteratorBenchmark.eager       10    1000  thrpt    3    56338.044 ±   3147.049  ops/s
RandomSpliteratorBenchmark.eager       10   10000  thrpt    3     5095.897 ±   1049.684  ops/s
RandomSpliteratorBenchmark.eager       10  100000  thrpt    3      476.380 ±    191.963  ops/s
RandomSpliteratorBenchmark.lazy        10      10  thrpt    3  1641058.303 ± 107292.827  ops/s
RandomSpliteratorBenchmark.lazy        10     100  thrpt    3   708515.643 ± 176020.828  ops/s
RandomSpliteratorBenchmark.lazy        10    1000  thrpt    3   178260.983 ±  60104.515  ops/s
RandomSpliteratorBenchmark.lazy        10   10000  thrpt    3    14318.181 ±   1189.146  ops/s
RandomSpliteratorBenchmark.lazy        10  100000  thrpt    3     1391.557 ±    599.166  ops/s
 */
/*
Benchmark                         (limit)  (size)   Mode  Cnt       Score       Error  Units
RandomSpliteratorBenchmark.eager      100     100  thrpt    3  337366.984 ± 84329.753  ops/s
RandomSpliteratorBenchmark.eager      100    1000  thrpt    3   49547.207 ± 10247.023  ops/s
RandomSpliteratorBenchmark.eager      100   10000  thrpt    3    5107.400 ±   831.823  ops/s
RandomSpliteratorBenchmark.eager      100  100000  thrpt    3     480.338 ±    28.043  ops/s
RandomSpliteratorBenchmark.lazy       100     100  thrpt    3  153716.794 ± 18555.820  ops/s
RandomSpliteratorBenchmark.lazy       100    1000  thrpt    3   59311.037 ± 41748.589  ops/s
RandomSpliteratorBenchmark.lazy       100   10000  thrpt    3    8178.317 ±  8271.298  ops/s
RandomSpliteratorBenchmark.lazy       100  100000  thrpt    3     780.395 ±   544.583  ops/s
 */
/*
Benchmark                         (limit)  (size)   Mode  Cnt      Score      Error  Units
RandomSpliteratorBenchmark.eager     1000    1000  thrpt    3  28637.221 ± 1832.696  ops/s
RandomSpliteratorBenchmark.eager     1000   10000  thrpt    3   4900.768 ±  233.069  ops/s
RandomSpliteratorBenchmark.eager     1000  100000  thrpt    3    474.310 ±   40.493  ops/s
RandomSpliteratorBenchmark.lazy      1000    1000  thrpt    3  11450.824 ± 1862.817  ops/s
RandomSpliteratorBenchmark.lazy      1000   10000  thrpt    3   1978.120 ± 1816.602  ops/s
RandomSpliteratorBenchmark.lazy      1000  100000  thrpt    3    166.114 ±  102.544  ops/s
 */
/*
Benchmark                         (limit)  (size)   Mode  Cnt        Score   Error  Units
RandomSpliteratorBenchmark.eager        1     100  thrpt        339875.092          ops/s
RandomSpliteratorBenchmark.eager       10     100  thrpt        333300.019          ops/s
RandomSpliteratorBenchmark.eager       20     100  thrpt        332877.284          ops/s
RandomSpliteratorBenchmark.eager       30     100  thrpt        411932.699          ops/s
RandomSpliteratorBenchmark.eager       40     100  thrpt        318663.424          ops/s
RandomSpliteratorBenchmark.eager       50     100  thrpt        400886.002          ops/s
RandomSpliteratorBenchmark.eager       60     100  thrpt        388944.643          ops/s
RandomSpliteratorBenchmark.eager       70     100  thrpt        377956.106          ops/s
RandomSpliteratorBenchmark.eager       80     100  thrpt        366760.001          ops/s
RandomSpliteratorBenchmark.eager       90     100  thrpt        279271.749          ops/s
RandomSpliteratorBenchmark.eager      100     100  thrpt        332118.388          ops/s
RandomSpliteratorBenchmark.lazy         1     100  thrpt       1058189.932          ops/s
RandomSpliteratorBenchmark.lazy        10     100  thrpt        690327.305          ops/s
RandomSpliteratorBenchmark.lazy        20     100  thrpt        479716.626          ops/s
RandomSpliteratorBenchmark.lazy        30     100  thrpt        374586.487          ops/s
RandomSpliteratorBenchmark.lazy        40     100  thrpt        302133.246          ops/s
RandomSpliteratorBenchmark.lazy        50     100  thrpt        248947.257          ops/s
RandomSpliteratorBenchmark.lazy        60     100  thrpt        230393.997          ops/s
RandomSpliteratorBenchmark.lazy        70     100  thrpt        193400.913          ops/s
RandomSpliteratorBenchmark.lazy        80     100  thrpt        183020.787          ops/s
RandomSpliteratorBenchmark.lazy        90     100  thrpt        158960.987          ops/s
RandomSpliteratorBenchmark.lazy       100     100  thrpt        153751.373          ops/s
 */
/*
Benchmark                         (limit)  (size)   Mode  Cnt      Score   Error  Units
RandomSpliteratorBenchmark.eager      100    1000  thrpt       38157.139          ops/s
RandomSpliteratorBenchmark.eager      200    1000  thrpt       47181.621          ops/s
RandomSpliteratorBenchmark.eager      400    1000  thrpt       35021.957          ops/s
RandomSpliteratorBenchmark.eager      600    1000  thrpt       41656.374          ops/s
RandomSpliteratorBenchmark.eager      800    1000  thrpt       34852.124          ops/s
RandomSpliteratorBenchmark.eager     1000    1000  thrpt       28942.122          ops/s
RandomSpliteratorBenchmark.lazy       100    1000  thrpt       57753.113          ops/s
RandomSpliteratorBenchmark.lazy       200    1000  thrpt       39394.122          ops/s
RandomSpliteratorBenchmark.lazy       400    1000  thrpt       24236.017          ops/s
RandomSpliteratorBenchmark.lazy       600    1000  thrpt       18157.620          ops/s
RandomSpliteratorBenchmark.lazy       800    1000  thrpt       13806.402          ops/s
RandomSpliteratorBenchmark.lazy      1000    1000  thrpt       11597.400          ops/s
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

    // @Benchmark
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
            .warmupIterations(0)
            .measurementIterations(1)
            .forks(1)
            .build()).run();
    }
}