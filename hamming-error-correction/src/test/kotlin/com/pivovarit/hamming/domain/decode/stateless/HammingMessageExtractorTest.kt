package com.pivovarit.hamming.domain.decode.stateless

import com.pivovarit.hamming.domain.BinaryString
import com.pivovarit.hamming.domain.EncodedString
import com.pivovarit.hamming.domain.decode.HammingMessageExtractor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class HammingMessageExtractorTest {

    private val sut = HammingMessageExtractor()

    @ParameterizedTest(name = "{1} should be stripped to {0}")
    @CsvSource(
      "1,XX1",
      "01,XX0X1",
      "11,XX1X1",
      "1001000,XX110010000",
      "1100001,XX111001001",
      "1101101,XX1X101X101",
      "1101001,XX1X101X001",
      "1101110,XX1X101X110",
      "1100111,XX1X100X111",
      "0100000,XX0X100X000",
      "1100011,XX1X100X011",
      "1101111,XX1X101X111",
      "1100100,XX1X100X100",
      "1100101,XX1X100X101",
      "10011010,XX1X001X1010")
    fun shouldStripHammingMetadata(first: String, second: String) {
        assertThat(sut.stripHammingMetadata(EncodedString(second)))
          .isEqualTo(BinaryString(first))
    }
}
