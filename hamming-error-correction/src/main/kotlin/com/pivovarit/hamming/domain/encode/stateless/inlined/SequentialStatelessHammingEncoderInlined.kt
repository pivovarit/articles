package com.pivovarit.hamming.domain.encode.stateless.inlined

import com.pivovarit.hamming.domain.BinaryString
import com.pivovarit.hamming.domain.EncodedString
import com.pivovarit.hamming.domain.encode.HammingEncoder
import com.pivovarit.hamming.domain.isPowerOfTwo

internal class SequentialStatelessHammingEncoderInlined : HammingEncoder {

    override fun encode(input: BinaryString) = generateSequence(0) { it + 1 }
      .take(generateSequence(2) { it + 1 }
        .first { r -> input.value.length + r + 1 <= (1 shl r) } + input.value.length)
      .map {
          when ((it + 1).isPowerOfTwo()) {
              true -> generateSequence(it) { it + 1 }
                .take(generateSequence(2) { it + 1 }
                  .first { r -> input.value.length + r + 1 <= (1 shl r) } + input.value.length - it)
                .filterIndexed { i, _ -> i % ((2 * (it + 1))) < it + 1 }
                .drop(1)
                .map {
                    input
                      .value[it - Integer.toBinaryString(it).length].toString().toInt()
                }
                .reduce { a, b -> a xor b }
                .toString()
              false -> input
                .value[it - Integer.toBinaryString(it).length].toString()
          }
      }
      .joinToString("")
      .let(::EncodedString)
}