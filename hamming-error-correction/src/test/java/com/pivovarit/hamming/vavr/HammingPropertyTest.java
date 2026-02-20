package com.pivovarit.hamming.vavr;

import io.vavr.collection.Stream;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class HammingPropertyTest {

    private static final Random rand = new Random();
    private final HammingEncoder encoder = HammingEncoder.vavrEncoder();
    private final HammingDecoder decoder = HammingDecoder.vavrDecoder();

    @Test
    void shouldEncodeAndDecode() {
        for (int i = 0; i < 10000; i++) {
            BinaryString msg = randomMessage();
            assertThat(msg)
              .isEqualTo(decoder.decode(encoder.encode(msg)));
        }
    }

    @Test
    void shouldEncodeAndDecodeWithSingleBitErrors() {
        for (int i = 0; i < 10000; i++) {
            BinaryString msg = randomMessage();
            EncodedString encoded = encoder.encode(msg);
            assertThat(msg)
              .isEqualTo(decoder.decode(withBitFlippedAt(rand.nextInt(encoded.getValue().length()), encoded)));
        }
    }

    private BinaryString randomMessage() {
        String msg = Stream.continually(() -> rand.nextInt(2))
          .map(i -> Integer.toString(i))
          .take(rand.nextInt(10) + 1)
          .reduce(String::concat);

        return BinaryString.of(msg);
    }

    private EncodedString withBitFlippedAt(int ind, EncodedString source) {
        char it = source.getValue().charAt(ind);
        StringBuilder builder = new StringBuilder(source.getValue());
        builder.setCharAt(ind, it == '0' ? '1' : '0');
        return EncodedString.of(builder.toString());
    }
}
