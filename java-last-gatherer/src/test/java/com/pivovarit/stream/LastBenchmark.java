/*
 * Copyright 2014-2026 Grzegorz Piwowarek, https://4comprehension.com/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pivovarit.stream;

import com.ginsberg.gatherers4j.Gatherers4j;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

import java.util.stream.Stream;

@State(Scope.Thread)
public class LastBenchmark {

    @Param({"1", "10", "100", "1000"})
    int n;

    @Param({"1000", "10000", "100000", "1000000", "10000000"})
    int size;

    Integer[] data;

    @Setup(Level.Trial)
    public void setup() {
        data = new Integer[size];
        for (int i = 0; i < size; i++) {
            data[i] = i;
        }
    }

    @Benchmark
    public void baseline(Blackhole bh) {
        Integer last = null;
        for (int i = 0; i < data.length; i++) {
            last = data[i];
        }
        bh.consume(last);
    }

    @Benchmark
    public void take_1(Blackhole bh) {
        Stream.of(data)
          .gather(new LastGathererTake1<>(n))
          .forEach(bh::consume);
    }

    @Benchmark
    public void take_2(Blackhole bh) {
        Stream.of(data)
          .gather(new LastGathererTake2<>(n))
          .forEach(bh::consume);
    }

    @Benchmark
    public void take_3(Blackhole bh) {
        Stream.of(data)
          .gather(new LastGathererTake3<>(n))
          .forEach(bh::consume);
    }

    @Benchmark
    public void take_4(Blackhole bh) {
        Stream.of(data)
          .gather(new LastGathererTake4<>(n))
          .forEach(bh::consume);
    }

    @Benchmark
    public void take_5(Blackhole bh) {
        Stream.of(data)
          .gather(new LastGathererTake5<>(n))
          .forEach(bh::consume);
    }

    @Benchmark
    public void take_6(Blackhole bh) {
        Stream.of(data)
          .gather(MoreGatherers.last(n))
          .forEach(bh::consume);
    }

    @Benchmark
    public void gatherers4j(Blackhole bh) {
        Stream.of(data)
          .gather(Gatherers4j.takeLast(n))
          .forEach(bh::consume);
    }

    public static void main(String[] ignored) throws RunnerException {
        Benchmarks.run(LastBenchmark.class);
    }
}
