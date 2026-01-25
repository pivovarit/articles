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

import org.openjdk.jmh.profile.AsyncProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.file.Path;

final class Benchmarks {

    private Benchmarks() {
    }

    private static final Path BENCHMARKS_PATH = Path.of("java-last-gatherer/src/test/resources/benchmarks/");

    static void run(Class<?> clazz) throws RunnerException {
        new Runner(new OptionsBuilder()
          .include(clazz.getSimpleName())
//          .jvmArgsAppend("-XX:+UnlockDiagnosticVMOptions","-XX:+DebugNonSafepoints", "-Xlog:class+load=info", "-XX:+LogCompilation", "-XX:+PrintAssembly")
          .warmupIterations(3)
          .measurementIterations(4)
          .resultFormat(ResultFormatType.JSON)
          .addProfiler(AsyncProfiler.class, "event=cpu;output=flamegraph;dir=" + Benchmarks.BENCHMARKS_PATH + ";libPath=" + "/opt/homebrew/opt/async-profiler/lib/libasyncProfiler.dylib")
          .forks(1)
          .build()).run();
    }
}
