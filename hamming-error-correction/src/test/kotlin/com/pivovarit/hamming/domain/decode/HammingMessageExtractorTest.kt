package com.pivovarit.hamming.domain.decode

import com.pivovarit.hamming.exampleValidData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HammingMessageExtractorTest {

    private val sut = HammingMessageExtractor()

    @Test
    fun shouldStripHammingMetadata() {
        exampleValidData()
          .forEach {
              assertThat(sut.stripHammingMetadata(it.second))
                .isEqualTo(it.first)
          }
    }
}
