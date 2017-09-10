package com.pivovarit.hamming.decode

import com.pivovarit.hamming.exampleData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.pivovarit.hamming.decode.SimpleHammingDecoder
import org.pivovarit.hamming.message.BinaryString
import org.pivovarit.hamming.message.EncodedString

class SimpleHammingDecoderTest {

    private val decoder = SimpleHammingDecoder()

    @Test
    fun shouldDecode() {
        exampleData()
          .map { it.second to it.first }
          .forEach {
              assertThat(decoder.decode(it.first))
                .isEqualTo(it.second)
          }
    }

    @Test
    fun shouldDecodeWithOneBadBit() {
        listOf(
          "1" to "101",
          "01" to "00011",
          "11" to "11111",
          "1001000" to "00111010000",
          "1100001" to "10111011001",
          "1101101" to "11101000101",
          "1101001" to "01101001001",
          "1101110" to "01101000110",
          "1100111" to "01111011111",
          "0100000" to "10011000010",
          "1100011" to "11111000001",
          "1101111" to "10101011101",
          "1100100" to "11111001101",
          "1100101" to "00111000100",
          "10011010" to "011100101110")
          .map { EncodedString(it.second) to BinaryString(it.first) }
          .forEach {
              assertThat(decoder.decode(it.first))
                .isEqualTo(it.second)
          }
    }

    @Test
    fun shouldDetectTwoBadBits() {
        listOf(
          "100",
          "01011",
          "10111",
          "11111010000",
          "10111000001",
          "11101011101",
          "11101001000",
          "11101000111",
          "11111011101",
          "11011010010",
          "11011001001",
          "10111011101",
          "11111011101",
          "10111100100",
          "111100101111")
          .mapNotNull { decoder.decode(EncodedString(it)) }
          .also {
              assertThat(it).isEmpty()
          }
    }
}