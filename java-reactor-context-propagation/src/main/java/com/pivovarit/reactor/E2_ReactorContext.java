package com.pivovarit.reactor;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

class E2_ReactorContext {

    record Example1() {
        public static void main(String[] args) {
            ThreadLocal<String> preferences = new ThreadLocal<>();

            preferences.set("lactose-free");

            Mono.just("table 42")
              .publishOn(Schedulers.newSingle("waiter"))
              .map(i -> {
                  System.out.printf("preferences: %s, thread: %s%n", preferences.get(), Thread.currentThread()
                    .getName());
                  // ...
                  return i;
              })
              .publishOn(Schedulers.newSingle("barista"))
              .map(i -> {
                  System.out.printf("preferences: %s, thread: %s%n", preferences.get(), Thread.currentThread()
                    .getName());
                  // ...
                  return i;
              })
              .block();
        }
    }

    record Example2() {
        public static void main(String[] args) {

            Mono.just("table 42")
              .publishOn(Schedulers.newSingle("waiter"))
              .flatMap(i -> Mono.deferContextual(ctx -> {
                  System.out.printf("preferences: %s, thread: %s%n", ctx.get("preferences"), Thread.currentThread()
                    .getName());
                  // ...
                  return Mono.just(i);
              }))
              .publishOn(Schedulers.newSingle("barista"))
              .flatMap(i -> Mono.deferContextual(ctx -> {
                  System.out.printf("preferences: %s, thread: %s%n", ctx.get("preferences"), Thread.currentThread()
                    .getName());
                  // ...
                  return Mono.just(i);
              }))
              .contextWrite(Context.of("preferences", "lactose-free"))
              .block();

            // preferences: lactose-free, thread: waiter-1
            // preferences: lactose-free, thread: barista-2
        }
    }

    record Example3() {
        public static void main(String[] args) {

            String preferences = "lactose-free";

            Mono.just("table 42")
              .publishOn(Schedulers.newSingle("waiter"))
              .map(i -> {
                  System.out.printf("preferences: %s, thread: %s%n", preferences, Thread.currentThread().getName());
                  // ...
                  return i;
              })
              .publishOn(Schedulers.newSingle("barista"))
              .map(i -> {
                  System.out.printf("preferences: %s, thread: %s%n", preferences, Thread.currentThread().getName());
                  // ...
                  return i;
              })
              .block();

            // preferences: lactose-free, thread: waiter-1
            // preferences: lactose-free, thread: barista-2
        }
    }
}
