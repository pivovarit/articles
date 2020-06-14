package com.pivovarit.es.concurrent.test;

import com.pivovarit.es.concurrent.ESList;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class ConcurrentHandlingTest {

    @Test
    void binlog_concurrent_modification() throws Exception {
        ESList<Integer> ints = ESList.newInstance();
        executeInParallel(() -> ints.add(1), 500);

        assertThat(ints.snapshot()).hasSize(500);
    }

    @Test
    void snapshot_concurrent_modification() throws Exception {
        ESList<Integer> ints = ESList.newInstance();

        executeInParallel(() -> ints.add(1), 500);

        assertThat(ints).hasSize(500);
        assertThat(ints.snapshot().stream().mapToInt(i -> i).sum()).isEqualTo(500);
    }

    private void executeInParallel(Runnable runnable, int parallelism) {
        ExecutorService executorService = Executors.newFixedThreadPool(parallelism);
        CountDownLatch countDownLatch = new CountDownLatch(parallelism);
        Stream.generate(() -> 42)
          .limit(parallelism)
          .map(__ -> runAsync(() -> {
              countDownLatch.countDown();
              try {
                  countDownLatch.await();
              } catch (InterruptedException e) {
                  throw new RuntimeException(e);
              }

              runnable.run();
          }, executorService))
          .collect(collectingAndThen(toList(), l -> allOf(l.toArray(CompletableFuture[]::new))))
          .join();
        executorService.shutdown();
    }
}
