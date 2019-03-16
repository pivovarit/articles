package com.pivovarit.stream;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class RandomStreamTest {

    @Test
    void example_1() {
        IntStream.range(0, 10).boxed()
          .collect(RandomCollectors.toLazyShuffledStream())
          .forEach(System.out::println);
    }

    @Test
    void should_shuffle() {
        var source = IntStream.range(0, 100_000).boxed().collect(toList());

        var result = source.stream()
          .collect(RandomCollectors.toLazyShuffledStream())
          .collect(toList());

        assertThat(result)
          .hasSameSizeAs(source)
          .doesNotContainSequence(source);
    }

    @Test
    void should_shuffle_improved() {
        var source = IntStream.range(0, 100_000).boxed().collect(toList());

        var result = source.stream()
          .collect(RandomCollectors.toOptimizedLazyShuffledStream())
          .collect(toList());

        assertThat(result)
          .hasSameSizeAs(source)
          .doesNotContainSequence(source);
    }
}
