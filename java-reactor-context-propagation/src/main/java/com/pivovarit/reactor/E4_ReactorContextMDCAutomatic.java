package com.pivovarit.reactor;

import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ThreadLocalAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

class E4_ReactorContextMDCAutomatic {

    record Example() {

        private static final Logger log = LoggerFactory.getLogger(Example.class);

        public static void main(String[] args) {

            Hooks.enableAutomaticContextPropagation();

            ContextRegistry.getInstance().registerThreadLocalAccessor(new ThreadLocalAccessor<String>() {
                @Override
                public Object key() {
                    return "tid";
                }

                @Override
                public String getValue() {
                    return MDC.get("tid");
                }

                @Override
                public void setValue(String value) {
                    MDC.put("tid", value);
                }

                @Override
                public void setValue() {
                    MDC.remove("tid");
                }
            });

            MDC.put("tid", "4comprehension");

            Mono.just("table 42")
              .publishOn(Schedulers.newSingle("waiter"))
              .map(i -> {
                  log.info("processing i");
                  // ...
                  return i;
              })
              .publishOn(Schedulers.newSingle("barista"))
              .doOnNext(i -> log.info("processing i: {}", i))
              .block();
        }
    }
}
