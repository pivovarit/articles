package com.pivovarit.priority;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PriorityQueueToStreamTest {

    static <T> Stream<T> drainToStream(PriorityQueue<T> queue) {
        Objects.requireNonNull(queue);
        return Stream.generate(queue::poll)
          .limit(queue.size());
    }

    static <T> Stream<T> drainToStreamJava9(PriorityQueue<T> queue) {
        Objects.requireNonNull(queue);
        return Stream.generate(queue::poll)
          .takeWhile(Objects::nonNull);
    }

    static <T> Stream<T> asStream(PriorityQueue<T> queue) {
        Objects.requireNonNull(queue);
        Comparator<? super T> comparator = queue.comparator();
        return comparator != null
          ? queue.stream().sorted(comparator)
          : queue.stream().sorted();
    }

    @Test
    void shouldMaintainInsertionOrder() {
        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparing(String::length));
        List<String> content = Arrays.asList("1", "333", "22", "55555", "4444");
        queue.addAll(content);

        assertThat(queue.stream())
          .containsExactlyElementsOf(content);
    }

    @Test
    void solution_1() throws Exception {
        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparing(String::length));
        queue.addAll(Arrays.asList("1", "333", "22", "55555", "4444"));

        List<String> result = Stream.generate(queue::poll)
          .limit(queue.size())
          .collect(Collectors.toList());

        assertThat(result).containsExactly("1", "22", "333", "4444", "55555");
        assertThat(queue).isEmpty();
    }

    @Test
    void solution_2() throws Exception {
        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparing(String::length));
        queue.addAll(Arrays.asList("1", "333", "22", "55555", "4444"));

        List<String> result = queue.stream()
          .sorted(queue.comparator())
          .collect(Collectors.toList());

        assertThat(result).containsExactly("1", "22", "333", "4444", "55555");
        assertThat(queue).isNotEmpty();
    }

    @Test
    void solution_2_comparable() throws Exception {
        PriorityQueue<String> queue = new PriorityQueue<>(Arrays.asList("1", "333", "22", "55555", "4444"));

        List<String> result = queue.stream()
          .sorted()
          .collect(Collectors.toList());

        assertThat(result).containsExactly("1", "22", "333", "4444", "55555");
        assertThat(queue).isNotEmpty();
    }
}
