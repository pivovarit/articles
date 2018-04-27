package com.pivovarit.stream;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.pivovarit.stream.WindowSpliterator.sliding;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class WindowStreamCollectorTest {

    @Test
    void applySlidingWindow() {
        List<Integer> source = asList(1, 2, 3, 4);

        List<List<Integer>> result = source.stream()
          .collect(sliding(3))
          .map(s -> s.collect(toList()))
          .collect(toList());

        assertThat(result)
          .containsExactly(asList(1, 2, 3), asList(2, 3, 4));
    }

    @Test
    void applySlidingWindowToStreamSmallerThanWindow() {
        List<Integer> source = asList(1, 2);

        List<List<Integer>> result = source.stream()
          .collect(sliding(3))
          .map(s -> s.collect(toList()))
          .collect(toList());

        assertThat(result)
          .containsExactly(asList(1, 2));
    }

    @Test
    void applySlidingWindowToEmptyStream() {
        List<Integer> source = Collections.emptyList();

        List<List<Integer>> result = source.stream()
          .collect(sliding(3))
          .map(s -> s.collect(toList()))
          .collect(toList());

        assertThat(result).isEmpty();
    }

    @Test
    void applyZeroSlidingWindow() {
        List<Integer> source = asList(1, 2, 3, 4);

        List<List<Integer>> result = source.stream()
          .collect(sliding(0))
          .map(s -> s.collect(toList()))
          .collect(toList());

        assertThat(result).isEmpty();
    }
}
