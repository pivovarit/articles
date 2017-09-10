package com.pivovarit.hamming.domain.decode

import com.pivovarit.hamming.domain.BinaryString
import com.pivovarit.hamming.domain.EncodedString

interface HammingDecoder {
    companion object {
        fun sequentialStateless() : HammingDecoder = SequentialStatelessDecoder()
    }
    fun decode(input: EncodedString): BinaryString?
}