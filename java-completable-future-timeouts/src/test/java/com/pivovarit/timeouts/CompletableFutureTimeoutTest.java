package com.pivovarit.timeouts;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompletableFutureTimeoutTest {

    @Test
    void example_orTimeout() {
        assertThatThrownBy(() -> {
            CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(Integer.MAX_VALUE);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                return 42;
            }).orTimeout(1, TimeUnit.SECONDS);

            future.get(); // explicitly waiting until timeout
        })
          .isInstanceOf(java.util.concurrent.ExecutionException.class)
          .hasCauseExactlyInstanceOf(java.util.concurrent.TimeoutException.class);
    }
}
