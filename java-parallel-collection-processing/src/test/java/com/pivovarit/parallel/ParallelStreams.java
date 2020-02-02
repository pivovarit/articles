package com.pivovarit.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

class ParallelStreams {
    public static <T, R> CompletableFuture<List<R>> inParallel(List<? extends T> source, Function<T, R> mapper, Executor executor) {
        return source.stream()
          .map(i -> CompletableFuture.supplyAsync(() -> mapper.apply(i), executor))
          .collect(collectingAndThen(toList(), ParallelStreams::allOfOrException));
    }

    static <T, R> CompletableFuture<List<R>> inParallelBatching(List<T> source, Function<T, R> mapper, Executor executor, int batches) {
        return BatchingStream.partitioned(source, batches)
          .map(batch -> CompletableFuture.supplyAsync(() -> batching(mapper).apply(batch), executor))
          .collect(collectingAndThen(toList(), ParallelStreams::allOfOrException))
          .thenApply(list -> {
              List<R> result = new ArrayList<>(source.size());
              for (List<R> rs : list) {
                  result.addAll(rs);
              }
              return result;
          });
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

    private static <T, R> Function<List<T>, List<R>> batching(Function<T, R> mapper) {
        return batch -> {
            List<R> list = new ArrayList<>(batch.size());
            for (T t : batch) {
                list.add(mapper.apply(t));
            }
            return list;
        };
    }
}
