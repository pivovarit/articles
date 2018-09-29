package com.pivovarit.string;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

class ParallelCollectionProcessingTest {

    private static final List<Integer> inputs = IntStream
      .range(0, 1000)
      .boxed()
      .collect(toList());

    @Test
    void parallelStreams() {
        var result = inputs.parallelStream()
          .map(i -> process(i))
          .collect(toList());
    }

    @Test
    void withoutParallelStreams() {
        var executorService = Executors.newCachedThreadPool();
        List<String> results = inputs.stream()
          .map(v -> supplyAsync(() -> process(v), executorService))
          .collect(collectingAndThen(toList(), Collection::stream))
          .map(CompletableFuture::join)
          .collect(toList());
    }

    private static String process(Integer value) {
        try {
            System.out.println("processing[" + value + "]...");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return value + "-processed";
    }
}
