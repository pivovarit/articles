package com.pivovarit.hamming.domain.encode

import com.pivovarit.hamming.domain.BinaryString
import com.pivovarit.hamming.domain.EncodedString
import com.pivovarit.hamming.domain.encode.stateless.SequentialStatelessHammingEncoder

interface HammingEncoder {
    companion object {
        fun sequentialStateless(): HammingEncoder = SequentialStatelessHammingEncoder()
    }

    fun encode(input: BinaryString): EncodedString
}