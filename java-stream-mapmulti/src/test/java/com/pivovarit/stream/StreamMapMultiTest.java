package com.pivovarit.stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.of;
import static org.assertj.core.api.Assertions.assertThat;

class StreamMapMultiTest {

    @Test
    void example_1() {
        // import static java.util.stream.Stream.of;
        var result = of(of(1), of(2, 3), Stream.<Integer>empty())
          .flatMap(i -> i)
          .collect(Collectors.toList());

        assertThat(result)
          .containsExactly(1, 2, 3);
    }
}
