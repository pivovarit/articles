package com.pivovarit.allof;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public final class CompletableFutures {

    private CompletableFutures() {
    }

    public static <T> CompletableFuture<List<T>> allOf(Collection<CompletableFuture<T>> futures) {
        return futures.stream()
          .collect(collectingAndThen(toList(), l -> CompletableFuture.allOf(l.toArray(new CompletableFuture[0]))
            .thenApply(__ -> l)))
          .thenApply(list -> list.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList()));
    }

    public static <T> CompletableFuture<List<T>> allOfShortcircuiting(Collection<CompletableFuture<T>> futures) {
        CompletableFuture<List<T>> result = new CompletableFuture<>();

        for (CompletableFuture<?> f : futures) {
            f.handle((__, ex) -> ex == null || result.completeExceptionally(ex));
        }

        allOf(futures)
          .handle((r, ex) -> ex != null ? result.completeExceptionally(ex) : result.complete(r));

        return result;
    }

    public static <T> List<T> allOfSync(Collection<CompletableFuture<T>> futures) {
        return futures.stream()
          .map(CompletableFuture::join)
          .collect(Collectors.toList());
    }

    public static <T> CompletableFuture<T> anyOf(List<CompletableFuture<T>> cfs) {
        return CompletableFuture.anyOf(cfs.toArray(new CompletableFuture[0])).thenApply(o -> (T) o);
    }
}
