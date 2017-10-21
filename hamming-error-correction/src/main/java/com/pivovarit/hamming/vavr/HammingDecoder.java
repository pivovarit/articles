package com.pivovarit.hamming.vavr;

import io.vavr.control.Option;

interface HammingDecoder {
    static HammingDecoder vavrDecoder() {
        return new VavrHammingDecoder();
    }

    Option<BinaryString> decode(EncodedString input);

    boolean isValid(EncodedString input);
}
