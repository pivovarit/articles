package com.pivovarit.hamming.domain.decode.stateless

import com.pivovarit.hamming.domain.BinaryString
import com.pivovarit.hamming.domain.EncodedString
import com.pivovarit.hamming.domain.decode.HammingDecoder
import com.pivovarit.hamming.domain.decode.HammingMessageExtractor
import com.pivovarit.hamming.domain.encode.stateless.HammingHelper

internal class SequentialStatelessDecoder : HammingDecoder {
    private val helper = HammingHelper()
    private val extractor = HammingMessageExtractor()

    override fun isValid(codeWord: EncodedString) =
      indexesOfInvalidParityBits(codeWord).isEmpty()

    override fun decode(codeWord: EncodedString): BinaryString =
      indexesOfInvalidParityBits(codeWord).let { result ->
          when (result.isEmpty()) {
              true -> codeWord
              false -> codeWord.withBitFlippedAt(result.sum() - 1)
          }.let { extractor.stripHammingMetadata(it) }
      }

    private fun indexesOfInvalidParityBits(input: EncodedString): List<Int> {
        fun toValidationResult(it: Int, input: EncodedString): Pair<Int, Boolean> =
          helper.parityIndicesSequence(it - 1, input.length)
            .map { v -> input[v].toBinaryInt() }
            .fold(input[it - 1].toBinaryInt()) { a, b -> a xor b }
            .let { r -> it to (r == 0) }

        return generateSequence(1) { it * 2 }
          .takeWhile { it < input.length }
          .map { toValidationResult(it, input) }
          .filter { !it.second } // take only failed
          .map { it.first }  // extract only value
          .toList()
    }

    private fun EncodedString.withBitFlippedAt(index: Int): EncodedString = this[index].toString().toInt()
      .let { this.value.replaceRange(index, index + 1, ((it + 1) % 2).toString()) }
      .let(::EncodedString)

    private fun Char.toBinaryInt() = this.toString().toInt()
}
