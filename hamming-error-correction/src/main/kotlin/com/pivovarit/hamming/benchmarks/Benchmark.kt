package com.pivovarit.hamming.benchmarks

import com.pivovarit.hamming.domain.BinaryString
import com.pivovarit.hamming.domain.encode.HammingEncoder
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import java.util.concurrent.TimeUnit

internal const val SIZE: Int = 10000

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
open class SimpleBenchmark {

    companion object {
        private val msg = generateSequence { "0" }
          .take(SIZE)
          .reduce { acc, s -> acc + s }
          .let(::BinaryString)

        private val sequential = HammingEncoder.sequentialStateless()
        private val parallel = HammingEncoder.parallelStateless()
    }

    @Benchmark fun sequential() = sequential.encode(msg)

    @Benchmark fun parallel() = parallel.encode(msg)
}