package com.pivovarit.hamming.domain.encode.stateless

import com.pivovarit.hamming.domain.BinaryString
import com.pivovarit.hamming.domain.encode.HammingEncoder
import com.pivovarit.hamming.exampleValidData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SequentialStatelessHammingEncoderTest {

    private val sut = HammingEncoder.sequentialStateless()

    @Test
    fun shouldEncode() {
        exampleValidData()
          .forEach {
              assertThat(sut.encode(it.first))
                .isEqualTo(it.second)
          }
    }

    @Test
    fun shouldEncodeZeros() {
        generateSequence("0") { it + "0" }
          .take(1000)
          .map { sut.encode(BinaryString(it)).value }
          .forEach {
              assertThat(it)
                .doesNotContain("1")
          }
    }
}