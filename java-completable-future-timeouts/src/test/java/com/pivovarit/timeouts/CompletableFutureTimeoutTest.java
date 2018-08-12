package com.pivovarit.timeouts;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompletableFutureTimeoutTest {

    @Test
    void example_orTimeout() {
        assertThatThrownBy(() -> {
            CompletableFuture<Integer> future = CompletableFuture.supplyAsync(this::computeEndlessly)
              .orTimeout(1, TimeUnit.SECONDS);

            future.get(); // explicitly waiting until timeout
        })
          .isInstanceOf(java.util.concurrent.ExecutionException.class)
          .hasCauseExactlyInstanceOf(java.util.concurrent.TimeoutException.class);
    }

    @Test
    void example_completeOnTimeout() throws ExecutionException, InterruptedException {
        int defaultValue = 7;
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(this::computeEndlessly)
          .completeOnTimeout(defaultValue, 1, TimeUnit.SECONDS);

        Integer result = future.get(); // explicitly waiting until timeout

        assertThat(result).isEqualTo(defaultValue);
    }

    private Integer computeEndlessly() {
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return 42;
    }
}
