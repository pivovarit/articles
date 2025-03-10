package com.pivovarit.reactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

class E3_ReactorContextMDC {

    record Example1() {

        private static final Logger log = LoggerFactory.getLogger(Example1.class);

        public static void main(String[] args) {

            MDC.put("tid", "4comprehension");

            Mono.just("table 42")
              .publishOn(Schedulers.newSingle("waiter"))
              .map(i -> {
                  log.info("processing i");
                  // ...
                  return i;
              })
              .publishOn(Schedulers.newSingle("barista"))
              .map(i -> {
                  log.info("processing i");
                  // ...
                  return i;
              })
              .block();

            // 124  [waiter-1] INFO  tenantId:!missing! - processing i
            // 125  [barista-2] INFO  tenantId:!missing! - processing i
        }
    }

    record Example2() {

        private static final Logger log = LoggerFactory.getLogger(Example2.class);

        public static void main(String[] args) {

            MDC.put("tid", "4comprehension");

            Mono.just("table 42")
              .publishOn(Schedulers.newSingle("waiter"))
              .flatMap(i -> Mono.deferContextual(ctx -> {
                  try (var ignored = MDC.putCloseable("tid", ctx.get("tid"))) {
                      log.info("processing i");
                      // ...
                      return Mono.just(i);
                  }
              }))
              .publishOn(Schedulers.newSingle("barista"))
              .flatMap(i -> Mono.deferContextual(ctx -> {
                  try (var ignored = MDC.putCloseable("tid", ctx.get("tid"))) {
                      log.info("processing i");
                      // ...
                      return Mono.just(i);
                  }
              }))
              .contextWrite(Context.of("tid", MDC.get("tid")))
              .block();

            // 124  [waiter-1] INFO  tenantId:!missing! - processing i
            // 125  [barista-2] INFO  tenantId:!missing! - processing i
        }
    }

    record Example3() {

        private static final Logger log = LoggerFactory.getLogger(Example3.class);

        public static void main(String[] args) {

            MDC.put("tid", "4comprehension");

            Mono.just("table 42")
              .publishOn(Schedulers.newSingle("waiter"))
              .flatMap(withMDC(i -> {
                  log.info("processing i");
                  // ...
                  return Mono.just(i);
              }))
              .publishOn(Schedulers.newSingle("barista"))
              .flatMap(withMDC(i -> {
                  log.info("processing i");
                  // ...
                  return Mono.just(i);
              }))
              .contextWrite(Context.of("tid", MDC.get("tid")))
              .block();

            // 124  [waiter-1] INFO  tenantId:!missing! - processing i
            // 125  [barista-2] INFO  tenantId:!missing! - processing i
        }

        static <T, R> Function<? super T, Mono<? extends R>> withMDC(Function<? super T, ? extends R> mapper) {
            Objects.requireNonNull(mapper);
            return i -> Mono.deferContextual(ctx -> {
                try (var ignored = MDC.putCloseable("tid", ctx.get("tid"))) {
                    return Mono.just(mapper.apply(i));
                }
            });
        }
    }

    record Example4() {
        private static final Logger log = LoggerFactory.getLogger(Example4.class);

        public static void main(String[] args) {
            Mono.just("table 42")
              .publishOn(Schedulers.newSingle("waiter"))
              .doOnNext(i -> log.info("processing :{}", i))
              .block();
        }
    }

    record Example5() {
        private static final Logger log = LoggerFactory.getLogger(Example5.class);

        public static void main(String[] args) {
            MDC.put("tid", "4comprehension");

            Mono.just("table 42")
              .publishOn(Schedulers.newSingle("waiter"))
              .doOnEach(signal -> {
                  if (signal.isOnNext()) {
                      try (var ignored = MDC.putCloseable("tid", signal.getContextView().get("tid"))) {
                          log.info("processing :{}", signal.get());
                      }
                  }
              })
              .contextWrite(Context.of("tid", MDC.get("tid")))
              .block();
        }


    }

    record Example6() {
        private static final Logger log = LoggerFactory.getLogger(Example6.class);

        public static void main(String[] args) {
            MDC.put("tid", "4comprehension");

            Mono.just("table 42")
              .publishOn(Schedulers.newSingle("waiter"))
              .doOnEach(withMDC(c -> log.info("processing :{}", c)))
              .contextWrite(Context.of("tid", MDC.get("tid")))
              .block();
        }

        static <T> Consumer<Signal<? extends T>> withMDC(Consumer<? super T> consumer) {
            return signal -> {
                if (signal.isOnNext()) {
                    try (var ignored = MDC.putCloseable("tid", signal.getContextView().get("tid"))) {
                        consumer.accept(signal.get());
                    }
                }
            };
        }
    }
}
