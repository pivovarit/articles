package com.pivovarit.future;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class CancellableCompletableFutureTest {

    @Test
    void shouldInterruptBackingTask() throws Exception {
        AtomicBoolean result = new AtomicBoolean(false);
        CompletableFuture<Integer> future = CancellableCompletableFuture.supplyAsyncCancellable(() -> {
            try {
                Thread.sleep(Integer.MAX_VALUE);
            } catch (InterruptedException ex) {
                result.set(true);
            }
            return 42;
        }, Executors.newSingleThreadExecutor());

        future.cancel(true);
        try {
            future.join();
        } catch (Exception e) {
        }

        Awaitility.await()
          .atMost(1, TimeUnit.SECONDS)
          .until(result::get);
    }

    @Test
    void shouldNotInterruptBackingTask() throws Exception {
        AtomicBoolean result = new AtomicBoolean(false);
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(Integer.MAX_VALUE);
            } catch (InterruptedException ex) {
                result.set(true);
            }

            return 42;
        }, Executors.newSingleThreadExecutor());

        future.cancel(true);
        try {
            future.join();
        } catch (Exception e) {
        }
        Thread.sleep(100);
        assertThat(result).isFalse();
    }
}