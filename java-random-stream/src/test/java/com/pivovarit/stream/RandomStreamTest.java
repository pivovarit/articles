package com.pivovarit.stream;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static com.pivovarit.stream.RandomSpliterator.toLazyShuffledStream;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class RandomStreamTest {

    @Test
    void example_1() {
        IntStream.range(0, 10).boxed()
          .collect(RandomSpliterator.toLazyShuffledStream())
          .forEach(System.out::println);
    }

    @Test
    void should_shuffle() {
        var source = IntStream.range(0, 100_000).boxed().collect(toList());

        var result = source.stream()
          .collect(toLazyShuffledStream())
          .collect(toList());

        assertThat(result)
          .hasSameSizeAs(source)
          .doesNotContainSequence(source);
    }

    @Test
    void should_shuffle_improved() {
        var source = IntStream.range(0, 100_000).boxed().collect(toList());

        var result = source.stream()
          .collect(ImprovedRandomSpliterator.toLazyShuffledStream())
          .collect(toList());

        assertThat(result)
          .hasSameSizeAs(source)
          .doesNotContainSequence(source);
    }
}
