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
public class CompletionOrderSpliteratorCreationAndIterationBench {

    private List<CompletableFuture<Integer>> futures;

    @Param({"1", "10", "100", "1000", "10000"})
    public int size;

    @Setup(Level.Trial)
    public void setUp() {
        futures = Stream.generate(() -> CompletableFuture.completedFuture(42))
          .limit(size)
          .collect(Collectors.toList());
    }

    @Benchmark
    public List<Integer> hashmp_based() {
        return StreamSupport.stream(new CompletionOrderSpliterator<>(futures), false)
          .collect(Collectors.toList());
    }

    @Benchmark
    public List<Integer> bqueue_based() {
        return StreamSupport.stream(new CompletionOrderSpliterator2<>(futures), false)
          .collect(Collectors.toList());
    }

    @Benchmark
    public List<Integer> cqueue_based() {
        return StreamSupport.stream(new CompletionOrderSpliterator3<>(futures), false)
          .collect(Collectors.toList());
    }

    public static void main(String[] args) throws RunnerException {
        var result = new Runner(
          new OptionsBuilder()
            .include(CompletionOrderSpliteratorCreationAndIterationBench.class.getSimpleName())
            .warmupIterations(4)
            .measurementIterations(4)
            .forks(1)
            .build()).run();
    }
}
