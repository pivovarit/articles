package com.pivovarit.parallel;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class BatchingBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState {

        @Param({"1", "10", "100", "1000"})
        public int threads;

        private volatile ExecutorService executor;

        @Setup(Level.Trial)
        public void setup() {
            executor = new ThreadPoolExecutor(threads, threads,
              0L, TimeUnit.MILLISECONDS,
              new LinkedBlockingQueue<>());
        }

        @TearDown(Level.Trial)
        public void tearDown() {
            executor.shutdown();
        }
    }

    private static final List<Integer> source = IntStream.range(0, 1000)
      .boxed()
      .collect(toList());

    @Benchmark
    public List<Integer> no_batching(BenchmarkState state) {
        return ParallelStreams.inParallel(source, i -> i, state.executor).join();
    }

    @Benchmark
    public List<Integer> with_batching(BenchmarkState state) {
        return ParallelStreams.inParallelBatching(source, i -> i, state.executor, state.threads).join();
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(
          new OptionsBuilder()
            .include(BatchingBenchmark.class.getSimpleName())
            .mode(Mode.AverageTime)
            .timeUnit(TimeUnit.MILLISECONDS)
            .resultFormat(ResultFormatType.JSON)
            .result(System.currentTimeMillis() + ".json")
            .warmupIterations(4)
            .measurementIterations(5)
            .forks(1)

            .build()).run();
    }
}

