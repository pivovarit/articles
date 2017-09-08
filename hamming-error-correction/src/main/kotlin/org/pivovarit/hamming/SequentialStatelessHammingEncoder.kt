package org.pivovarit.hamming

import org.pivovarit.hamming.message.BinaryString
import org.pivovarit.hamming.message.EncodedString

class SequentialStatelessHammingEncoder : HammingEncoder {
    companion object {

        internal fun codewordSize(msgLength: Int) = generateSequence(0) { it + 1 }
          .first { r -> msgLength + r + 1 <= (1 shl r) } + msgLength

        internal fun toHammingCodeValue(it: Int, input: BinaryString): String {
            return when ((it + 1).isPowerOfTwo()) {
                true -> calculateParityBit(it, input)
                false -> getDataBit(it, input)
            }
        }

        private fun calculateParityBit(codeWordIndex: Int, msg: BinaryString): String {
            return parityIndicesSequence(codeWordIndex, codewordSize(msg.value.length))
              .map { getDataBit(it, msg).toInt() }
              .reduce { a, b -> a xor b }
              .toString()
        }

        /**
         * Fetches corresponding data bit from source for given index in the codeword
         */
        internal fun getDataBit(ind: Int, input: BinaryString) = input
          .value[ind - Integer.toBinaryString(ind).length].toString()

        /**
         * Indices that require checking for a particular parity bit index in the codeword
         */
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