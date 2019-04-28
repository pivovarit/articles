package com.pivovarit.stream;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.concurrent.CompletableFuture.anyOf;

/**
 * @author Grzegorz Piwowarek
 */
final class CompletionOrderSpliterator<T> implements Spliterator<T> {

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(10, t -> {
            Thread thread = new Thread(t);
            thread.setDaemon(true);
            return thread;
        });

        List<CompletableFuture<Integer>> futures = Stream
          .iterate(0, i -> i + 1)
          .limit(100)
          .map(i -> CompletableFuture.supplyAsync(
            withRandomDelay(i), executorService))
          .collect(Collectors.toList());

        completionOrder(futures)
          .forEach(System.out::println);
    }

    public static <T> Stream<T> completionOrder(Collection<CompletableFuture<T>> futures) {
        return StreamSupport.stream(
          new CompletionOrderSpliterator<>(futures), false);
    }

    public static <T> Stream<T> originalOrder(Collection<CompletableFuture<T>> futures) {
        return futures.stream().map(CompletableFuture::join);
    }

    private static Supplier<Integer> withRandomDelay(Integer i) {
        return () -> {
            try {
                Thread.sleep(ThreadLocalRandom.current()
                  .nextInt(10000));
            } catch (InterruptedException e) {
                // ignore shamelessly, don't do this on production
            }
            return i;
        };
    }

    private final Map<Integer, CompletableFuture<Map.Entry<Integer, T>>> indexedFutures;

    CompletionOrderSpliterator(Collection<CompletableFuture<T>> futures) {
        indexedFutures = toIndexedFutures(futures);
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if (!indexedFutures.isEmpty()) {
            action.accept(nextCompleted());
            return true;
        } else {
            return false;
        }
    }

    private T nextCompleted() {
        return anyOf(indexedFutures.values().toArray(new CompletableFuture[0]))
          .thenApply(result -> ((Map.Entry<Integer, T>) result))
          .thenApply(result -> {
              indexedFutures.remove(result.getKey());
              return result.getValue();
          }).join();
    }

    @Override
    public Spliterator<T> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return indexedFutures.size();
    }

    @Override
    public int characteristics() {
        return SIZED & IMMUTABLE & NONNULL;
    }

    private static <T> Map<Integer, CompletableFuture<Map.Entry<Integer, T>>> toIndexedFutures(Collection<CompletableFuture<T>> futures) {
        Map<Integer, CompletableFuture<Map.Entry<Integer, T>>> map = new HashMap<>(futures.size(), 1);

        int counter = 0;
        for (CompletableFuture<T> f : futures) {
            int index = counter++;
            map.put(index, f.thenApply(value -> new AbstractMap.SimpleEntry<>(index, value)));
        }
        return map;
    }
}


