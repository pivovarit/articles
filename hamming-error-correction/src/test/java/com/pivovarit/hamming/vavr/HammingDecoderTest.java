package com.pivovarit.hamming.vavr;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class HammingDecoderTest {

    private final HammingDecoder sut = HammingDecoder.vavrDecoder();

    @ParameterizedTest(name = "{1} should be decoded to {0}")
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
      "10011010,011100101010"
    })
    void shouldDecodeCodewordWithoutErrors(String first, String second) {
        assertThat(sut.isValid(EncodedString.of(second)))
          .isTrue();
        assertThat(sut.decode(EncodedString.of(second)))
          .isEqualTo(BinaryString.of(first));
    }

    @ParameterizedTest(name = "{1} should be decoded to {0}")
    @CsvSource({
      "1,101",
      "01,00011",
      "11,11111",
      "1001000,00110000000",
      "1100001,10111011001",
      "1101101,11101010100",
      "1101001,01101011011",
      "1101110,01101010010",
      "1100111,01111000111",
      "0100000,10011010000",
      "1100011,11111010011",
      "1101111,10101111111",
      "1100100,11111101100",
      "1100101,00111100101",
      "10011010,011110101010"
    })
    void shouldDecodeCodewordWithSingleBitErrors(String first, String second) {
        assertThat(sut.isValid(EncodedString.of(second)))
          .isFalse();
        assertThat(sut.decode(EncodedString.of(second)))
          .isEqualTo(BinaryString.of(first));
    }

    @ParameterizedTest(name = "{0} error should be detected")
    @CsvSource({
      "001",
      "11111",
      "00011",
      "11110010000",
      "10100001001",
      "11110010101",
      "01110011001",
      "01101001110",
      "01111000011",
      "10011001100",
      "11111011011",
      "10101000111",
      "11111111100",
      "00100000101",
      "010000101010"
    })
    void shouldDetectCodewordWithDoubleBitErrors(String first) {
        assertThat(sut.isValid(EncodedString.of(first))).isFalse();
    }
}
