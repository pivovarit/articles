package com.pivovarit.chm;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

class ComputeIfAbsentExample {

    public static void main(String[] args) {
        var map = new ConcurrentHashMap<>(1024);
        var counter = new AtomicInteger();
        Stream.of(1, 2)
          .map(tid -> Thread.ofPlatform().start(() -> {
              for (int i = 0; i < 10; i++) {
                  int key = ThreadLocalRandom.current().nextInt(10);
                  map.computeIfAbsent(key, s -> {
                      if (counter.incrementAndGet() > 1) {
                          System.out.println("I told you so!");
                      }
                      doWork();
                      counter.decrementAndGet();
                      return key;
                  });
              }
          })).forEach(t -> {});
    }

    private static void doWork() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }
    }
}
