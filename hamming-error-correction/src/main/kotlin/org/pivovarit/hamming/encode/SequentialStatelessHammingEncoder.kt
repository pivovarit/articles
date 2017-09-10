package org.pivovarit.hamming.encode

import org.pivovarit.hamming.message.BinaryString
import org.pivovarit.hamming.message.EncodedString

class SequentialStatelessHammingEncoder : HammingEncoder {
    companion object {

        internal fun codewordSize(msgLength: Int) = generateSequence(2) { it + 1 }
          .first { r -> msgLength + r + 1 <= (1 shl r) } + msgLength

        internal fun toHammingCodeValue(it: Int, input: BinaryString) =
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

        internal fun parityIndicesSequence(startIndex: Int, endExclusive: Int) = generateSequence(startIndex) { it + 1 }
          .take(endExclusive - startIndex)
          .filterIndexed { i, _ -> i % ((2 * (startIndex + 1))) < startIndex + 1 }
          .drop(1)
    }

    override fun encode(input: BinaryString) = generateSequence(0) { it + 1 }
      .take(codewordSize(input.value.length))
      .map { toHammingCodeValue(it, input) }
      .joinToString("")
      .let { EncodedString(it) }
}

private fun Int.isPowerOfTwo() = this != 0 && this and this - 1 == 0