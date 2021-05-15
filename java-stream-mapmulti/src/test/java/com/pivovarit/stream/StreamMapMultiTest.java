package com.pivovarit.stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.*;

class StreamMapMultiTest {

    @Test
    void example_1_flatmap() {
        // import static java.util.List.of;
        var lists = of(of(1), of(2, 3), List.<Integer>of());
        var result = lists.stream()
          .flatMap(i -> i.stream())
          .collect(Collectors.toList());

        assertThat(result).containsExactly(1, 2, 3);
    }

    @Test
    void example_1_mapmulti() {
        // import static java.util.List.of;
        var lists = of(of(1), of(2, 3), List.<Integer>of());

        var result = lists.stream()
          .mapMulti((list, consumer) -> list.forEach(consumer))
          // .mapMulti(Stream::forEach)
          .collect(Collectors.toList());

        assertThat(result).containsExactly(1, 2, 3);
    }
}
