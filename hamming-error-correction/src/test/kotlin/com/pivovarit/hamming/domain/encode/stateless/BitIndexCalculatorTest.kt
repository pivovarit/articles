package com.pivovarit.hamming.domain.encode.stateless

import com.pivovarit.hamming.domain.BinaryString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class BitIndexCalculatorTest {

    private val sut = BitIndexCalculator()

    @ParameterizedTest(name = "Data bit for {0} should be {1}")
    @CsvSource(
      "2,1",
      "4,2",
      "5,3",
      "6,4",
      "8,5",
      "9,6",
      "10,7",
      "11,8")
    fun shouldGetDataBit(first: Int, second: String) {
        assertThat(sut.getDataBit(first, BinaryString("12345678")))
          .isEqualTo(second)
    }

    @ParameterizedTest(name = "Codeword size for {0} should be {1}")
    @CsvSource(
      "1,3",
      "2,5",
      "3,6",
      "4,7",
      "5,9",
      "6,10",
      "7,11",
      "8,12")
    fun shouldGetCodewordSize(first: Int, second: Int) {
        assertThat(sut.codewordSize(first))
          .isEqualTo(second)
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
              assertThat(sut.parityIndicesSequence(it.first.first, it.first.second).toList())
                .containsExactlyElementsOf(it.second)
          }
    }
}