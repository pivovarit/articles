package com.pivovarit.stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TailGatherersTest {

    @Nested
    class ListAccumulatingTests {

        @Test
        void shouldTakeLastElements() {
            assertThat(Stream.of(1, 2, 3).gather(TailGatherers.list_accumulating(2))).containsExactly(2, 3);
        }

        @Test
        void shouldHandleStreamSmallerThanLimit() {
            assertThat(Stream.of(1).gather(TailGatherers.list_accumulating(1000))).containsExactly(1);

        }

        @Test
        void shouldHandleEmpty() {
            assertThat(Stream.of().gather(TailGatherers.list_accumulating(1))).isEmpty();
        }
    }

    @Nested
    class ListReplacingTests {

        @Test
        void shouldTakeLastElements() {
            assertThat(Stream.of(1, 2, 3).gather(TailGatherers.list_replacing(2))).containsExactly(2, 3);
        }

        @Test
        void shouldHandleStreamSmallerThanLimit() {
            assertThat(Stream.of(1).gather(TailGatherers.list_replacing(1000))).containsExactly(1);

        }

        @Test
        void shouldHandleEmpty() {
            assertThat(Stream.of().gather(TailGatherers.list_replacing(1))).isEmpty();
        }
    }

    @Nested
    class DequeTests {
        @Test
        void shouldTakeLastElements() {
            assertThat(Stream.of(1, 2, 3).gather(TailGatherers.deque(2))).containsExactly(2, 3);
        }

        @Test
        void shouldHandleStreamSmallerThanLimit() {
            assertThat(Stream.of(1).gather(TailGatherers.deque(1000))).containsExactly(1);

        }

        @Test
        void shouldHandleEmpty() {
            assertThat(Stream.of().gather(TailGatherers.deque(1))).isEmpty();
        }
    }
}
