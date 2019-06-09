package com.pivovarit.stream;

import java.util.List;
import java.util.Queue;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * @author Grzegorz Piwowarek
 */
final class CompletionOrderSpliterator3<T> implements Spliterator<T> {

    private final int initialSize;
    private final Queue<CompletableFuture<T>> completed = new ConcurrentLinkedQueue<>();
    private int remaining;

    CompletionOrderSpliterator3(List<CompletableFuture<T>> futures) {
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
        CompletableFuture<T> next = completed.poll();
        while (next == null) {
            Thread.onSpinWait();
            next = completed.poll();
        }
        return next;
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


