package com.pivovarit.hamming.domain.encode

import com.pivovarit.hamming.domain.isPowerOfTwo
import com.pivovarit.hamming.domain.BinaryString
import com.pivovarit.hamming.domain.EncodedString

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
