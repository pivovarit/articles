package com.pivovarit.stream;

import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

record SingleElementLastGatherer<T>() implements Gatherer<T, SingleElementLastGatherer.ValueHolder<T>, T> {

    @Override
    public Supplier<ValueHolder<T>> initializer() {
        return ValueHolder::new;
    }

    @Override
    public Integrator<ValueHolder<T>, T, T> integrator() {
        return Integrator.ofGreedy((state, element, _) -> {
            state.value = element;
            state.isSet = true;
            return true;
        });
    }

    @Override
    public BiConsumer<ValueHolder<T>, Downstream<? super T>> finisher() {
        return (state, downstream) -> {
            if (state.isSet && !downstream.isRejecting()) {
                downstream.push(state.value);
            }
        };
    }

    static class ValueHolder<T> {
        private T value;
        private boolean isSet;
    }
}
