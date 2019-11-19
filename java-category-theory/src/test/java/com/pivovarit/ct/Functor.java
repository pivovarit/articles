package com.pivovarit.ct;

import java.util.function.Function;

public interface Functor<T> {
    <R> Functor<R> fmap(Function<? super T, ? extends R> mapper);
}
