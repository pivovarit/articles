package com.pivovarit.parallel;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class PostJava8Test {

    private static final List<Integer> integers = IntStream.range(0, 100)
      .boxed()
      .collect(toList());

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
        ExecutorService executor = Executors.newFixedThreadPool(10);

        /* don't, it doesn't work
        integers.stream()
          .map(i -> CompletableFuture.supplyAsync(() -> Utils.process(i), executor))
          .map(CompletableFuture::join)
          .collect(Collectors.toList());
          */

        List<Integer> results = integers.stream()
          .map(i -> CompletableFuture.supplyAsync(() -> Utils.process(i), executor))
          .collect(collectingAndThen(toList(), list -> list.stream()
            .map(CompletableFuture::join)
            .collect(toList())));

        assertThat(results)
          .containsExactlyElementsOf(integers);
    }

    @Test
    void example_parallel_ordered_async() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        CompletableFuture<List<Integer>> results = integers.stream()
          .map(i -> CompletableFuture.supplyAsync(() -> Utils.process(i), executor))
          .collect(toFuture());

        assertThat(results.join())
          .containsExactlyElementsOf(integers);
    }

    private Collector<CompletableFuture<Integer>, Object, CompletableFuture<List<Integer>>> toFuture() {
        return collectingAndThen(toList(), list -> {
            return CompletableFuture.allOf(list.toArray(new CompletableFuture[0]))
              .thenApply(__ -> list.stream().map(CompletableFuture::join)
                .collect(toList()));
        });
    }

    @Test
    void example_parallel_completion_order() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        List<Integer> results = integers.stream()
          .map(i -> CompletableFuture.supplyAsync(() -> Utils.process(i), executor))
          .collect(toUnorderedStream())
          .collect(toList());

        assertThat(results)
          .containsExactlyInAnyOrderElementsOf(integers);
    }

    private Collector<CompletableFuture<Integer>, Object, Stream<Integer>> toUnorderedStream() {
        return collectingAndThen(toList(), list -> StreamSupport.stream(new CompletionOrderSpliterator<>(list), false));
    }

    static final class CompletionOrderSpliterator<T> implements Spliterator<T> {

        private final int initialSize;
        private final BlockingQueue<CompletableFuture<T>> completed = new LinkedBlockingQueue<>();
        private int remaining;

        CompletionOrderSpliterator(List<CompletableFuture<T>> futures) {
            this.initialSize = futures.size();
            this.remaining = initialSize;
            futures.forEach(f -> f.whenComplete((t, __) -> completed.add(f)));
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if (remaining > 0) {
                nextCompleted().thenAccept(action).join();
                return true;
            } else {
                return false;
            }
        }

        private CompletableFuture<T> nextCompleted() {
            remaining--;
            try {
                return completed.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return initialSize;
        }

        @Override
        public int characteristics() {
            return SIZED | IMMUTABLE | NONNULL;
        }
    }
}
