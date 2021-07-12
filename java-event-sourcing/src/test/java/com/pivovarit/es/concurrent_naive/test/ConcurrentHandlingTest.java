package com.pivovarit.es.concurrent_naive.test;

import com.pivovarit.es.concurrent_naive.ESList;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static com.pivovarit.collectors.ParallelCollectors.Batching;
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

        try {
            Stream.generate(() -> 42)
              .limit(parallelism)
              .collect(Batching.parallel(__ -> {
                  countDownLatch.countDown();
                  try {
                      countDownLatch.await();
                  } catch (InterruptedException e) {
                      throw new RuntimeException(e);
                  }

                  runnable.run();
                  return null;
              }, executorService, parallelism))
              .join();
        } finally {
            executorService.shutdown();
        }
    }
}
