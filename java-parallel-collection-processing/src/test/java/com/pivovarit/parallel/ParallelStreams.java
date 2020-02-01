package com.pivovarit.parallel;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

class ParallelStreams {
    static <T, R> CompletableFuture<List<R>> inParallel(Collection<T> source, Function<T, R> mapper, Executor executor) {
        return source.stream()
          .map(i -> CompletableFuture.supplyAsync(() -> mapper.apply(i), executor))
          .collect(collectingAndThen(toList(), ParallelStreams::allOfOrException));
    }

    static <T, R> CompletableFuture<List<R>> inParallelBatching(Collection<T> source, Function<T, R> mapper, Executor executor) {
        return null; //TODO
    }

    static <T> CompletableFuture<List<T>> allOfOrException(Collection<CompletableFuture<T>> futures) {
        CompletableFuture<List<T>> result = futures.stream()
          .collect(collectingAndThen(
            toList(),
            l -> CompletableFuture.allOf(l.toArray(new CompletableFuture[0]))
              .thenApply(__1 -> l.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()))));

        for (CompletableFuture<?> f : futures) {
            f.handle((__, ex) -> ex == null || result.completeExceptionally(ex));
        }

        return result;
    }
}
