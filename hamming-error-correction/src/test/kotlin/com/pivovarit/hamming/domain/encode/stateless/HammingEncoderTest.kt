package com.pivovarit.hamming.domain.encode.stateless

import com.pivovarit.hamming.domain.BinaryString
import com.pivovarit.hamming.domain.EncodedString
import com.pivovarit.hamming.domain.encode.HammingEncoder
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ParallelStatelessHammingEncoderTest : HammingEncoderTestBase(HammingEncoder.parallelStateless())

class SequentialStatelessHammingEncoderTest : HammingEncoderTestBase(HammingEncoder.sequentialStateless())

abstract class HammingEncoderTestBase(private val sut: HammingEncoder) {

    @ParameterizedTest(name = "{0} should be encoded to {1}")
    @CsvSource(
      "1,111",
      "01,10011",
      "11,01111",
      "1001000,00110010000",
      "1100001,10111001001",
      "1101101,11101010101",
      "1101001,01101011001",
      "1101110,01101010110",
      "1100111,01111001111",
      "0100000,10011000000",
      "1100011,11111000011",
      "1101111,10101011111",
      "1100100,11111001100",
      "1100101,00111000101",
      "10011010,011100101010")
    fun shouldEncode(first: String, second: String) {
        assertThat(sut.encode(BinaryString(first))).isEqualTo(EncodedString(second))
    }

    @Test
    @DisplayName("should always encode zeros to zeros")
    fun shouldEncodeZeros() {
        generateSequence("0") { it + "0" }
          .take(1000)
          .map { sut.encode(BinaryString(it)).value }
          .forEach { assertThat(it).doesNotContain("1") }
    }

}
