package com.pivovarit.stream;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

record LastGathererTake3<T>(int n) implements Gatherer<T, ArrayDeque<T>, T> {

    @Override
    public Supplier<ArrayDeque<T>> initializer() {
        return ArrayDeque::new;
    }

    @Override
    public Integrator<ArrayDeque<T>, T, T> integrator() {
        return Integrator.ofGreedy((state, element, ignored) -> {
            if (state.size() == n) {
                state.removeFirst();
            }
            state.addLast(element);
            return true;
        });
    }

    @Override
    public BiConsumer<ArrayDeque<T>, Downstream<? super T>> finisher() {
        return (state, ds) -> {
            for (Iterator<T> it = state.iterator(); it.hasNext() && !ds.isRejecting(); ) {
                if (!ds.push(it.next())) {
                    break;
                }
            }
        };
    }
}


