package com.pivovarit.hamming.domain.decode

import com.pivovarit.hamming.domain.BinaryString
import com.pivovarit.hamming.domain.EncodedString
import com.pivovarit.hamming.domain.decode.stateless.SequentialStatelessDecoder

interface HammingDecoder {
    companion object {
        fun sequentialStateless(): HammingDecoder = SequentialStatelessDecoder()
    }

    fun decode(codeWord: EncodedString): BinaryString
    fun isValid(codeWord: EncodedString): Boolean
}