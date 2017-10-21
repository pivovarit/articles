package com.pivovarit.hamming.vavr;

import io.vavr.collection.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class VavrHammingEncoderTest {

    private HammingEncoder sut = HammingEncoder.vavrEncoder();

    @ParameterizedTest(name = "{0} should be encoded to {1}")
    @CsvSource({
      "1,111",
      "01,10011",
      "11,01111",
      "1001000,00110010000",
      "1100001,10111001001",
      "1101101,11101010101",
      "1101001,01101011001",
      "1101110,01101010110",
      "1100111,01111001111",
      "0100000,10011000000",
      "1100011,11111000011",
      "1101111,10101011111",
      "1100100,11111001100",
      "1100101,00111000101",
      "10011010,011100101010"}
    )
    void shouldEncode(String first, String second) {
        assertThat(sut.encode(BinaryString.of(first)))
          .isEqualTo(EncodedString.of(second));
    }

    @Test
    @DisplayName("should always encode zeros to zeros")
    void shouldEncodeZeros() {
        Stream.iterate("0", i -> i + "0")
          .take(1000)
          .map(it -> sut.encode(BinaryString.of(it)).getValue())
          .forEach(msg -> assertThat(msg).doesNotContain("1"));
    }
}
