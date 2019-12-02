package com.pivovarit.future;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class CancellableCompletableFutureTest {

    @Test
    void shouldInterruptBackingTask() throws Exception {
        AtomicBoolean result = new AtomicBoolean(false);
        CompletableFuture<Integer> future = CancellableCompletableFuture
          .supplyAsyncCancellable(interruptibleTask(result), Executors.newSingleThreadExecutor());

        future.cancel(true);
        try {
            future.join();
        } catch (Exception e) { }

        await()
          .atMost(1, TimeUnit.SECONDS)
          .until(result::get);
    }

    @Test
    void shouldNotInterruptBackingTask() throws Exception {
        AtomicBoolean result = new AtomicBoolean(false);
        CompletableFuture<Integer> future = CompletableFuture
          .supplyAsync(interruptibleTask(result), Executors.newSingleThreadExecutor());

        future.cancel(true);
        try {
            future.join();
        } catch (Exception e) { }

        Thread.sleep(100);
        assertThat(result).isFalse();
    }

    private static Supplier<Integer> interruptibleTask(AtomicBoolean result) {
        return () -> {
            try {
                Thread.sleep(Integer.MAX_VALUE);
            } catch (InterruptedException ex) {
                result.set(true);
            }
            return 42;
        };
    }
}