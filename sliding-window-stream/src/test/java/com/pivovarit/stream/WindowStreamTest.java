package com.pivovarit.stream;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.pivovarit.stream.SlidingWindowSpliterator.windowed;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class WindowStreamTest {

    @Test
    void applySlidingWindow() {
        var source = List.of(1, 2, 3, 4);

        var result = windowed(source, 3)
          .map(s -> s.collect(toList()))
          .collect(toList());

        assertThat(result)
          .containsExactly(List.of(1, 2, 3), List.of(2, 3, 4));
    }

    @Test
    void applySlidingWindowToStreamSmallerThanWindow() {
        var source = List.of(1, 2);

        var result = windowed(source, 3)
          .map(s -> s.collect(toList()))
          .collect(toList());

        assertThat(result)
          .containsExactly(List.of(1, 2));
    }

    @Test
    void applySlidingWindowToEmptyStream() {
        var source = Collections.emptyList();

        var result = windowed(source, 3)
          .map(s -> s.collect(toList()))
          .collect(toList());

        assertThat(result).isEmpty();
    }

    @Test
    void applyZeroSlidingWindow() {
        var source = List.of(1, 2, 3, 4);

        var result = windowed(source, 0)
          .map(s -> s.collect(toList()))
          .collect(toList());

        assertThat(result).isEmpty();
    }
}
