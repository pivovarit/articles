package com.pivovarit.stream;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.pivovarit.stream.ShuffledSpliterator.shuffledStream;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class ShuffledStreamTest {

    @Test
    void should_shuffle() {
        var source = IntStream.range(0, 100_000).boxed().collect(toList());

        var result = source.stream()
          .collect(shuffledStream())
          .collect(toList());

        assertThat(result).doesNotContainSequence(source);
    }

    @Test
    void should_shuffle_empty() {
        var result = Stream.of()
          .collect(shuffledStream())
          .collect(toList());

        assertThat(result).isEmpty();
    }
}
