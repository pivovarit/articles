package com.pivovarit.hamming.domain.decode

import com.pivovarit.hamming.domain.BinaryString
import com.pivovarit.hamming.domain.EncodedString
import com.pivovarit.hamming.domain.isPowerOfTwo

internal class HammingMessageExtractor {
    fun stripHammingMetadata(input: EncodedString): BinaryString {
        return input.value.asSequence()
          .filterIndexed { i, _ -> (i + 1).isPowerOfTwo().not() }
          .joinToString("")
          .let(::BinaryString)
    }
}