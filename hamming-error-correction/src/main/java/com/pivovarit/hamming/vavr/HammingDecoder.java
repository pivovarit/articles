package com.pivovarit.hamming.vavr;

import java.util.Optional;

interface HammingDecoder {
    static HammingDecoder vavrDecoder() {
        return new VavrHammingDecoder();
    }

    Optional<BinaryString> decode(EncodedString input);

    boolean isValid(EncodedString input);
}
