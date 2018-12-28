package com.pivovarit.stream;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static com.pivovarit.stream.RandomSpliterator.lazyShuffledStream;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class RandomStreamTest {

    @Test
    void should_shuffle() {
        var source = IntStream.range(0, 100_000).boxed().collect(toList());

        var result = source.stream()
          .collect(lazyShuffledStream())
          .collect(toList());

        assertThat(result)
          .hasSameSizeAs(source)
          .doesNotContainSequence(source);
    }
}
