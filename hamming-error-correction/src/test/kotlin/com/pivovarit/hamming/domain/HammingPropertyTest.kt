package com.pivovarit.hamming.domain

import com.pivovarit.hamming.domain.decode.HammingDecoder
import com.pivovarit.hamming.domain.encode.HammingEncoder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Random

class HammingPropertyTest {
    private val rand = Random()

    private val encoder = HammingEncoder.sequentialStateless()
    private val decoder = HammingDecoder.sequentialStateless()

    @Test
    fun shouldEncodeAndDecode() = repeat(10000) {
        randomMessage().let {
            assertThat(it)
              .isEqualTo(decoder.decode(encoder.encode(it)))
        }
    }

    @Test
    fun shouldEncodeAndDecodeWithSingleBitErrors() = repeat(10000) {
        randomMessage().let {
            assertThat(it).isEqualTo(decoder.decode(encoder.encode(it)
              .withBitFlippedAt(rand.nextInt(it.length))))
        }
    }

    private fun randomMessage(): BinaryString =
      generateSequence { rand.nextInt(2).toString() }
        .take(rand.nextInt(1000).inc())
        .reduce { acc, s -> acc + s }
        .let(::BinaryString)


    private fun EncodedString.withBitFlippedAt(ind: Int): EncodedString = this[ind].toString().toInt()
      .let { this.value.replaceRange(ind, ind + 1, ((it + 1) % 2).toString()) }
      .let(::EncodedString)
}
