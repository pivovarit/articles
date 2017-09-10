package org.pivovarit.hamming.decode

import org.pivovarit.hamming.message.BinaryString
import org.pivovarit.hamming.message.EncodedString

interface HammingDecoder {
    fun decode(input: EncodedString): BinaryString?
}