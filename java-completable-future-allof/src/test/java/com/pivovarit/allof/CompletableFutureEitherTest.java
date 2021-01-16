package com.pivovarit.allof;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static com.pivovarit.allof.CompletableFutures.either;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompletableFutureEitherTest {

    @Test
    void example_either() {
        CompletableFuture<Integer> f1 = CompletableFuture.completedFuture(42);
        CompletableFuture<Integer> f2 = CompletableFuture.failedFuture(new NullPointerException("oh no, anyway"));

        assertThat(either(f1, f2).join()).isEqualTo(42);
        assertThat(either(f2, f1).join()).isEqualTo(42);
    }

    @Test
    void example_either_exception() {
        CompletableFuture<Integer> f1 = CompletableFuture.failedFuture(new NullPointerException("oh no, anyway"));
        CompletableFuture<Integer> f2 = CompletableFuture.failedFuture(new NullPointerException("oh no, anyway"));

        assertThatThrownBy(() -> either(f1, f2).join()).hasCauseExactlyInstanceOf(NullPointerException.class);
    }
}
