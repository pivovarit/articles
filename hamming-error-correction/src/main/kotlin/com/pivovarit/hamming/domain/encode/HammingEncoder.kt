package com.pivovarit.hamming.domain.encode

import com.pivovarit.hamming.domain.BinaryString
import com.pivovarit.hamming.domain.EncodedString
import com.pivovarit.hamming.domain.encode.stateless.ParallelStatelessHammingEncoder
import com.pivovarit.hamming.domain.encode.stateless.SequentialStatelessHammingEncoder

interface HammingEncoder {
    companion object {
        fun sequentialStateless(): HammingEncoder = SequentialStatelessHammingEncoder()
        fun parallelStateless(): HammingEncoder = ParallelStatelessHammingEncoder()
    }

    fun encode(input: BinaryString): EncodedString
}