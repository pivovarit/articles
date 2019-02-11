package com.pivovarit.collectors;

import org.junit.jupiter.api.Test;

import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.summingDouble;
import static java.util.stream.Collectors.teeing;
import static org.assertj.core.api.Assertions.assertThat;

class TeeingTest {

    @Test
    void expected_value() {

        Double ev = Stream.of(1, 2, 3, 4, 5, 6)
          .collect(teeing(
            summingDouble(i -> i),
            counting(),
            (sum, n) -> sum / n));

        assertThat(ev).isEqualTo(3.5);
    }

    @Test
    void expected_value_collector() {

        Double ev = Stream.of(1, 2, 3, 4, 5, 6)
          .collect(derivingExpectedValue());

        assertThat(ev).isEqualTo(3.5);
    }

    @Test
    void expected_value_before() {

        Integer[] stream = Stream.of(1, 2, 3, 4, 5, 6).toArray(Integer[]::new);
        Double ev = IntStream.range(0, stream.length).boxed()
          .reduce(0d, (acc, i) -> acc + (((double) stream[i]) / stream.length), (acc1, acc2) -> acc1 + acc2);

        assertThat(ev).isEqualTo(3.5);
    }

    private static Collector<Integer, ?, Double> derivingExpectedValue() {
        return teeing(
          summingDouble(i -> i),
          counting(),
          (sum, n) -> sum / n);
    }
}
