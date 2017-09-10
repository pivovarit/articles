package com.pivovarit.hamming.domain.encode

import com.pivovarit.hamming.domain.message.BinaryString
import com.pivovarit.hamming.domain.message.EncodedString
import com.pivovarit.hamming.domain.internal.codewordSize
import com.pivovarit.hamming.domain.internal.isPowerOfTwo
import com.pivovarit.hamming.domain.internal.parityIndicesSequence

internal class SequentialStatelessHammingEncoder : HammingEncoder {
    companion object {
        private fun toHammingCodeValue(it: Int, input: BinaryString) =
          when ((it + 1).isPowerOfTwo()) {
              true -> getParityBit(it, input)
              false -> getDataBit(it, input)
          }

        private fun getParityBit(codeWordIndex: Int, msg: BinaryString) =
          parityIndicesSequence(codeWordIndex, codewordSize(msg.value.length))
            .map { getDataBit(it, msg).toInt() }
            .reduce { a, b -> a xor b }
            .toString()

        internal fun getDataBit(ind: Int, input: BinaryString) = input
          .value[ind - Integer.toBinaryString(ind).length].toString()
    }

    override fun encode(input: BinaryString) = generateSequence(0) { it + 1 }
      .take(codewordSize(input.value.length))
      .map { toHammingCodeValue(it, input) }
      .joinToString("")
      .let { EncodedString(it) }
}
