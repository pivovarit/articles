package com.pivovarit.parallel;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class ParallelCollectorsTest {
    private static final List<Integer> integers = IntStream.range(0, 100)
      .boxed()
      .collect(Collectors.toList());

    @Test
    @Disabled
    void example_sequential() throws Exception {
        List<Integer> results = integers.stream()
          .map(Utils::process)
          .collect(toList());

        assertThat(results)
          .containsExactlyElementsOf(integers);
    }

    @Test
    void example_parallel_ordered() throws Exception {
        // TODO
    }

    @Test
    void example_parallel_ordered_max_parallelism() throws Exception {
        // TODO
    }

    @Test
    void example_parallel_ordered_async() throws Exception {
        // TODO
    }

    @Test
    void example_parallel_completion_order() throws Exception {
        // TODO
    }
}
