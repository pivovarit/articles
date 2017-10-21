package com.pivovarit.hamming.vavr;

import io.vavr.collection.Stream;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class HammingPropertyTest {

    private final HammingEncoder encoder = HammingEncoder.vavrEncoder();
    private final HammingDecoder decoder = HammingDecoder.vavrDecoder();

    @Test
    void shouldEncodeAndDecode() {
        for (int i = 0; i < 10000; i++) {
            BinaryString msg = randomMessage();
            assertThat(msg)
              .isEqualTo(decoder.decode(encoder.encode(msg)).get());
        }
    }

    @Test
    void shouldEncodeAndDecodeWithSingleBitErrors() {
        Random rand = new Random();
        for (int i = 0; i < 10000; i++) {
            BinaryString msg = randomMessage();
            EncodedString encoded = encoder.encode(msg);
            assertThat(msg)
              .isEqualTo(decoder.decode(withBitFlippedAt(rand.nextInt(encoded.getValue().length()), encoded)).get());
        }
    }

    private BinaryString randomMessage() {
        Random rand = new Random();
        String msg = Stream.continually(() -> rand.nextInt(1))
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
