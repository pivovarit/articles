package com.pivovarit.stream;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

record LastGathererTake2<T>(int n) implements Gatherer<T, ArrayList<T>, T> {

    @Override
    public Supplier<ArrayList<T>> initializer() {
        return ArrayList::new;
    }

    @Override
    public Integrator<ArrayList<T>, T, T> integrator() {
        return Gatherer.Integrator.ofGreedy((state, elem, ignored) -> {
              state.add(elem);
              return true;
          }
        );
    }

    @Override
    public BiConsumer<ArrayList<T>, Downstream<? super T>> finisher() {
        return (state, downstream) -> {
            int start = Math.max(0, state.size() - n);
            for (int i = start; i < state.size(); i++) {
                if (!downstream.push(state.get(i))) {
                    break;
                }
            }
        };
    }
}
