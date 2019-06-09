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
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@State(Scope.Benchmark)
public class CompletionOrderSpliteratorIterationBench {

    private Spliterator<Integer> mapBased;
    private Spliterator<Integer> bqueueBased;
    private Spliterator<Integer> cqueueBased;
    private List<CompletableFuture<Integer>> futures;

    @Param({"1", "10", "100", "1000", "10000"})
    public int size;

    @Setup(Level.Trial)
    public void setUp() {
        futures = Stream.generate(() -> CompletableFuture.completedFuture(42))
          .limit(size)
          .collect(Collectors.toList());

        mapBased = new CompletionOrderSpliterator<>(futures);
        bqueueBased = new CompletionOrderSpliterator2<>(futures);
        cqueueBased = new CompletionOrderSpliterator3<>(futures);
    }

    @Benchmark
    public List<Integer> hashmp_based() {
        return StreamSupport.stream(mapBased, false)
          .collect(Collectors.toList());
    }

    @Benchmark
    public List<Integer> bqueue_based() {
        return StreamSupport.stream(bqueueBased, false)
          .collect(Collectors.toList());
    }

    @Benchmark
    public List<Integer> cqueue_based() {
        return StreamSupport.stream(cqueueBased, false)
          .collect(Collectors.toList());
    }

    public static void main(String[] args) throws RunnerException {
        var result = new Runner(
          new OptionsBuilder()
            .include(CompletionOrderSpliteratorIterationBench.class.getSimpleName())
            .warmupIterations(4)
            .measurementIterations(4)
            .forks(1)
            .build()).run();
    }
}
