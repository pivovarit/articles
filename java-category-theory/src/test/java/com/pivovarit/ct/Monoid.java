package com.pivovarit.ct;

import java.util.function.BinaryOperator;

public interface Monoid<T> extends Semigroup<T> {
    T identity();

    static <T> Monoid<T> of(T identity, BinaryOperator<T> semigroup) {
        return new Monoid<T>() {
            @Override
            public T identity() {
                return identity;
            }

            @Override
            public T combine(T t1, T t2) {
                return semigroup.apply(t1, t2);
            }
        };
    }
}
