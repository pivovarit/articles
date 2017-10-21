package com.pivovarit.hamming.vavr;

public interface HammingEncoder {

    static HammingEncoder vavrEncoder() {
        return new VavrHammingEncoder();
    }

    EncodedString encode(BinaryString input);
}
