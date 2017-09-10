package com.pivovarit.hamming.domain.encode.stateless

import com.pivovarit.hamming.domain.isPowerOfTwo
import com.pivovarit.hamming.domain.BinaryString
import com.pivovarit.hamming.domain.EncodedString
import com.pivovarit.hamming.domain.encode.HammingEncoder

internal class SequentialStatelessHammingEncoder : HammingEncoder {

    private val bitIndexCalculator = StatelessBitIndexCalculator()

    override fun encode(input: BinaryString) = bitIndexCalculator.getHammingCodewordIndices(input.value.length)
      .map { toHammingCodeValue(it, input) }
      .joinToString("")
      .let { EncodedString(it) }

    private fun toHammingCodeValue(it: Int, input: BinaryString) =
      when ((it + 1).isPowerOfTwo()) {
          true -> bitIndexCalculator.getParityBit(it, input)
          false -> bitIndexCalculator.getDataBit(it, input)
      }
}
