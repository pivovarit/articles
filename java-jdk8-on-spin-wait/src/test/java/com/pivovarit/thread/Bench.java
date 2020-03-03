package com.pivovarit.thread;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

import static java.lang.invoke.MethodType.methodType;

@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class Bench {

    @Benchmark
    public void baseline_empty() {
    }

    @Benchmark
    public void baseline_plain() {
        Thread.onSpinWait();
    }

    @Benchmark
    public void baseline_mh_resolved() {
        Internal.onSpinWaitOrNothing();
    }

    @Benchmark
    public void baseline_mh_unresolved() {
        Internal.onSpinWaitOrNothingUnresolved();
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(
          new OptionsBuilder()
            .include(Bench.class.getSimpleName())
            .resultFormat(ResultFormatType.JSON)
            .result(System.currentTimeMillis() + ".json")
            .build()).run();
    }

    static class Internal {
        private static final MethodHandle ON_SPIN_WAIT_HANDLE = resolve();
        private static final MethodHandle ON_SPIN_WAIT_HANDLE_UNRESOLVED = null;

        private Internal() {
        }

        private static MethodHandle resolve() {
            try {
                return MethodHandles.lookup().findStatic(Thread.class, "onSpinWait", methodType(void.class));
            } catch (Exception ignore) {
            }

            return null;
        }

        static boolean onSpinWaitOrNothing() {
            if (ON_SPIN_WAIT_HANDLE != null) {
                try {
                    ON_SPIN_WAIT_HANDLE.invokeExact();
                    return true;
                } catch (Throwable ignore) {
                }
            }
            return false;
        }

        static boolean onSpinWaitOrNothingUnresolved() {
            if (ON_SPIN_WAIT_HANDLE_UNRESOLVED != null) {
                try {
                    ON_SPIN_WAIT_HANDLE_UNRESOLVED.invokeExact();
                    return true;
                } catch (Throwable ignore) {
                }
            }
            return false;
        }
    }
}

