package com.pivovarit.future;

import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.function.Supplier;

public class CancellableCompletableFuture<T> extends CompletableFuture<T> {

    private final Queue<FutureTask<?>> backingTasks = new ConcurrentLinkedQueue<>();

    /**
     * Returns a new cancellable CompletableFuture that is asynchronously completed
     * by a task running in the given executor with the value obtained
     * by calling the given Supplier.
     *
     * @param supplier a function returning the value to be used
     *                 to complete the returned CompletableFuture
     * @param executor the executor to use for asynchronous execution
     * @param <U>      the function's return type
     *
     * @return the new CompletableFuture
     */
    public static <U> CompletableFuture<U> supplyAsyncCancellable(
      Supplier<U> supplier,
      Executor executor) {
        CancellableCompletableFuture<U> future = new CancellableCompletableFuture<U>();
        FutureTask<Void> backingTask = new FutureTask<>(() -> {
            try {
                future.complete(supplier.get());
            } catch (Throwable ex) {
                future.completeExceptionally(ex);
            }
        }, null);
        future.addCompletingTask(backingTask);
        executor.execute(backingTask);
        return future;
    }

    /**
     * If not already completed, completes this CompletableFuture with
     * a {@link CancellationException}. Dependent CompletableFutures
     * that have not already completed will also complete
     * exceptionally, with a {@link CompletionException} caused by
     * this {@code CancellationException}.
     *
     * @param mayInterruptIfRunning {@code true} if the thread executing this
     *                              task should be interrupted; otherwise, in-progress tasks are allowed
     *                              to complete
     *
     * @return {@code true} if this task is now cancelled
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        backingTasks.forEach(task -> task.cancel(mayInterruptIfRunning));
        return super.cancel(mayInterruptIfRunning);
    }

    private void addCompletingTask(FutureTask<?> task) {
        backingTasks.add(task);
    }
}
