package com.pivovarit.hamming.domain.decode

import com.pivovarit.hamming.domain.message.BinaryString
import com.pivovarit.hamming.domain.message.EncodedString

interface HammingDecoder {
    companion object {
        fun sequentialStateless() : HammingDecoder = SequentialStatelessDecoder()
    }
    fun decode(input: EncodedString): BinaryString?
}