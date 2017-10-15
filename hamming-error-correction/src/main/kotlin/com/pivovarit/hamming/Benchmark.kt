package com.pivovarit.hamming

import com.pivovarit.hamming.domain.BinaryString
import com.pivovarit.hamming.domain.encode.HammingEncoder
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import java.util.Random
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
open class Benchmark {

    companion object {
        private val sequential = HammingEncoder.sequentialStateless()
        private val parallel = HammingEncoder.parallelStateless()
    }

    @Param("10", "100", "1000", "10000")
    private var messageSize = 0

    private lateinit var msg: BinaryString

    @Setup(Level.Iteration)
    fun setup() {
        msg = Random().let {
            generateSequence { it.nextInt().coerceIn(0, 1).toString() }
              .take(messageSize)
              .reduce { acc, s -> acc + s }
              .let(::BinaryString)
        }
    }

    @Benchmark
    fun sequential() = sequential.encode(msg)

    @Benchmark
    fun parallel() = parallel.encode(msg)
}