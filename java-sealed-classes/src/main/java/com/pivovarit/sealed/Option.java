package com.pivovarit.sealed;

import java.util.function.Consumer;

public abstract class Option<T> {

    private Option() {
    }

    public final static class Some<T> extends Option<T> {

        private final T value;

        public Some(T value) {
            this.value = value;
        }
    }

    public final static class None<T> extends Option<T> {
    }

    public final static class Match<T> {
        private final Consumer<Some<T>> someConsumer;
        private final Consumer<None<T>> noneConsumer;

        private Match(Consumer<Some<T>> someConsumer, Consumer<None<T>> noneConsumer) {
            this.someConsumer = someConsumer;
            this.noneConsumer = noneConsumer;
        }

        public static <T> Match<T> of(
          Consumer<Some<T>> someConsumer,
          Consumer<None<T>> noneConsumer) {
            return new Match<>(someConsumer, noneConsumer);
        }
    }
}



