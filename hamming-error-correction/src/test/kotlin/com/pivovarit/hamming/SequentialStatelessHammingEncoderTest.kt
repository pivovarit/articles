package com.pivovarit.hamming

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.pivovarit.hamming.HammingEncoder
import org.pivovarit.hamming.SequentialStatelessHammingEncoder
import org.pivovarit.hamming.message.BinaryString
import org.pivovarit.hamming.message.EncodedString

class SequentialStatelessHammingEncoderTest {

    private val encoder: HammingEncoder = SequentialStatelessHammingEncoder()

    @Test
    fun shouldEncode() {
        listOf(
          "10011010" to "011100101010",
          "01" to "10011",
          "11" to "01111",
          "1" to "111",
          "1001000" to "00110010000",
          "1100001" to "10111001001",
          "1101101" to "11101010101",
          "1101001" to "01101011001",
          "1101110" to "01101010110",
          "1100111" to "01111001111",
          "0100000" to "10011000000",
          "1100011" to "11111000011",
          "1101111" to "10101011111",
          "1100100" to "11111001100",
          "1100101" to "00111000101")
          .map { BinaryString(it.first) to EncodedString(it.second) }
          .forEach {
              assertThat(encoder.encode(it.first))
                .isEqualTo(it.second)
          }

        /**
         * home made property testing
         */
        generateSequence("0") { it + "0" }
          .take(1000)
          .map { encoder.encode(BinaryString(it)).value }
          .forEach {
              assertThat(it)
                .doesNotContain("1")
          }
    }

    @Test
    fun shouldGetCodewordSize() {
        listOf(
          1 to 3,
          2 to 5,
          3 to 6,
          4 to 7,
          5 to 9,
          6 to 10,
          7 to 11,
          8 to 12)
          .forEach {
              assertThat(SequentialStatelessHammingEncoder.codewordSize(it.first))
                .isEqualTo(it.second)
          }
    }

    @Test
    fun shouldGetGroupedIndicesSequence() {
        listOf(
          (0 to 3) to listOf(2),
          (1 to 3) to listOf(2),
          (0 to 12) to listOf(2, 4, 6, 8, 10),
          (1 to 12) to listOf(2, 5, 6, 9, 10),
          (3 to 18) to listOf(4, 5, 6, 11, 12, 13, 14),
          (1 to 18) to listOf(2, 5, 6, 9, 10, 13, 14, 17))
          .forEach {
              assertThat(SequentialStatelessHammingEncoder.parityIndicesSequence(it.first.first, it.first.second).toList())
                .containsExactlyElementsOf(it.second)
          }
    }

    @Test
    fun shouldGetDataBit() {
        listOf(
          2 to "1",
          4 to "2",
          5 to "3",
          6 to "4",
          8 to "5",
          9 to "6",
          10 to "7",
          11 to "8")
          .forEach {
              assertThat(SequentialStatelessHammingEncoder.getDataBit(it.first, BinaryString("12345678")))
                .isEqualTo(it.second)
          }
    }
}