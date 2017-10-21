package com.pivovarit.hamming.vavr;

import java.util.Optional;

class VavrHammingDecoder implements HammingDecoder {
    @Override
    public Optional<BinaryString> decode(EncodedString input) {
        return null;
    }

    @Override
    public boolean isValid(EncodedString input) {
        return false;
    }
}
