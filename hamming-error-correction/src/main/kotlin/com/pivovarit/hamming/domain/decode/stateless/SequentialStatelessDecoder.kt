package com.pivovarit.hamming.domain.decode.stateless

import com.pivovarit.hamming.domain.BinaryString
import com.pivovarit.hamming.domain.EncodedString
import com.pivovarit.hamming.domain.decode.HammingDecoder
import com.pivovarit.hamming.domain.decode.HammingMessageExtractor
import com.pivovarit.hamming.domain.encode.stateless.HammingHelper

internal class SequentialStatelessDecoder : HammingDecoder {
    private val helper = HammingHelper()
    private val extractor = HammingMessageExtractor()

    override fun isValid(input: EncodedString) =
      indexesOfInvalidParityBits(input).isEmpty()

    override fun decode(input: EncodedString): BinaryString? =
      indexesOfInvalidParityBits(input)
        .let { result ->
            when (result.size) {
                0 -> extractor.stripHammingMetadata(input)
                else -> when (result.sum() > input.length) {
                    false -> input.withBitFlippedAt(result.sum() - 1)
                      .let { extractor.stripHammingMetadata(it) }
                    true -> null
                }
            }
        }

    private fun indexesOfInvalidParityBits(input: EncodedString): List<Int> {
        return generateSequence(1) { it * 2 }
          .takeWhile { it < input.length }
          .map {
              helper.parityIndicesSequence(it - 1, input.length)
                .map { v -> input[v].toBinaryInt() }
                .fold(input[it - 1].toBinaryInt()) { a, b -> a xor b }
                .let { r -> it to (r == 0) }
          }
          .filter { !it.second }
          .map { it.first }
          .toList()
    }

    private fun EncodedString.withBitFlippedAt(index: Int): EncodedString = this[index].toString().toInt()
      .let { this.value.replaceRange(index, index + 1, ((it + 1) % 2).toString()) }
      .let(::EncodedString)

    private fun Char.toBinaryInt() = this.toString().toInt()
}
