package com.pivovarit.hamming.domain.encode

import com.pivovarit.hamming.exampleValidData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.pivovarit.hamming.domain.encode.SequentialStatelessHammingEncoder
import com.pivovarit.hamming.domain.message.BinaryString

class SequentialStatelessHammingEncoderTest {

    private val encoder = SequentialStatelessHammingEncoder()

    @Test
    fun shouldEncode() {
        exampleValidData()
          .forEach {
              assertThat(encoder.encode(it.first))
                .isEqualTo(it.second)
          }
    }

    @Test
    fun shouldEncodeZeros() {
        generateSequence("0") { it + "0" }
          .take(1000)
          .map { encoder.encode(BinaryString(it)).value }
          .forEach {
              assertThat(it)
                .doesNotContain("1")
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