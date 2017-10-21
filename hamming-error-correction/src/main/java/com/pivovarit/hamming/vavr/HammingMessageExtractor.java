package com.pivovarit.hamming.vavr;

import io.vavr.Tuple;
import io.vavr.collection.Stream;

class HammingMessageExtractor {
    BinaryString stripHammingMetadata(EncodedString input) {
        String raw = Stream.from(0, 1).take(input.getValue().length())
          .map(i -> Tuple.of(i + 1, Character.toString(input.getValue().charAt(i))))
          .filter(i -> (i._1 & -i._1) != i._1)
          .map(i -> i._2)
          .foldLeft("", String::concat);

        return BinaryString.of(raw);
    }
}
