package org.pivovarit.hamming

import org.pivovarit.hamming.message.BinaryString
import org.pivovarit.hamming.message.EncodedString

interface HammingEncoder {
    fun encode(input: BinaryString): EncodedString
}