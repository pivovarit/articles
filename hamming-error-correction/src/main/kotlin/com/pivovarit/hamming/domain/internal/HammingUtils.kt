package com.pivovarit.hamming.domain.internal

import com.pivovarit.hamming.domain.message.BinaryString
import com.pivovarit.hamming.domain.message.EncodedString

internal fun parityIndicesSequence(startIndex: Int, endExclusive: Int) = generateSequence(startIndex) { it + 1 }
  .take(endExclusive - startIndex)
  .filterIndexed { i, _ -> i % ((2 * (startIndex + 1))) < startIndex + 1 }
  .drop(1)

internal fun codewordSize(msgLength: Int) = generateSequence(2) { it + 1 }
  .first { r -> msgLength + r + 1 <= (1 shl r) } + msgLength

internal fun stripHammingMetadata(input: EncodedString): BinaryString {
    return input.value.asSequence()
      .filterIndexed { ind, _ -> (ind + 1).isPowerOfTwo().not() }
      .joinToString("")
      .let(::BinaryString)
}

internal fun Int.isPowerOfTwo() = this != 0 && this and this - 1 == 0