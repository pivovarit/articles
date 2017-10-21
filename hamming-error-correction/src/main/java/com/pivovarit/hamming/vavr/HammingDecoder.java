package com.pivovarit.hamming.vavr;

interface HammingDecoder {
    static HammingDecoder vavrDecoder() {
        return new VavrHammingDecoder();
    }

    BinaryString decode(EncodedString input);

    boolean isValid(EncodedString input);
}
