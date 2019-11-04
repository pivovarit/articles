package com.pivovarit.allof;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class CompletableFutureAllOfTest {

    @Test
    void example_anyof() {
        var executorService = Executors.newFixedThreadPool(3);
        var cfs = Stream.of(10, 1, 5)
          .map(i -> supplyAsync(() -> returnWithDelay(i, Duration.ofSeconds(i)), executorService))
          .collect(Collectors.toList());

        var result = CompletableFutures.anyOf(cfs).join();
        assertThat(result).isEqualTo(1);
    }

    @Test
    void example_allof() {
        var executorService = Executors.newFixedThreadPool(3);
        var cfs = Stream.of(3, 1, 2)
          .map(i -> supplyAsync(() -> returnWithDelay(i, Duration.ofSeconds(i)), executorService))
          .collect(Collectors.toList());

        var result = CompletableFutures.allOf(cfs).join();
        assertThat(result).containsExactlyInAnyOrder(1, 2, 3);
    }

    @Test
    void example_allof_shortcircuiting() {
        var executorService = Executors.newFixedThreadPool(3);
        var cfs = Stream.of(10, 1, 5)
          .map(i -> supplyAsync(() -> {
              if (i == 5) throw new IllegalStateException();
              return returnWithDelay(i, Duration.ofSeconds(1));
          }, executorService))
          .collect(Collectors.toList());

        var result = CompletableFutures.allOfShortcircuiting(cfs);

        await()
          .atMost(2, TimeUnit.SECONDS)
          .until(result::isCompletedExceptionally);
    }

    private static int returnWithDelay(int i, Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            // ignoring consciously
        }

        return i;
    }
}
