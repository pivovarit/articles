package com.pivovarit.hamming.vavr;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class HammingHelperTest {

    private final HammingHelper sut = new HammingHelper();

    @ParameterizedTest(name = "Data bit for {0} should be {1}")
    @CsvSource({
      "2,1",
      "4,2",
      "5,3",
      "6,4",
      "8,5",
      "9,6",
      "10,7",
      "11,8"
    })
    void shouldGetDataBit(int first, String second) {
        assertThat(sut.getDataBit(first, BinaryString.of("12345678")))
          .isEqualTo(second);
    }

    @ParameterizedTest(name = "Codeword size for {0} should be {1}")
    @CsvSource({
      "1,3",
      "2,5",
      "3,6",
      "4,7",
      "5,9",
      "6,10",
      "7,11",
      "8,12"
    })
    void shouldGetCodewordSize(int first, int second) {
        assertThat(sut.codewordSize(first))
          .isEqualTo(second);
    }

    @ParameterizedTest(name = "Sequence for params: {0},{1} should be {2}")
    @CsvSource({
      "0,3,2",
      "1,3,2",
      "0,12,2:4:6:8:10",
      "1,12,2:5:6:9:10",
      "3,18,4:5:6:11:12:13:14",
      "1,18,2:5:6:9:10:13:14:17"
    })
    void shouldGetGroupedIndicesSequence(int first, int second, String third) {
        assertThat(sut.parityIndicesSequence(first, second))
          .containsExactlyElementsOf(Arrays.stream(third.split(":"))
            .map(Integer::valueOf)
            .collect(Collectors.toList()));
    }
}
