package com.pivovarit.hamming.vavr;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;

class VavrHammingEncoder implements HammingEncoder {

    private final HammingHelper helper = new HammingHelper();

    @Override
    public EncodedString encode(BinaryString input) {
        String result = helper.getHammingCodewordIndices(input.getValue().length())
          .map(i -> toHammingCodeValue(i, input))
          .reduce(String::concat);

        return EncodedString.of(result);
    }

    private String toHammingCodeValue(int it, BinaryString input) {
        return Match(it + 1).of(
          Case($(HammingHelper::isPowerOfTwo), () -> helper.getParityBit(it, input)),
          Case($(), () -> helper.getDataBit(it, input))
        );
    }
}
