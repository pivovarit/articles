package com.pivovarit.stream;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.pivovarit.stream.SlidingWindowSpliterator.windowed;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class SlidingWindowStreamTest {

    @Test
    void shouldApplySlidingWindow() {
        var source = List.of(1, 2, 3, 4);

        var result = windowed(source, 3)
          .map(s -> s.collect(toList()))
          .collect(toList());

        assertThat(result)
          .containsExactly(List.of(1, 2, 3), List.of(2, 3, 4));
    }

    @Test
    void shouldApplySlidingWindowToStreamSmallerThanWindow() {
        var source = List.of(1, 2);

        var result = windowed(source, 3)
          .map(s -> s.collect(toList()))
          .collect(toList());

        assertThat(result).isEmpty();
    }

    @Test
    void shouldApplySlidingWindowToEmptyStream() {
        var source = Collections.emptyList();

        var result = windowed(source, 3)
          .map(s -> s.collect(toList()))
          .collect(toList());

        assertThat(result).isEmpty();
    }

    @Test
    void shouldApplyZeroSlidingWindow() {
        var source = List.of(1, 2, 3, 4);

        var result = windowed(source, 0)
          .map(s -> s.collect(toList()))
          .collect(toList());

        assertThat(result).isEmpty();
    }

    @Test
    void shouldDoNotLateBindToInternalBuffer() {
        var source = List.of(1, 2, 3, 4);

        List<Stream<Integer>> result = windowed(source, 2)
          .collect(toList());

        var s3 = result.get(2);

        assertThat(s3.collect(toList())).containsExactly(3, 4);
    }

    @Test
    void shouldCalculateSize() {
        var source = List.of(1, 2, 3, 4);

        var result = windowed(source, 3).spliterator().estimateSize();

        assertThat(result).isEqualTo(2);
    }

    @Test
    void shouldEstimateSizeWhenWindowTooBig() {
        var source = List.of(1, 2, 3, 4);

        var result = windowed(source, source.size() + 1).spliterator().estimateSize();

        assertThat(result).isEqualTo(0);
    }
}
