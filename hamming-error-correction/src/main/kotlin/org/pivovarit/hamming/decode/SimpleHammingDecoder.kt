package org.pivovarit.hamming.decode

import org.pivovarit.hamming.message.BinaryString
import org.pivovarit.hamming.message.EncodedString

class SimpleHammingDecoder : HammingDecoder {
    override fun decode(input: EncodedString): BinaryString? {
        return null
    }
}