package com.pivovarit.hamming.domain.encode

import com.pivovarit.hamming.domain.BinaryString
import com.pivovarit.hamming.domain.EncodedString

interface HammingEncoder {
    companion object {
        fun sequentialStateless(): HammingEncoder = SequentialStatelessHammingEncoder()
    }

    fun encode(input: BinaryString): EncodedString
}