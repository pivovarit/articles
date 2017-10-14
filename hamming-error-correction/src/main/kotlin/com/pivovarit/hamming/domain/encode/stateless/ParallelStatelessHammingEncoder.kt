package com.pivovarit.hamming.domain.encode.stateless

import com.pivovarit.hamming.domain.BinaryString
import com.pivovarit.hamming.domain.EncodedString
import com.pivovarit.hamming.domain.encode.HammingEncoder
import com.pivovarit.hamming.domain.isPowerOfTwo

internal class ParallelStatelessHammingEncoder : HammingEncoder {

    private val bitIndexCalculator: BitIndexCalculator = BitIndexCalculator()

    override fun encode(input: BinaryString) = bitIndexCalculator.getHammingCodewordIndices(input.value.length)
      .toList().parallelStream()
      .map { toHammingCodeValue(it, input) }
      .reduce("") { t, u -> t + u }
      .let(::EncodedString)

    private fun toHammingCodeValue(it: Int, input: BinaryString) =
      when ((it + 1).isPowerOfTwo()) {
          true -> bitIndexCalculator.getParityBit(it, input)
          false -> bitIndexCalculator.getDataBit(it, input)
      }
}