package com.pivovarit.hamming.vavr;

import io.vavr.Tuple;
import io.vavr.collection.List;
import io.vavr.collection.Stream;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;

class VavrHammingDecoder implements HammingDecoder {

    private final HammingHelper helper = new HammingHelper();
    private final HammingMessageExtractor extractor = new HammingMessageExtractor();

    @Override
    public boolean isValid(EncodedString input) {
        return indexesOfInvalidParityBits(input).isEmpty();
    }

    @Override
    public BinaryString decode(EncodedString input) {

        List<Integer> result = indexesOfInvalidParityBits(input);
        EncodedString corrected = Match(result.isEmpty()).of(
          Case($(true), () -> input),
          Case($(false), () -> withBitFlippedAt(input, result.reduce((a, b) -> a + b) - 1))
        );

        return extractor.stripHammingMetadata(corrected);
    }

    private List<Integer> indexesOfInvalidParityBits(EncodedString input) {
        return Stream.iterate(1, i -> i * 2)
          .takeWhile(it -> it < input.getValue().length())
          .map(it -> {
              boolean result = helper.parityIndicesSequence(it - 1, input.getValue().length())
                .map(v -> toBinaryInt(input, v))
                .fold(toBinaryInt(input, it - 1), (a, b) -> a ^ b) == 0;
              return Tuple.of(it, result);
          })
          .filter(t -> !t._2)
          .map(t -> t._1)
          .toList();
    }

    private Integer toBinaryInt(EncodedString input, Integer v) {
        return Integer.valueOf(Character.toString(input.getValue().charAt(v)));
    }

    private EncodedString withBitFlippedAt(EncodedString source, int ind) {
        char it = source.getValue().charAt(ind);
        StringBuilder builder = new StringBuilder(source.getValue());
        builder.setCharAt(ind, it == '0' ? '1' : '0');
        return EncodedString.of(builder.toString());
    }
}
