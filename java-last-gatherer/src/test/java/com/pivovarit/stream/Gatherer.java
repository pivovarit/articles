package com.pivovarit.stream;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Gatherer.Downstream;
import java.util.stream.Gatherer.Integrator;

public interface Gatherer<T, A, R> {
    Supplier<A> initializer();
    Integrator<A, T, R> integrator();
    BinaryOperator<A> combiner();
    BiConsumer<A, Downstream<? super R>> finisher();

    boolean integrate(A state, T element, Downstream<? super R> downstream);

}
