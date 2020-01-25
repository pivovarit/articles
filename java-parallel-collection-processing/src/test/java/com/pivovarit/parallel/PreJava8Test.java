package com.pivovarit.parallel;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class PreJava8Test {

    private static final List<Integer> integers = IntStream.range(0, 100)
      .boxed()
      .collect(Collectors.toList());

    @Test
    @Disabled
    void example_sequential() throws Exception {
        List<Integer> results = new ArrayList<>();

        for (Integer integer : integers) {
            results.add(Utils.process(integer));
        }

        assertThat(results)
          .containsExactlyElementsOf(integers);
    }

    @Test
    void example_parallel_ordered() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        List<Future<Integer>> futures = new ArrayList<>();

        for (Integer integer : integers) {
            Future<Integer> result = executor.submit(() -> Utils.process(integer));
            futures.add(result);
        }

        List<Integer> results = new ArrayList<>();

        for (Future<Integer> task : futures) {
            results.add(task.get());
        }

        assertThat(results)
          .containsExactlyElementsOf(integers);
    }

    @Test
    void example_parallel_completion_order() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        CompletionService<Integer> completionService =
          new ExecutorCompletionService<>(executor);

        for (Integer integer : integers) {
            completionService.submit(() -> Utils.process(integer));
        }

        List<Integer> results = new ArrayList<>();

        for (int i = 0; i < integers.size(); i++) {
            results.add(completionService.take().get());
        }

        assertThat(results)
          .containsExactlyInAnyOrderElementsOf(integers);
    }
}
