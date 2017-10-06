package com.pivovarit.hamming.domain.encode.stateless

import com.pivovarit.hamming.domain.BinaryString
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class BitIndexCalculatorTest {

    private val sut = BitIndexCalculator()

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
              assertThat(sut.getDataBit(it.first, BinaryString("12345678")))
                .isEqualTo(it.second)
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
              assertThat(sut.codewordSize(it.first))
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
              assertThat(sut.parityIndicesSequence(it.first.first, it.first.second).toList())
                .containsExactlyElementsOf(it.second)
          }
    }
}