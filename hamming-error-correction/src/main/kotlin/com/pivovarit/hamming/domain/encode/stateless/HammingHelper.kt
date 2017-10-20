package com.pivovarit.hamming.domain.encode.stateless

import com.pivovarit.hamming.domain.BinaryString

internal class HammingHelper {

    fun codewordSize(msgLength: Int) = generateSequence(2, Int::inc)
      .first { r -> msgLength + r + 1 <= (1 shl r) } + msgLength

    fun getHammingCodewordIndices(msgLength: Int) = generateSequence(0, Int::inc)
      .take(codewordSize(msgLength))

    fun getParityBit(codeWordIndex: Int, msg: BinaryString) =
      parityIndicesSequence(codeWordIndex, codewordSize(msg.length))
        .map { getDataBit(it, msg).toInt() }
        .reduce { a, b -> a xor b }
        .toString()

    fun getDataBit(ind: Int, input: BinaryString) =
      input[ind - Integer.toBinaryString(ind).length].toString()

    fun parityIndicesSequence(startIndex: Int, endExclusive: Int) = generateSequence(startIndex, Int::inc)
      .take(endExclusive - startIndex)
      .filterIndexed { i, _ -> i % ((2 * (startIndex + 1))) < startIndex + 1 }
      .drop(1)
}