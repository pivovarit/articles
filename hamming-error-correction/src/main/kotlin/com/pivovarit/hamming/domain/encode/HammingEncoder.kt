package com.pivovarit.hamming.domain.encode

import com.pivovarit.hamming.domain.message.BinaryString
import com.pivovarit.hamming.domain.message.EncodedString

interface HammingEncoder {
    companion object {
        fun sequentialStateless() : HammingEncoder = SequentialStatelessHammingEncoder()
    }
    fun encode(input: BinaryString): EncodedString
}