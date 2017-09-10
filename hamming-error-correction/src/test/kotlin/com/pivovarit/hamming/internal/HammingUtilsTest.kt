package com.pivovarit.hamming.internal

import com.pivovarit.hamming.exampleValidData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.pivovarit.hamming.domain.internal.codewordSize
import com.pivovarit.hamming.domain.internal.parityIndicesSequence
import com.pivovarit.hamming.domain.internal.stripHammingMetadata

class HammingUtilsTest {
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
              assertThat(parityIndicesSequence(it.first.first, it.first.second).toList())
                .containsExactlyElementsOf(it.second)
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
              assertThat(codewordSize(it.first))
                .isEqualTo(it.second)
          }
    }

    @Test
    fun shouldStripHammingMetadata() {
        exampleValidData()
          .forEach {
              assertThat(stripHammingMetadata(it.second))
                .isEqualTo(it.first)
          }
    }
}