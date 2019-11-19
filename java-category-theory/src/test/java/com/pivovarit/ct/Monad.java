package com.pivovarit.ct;

import java.util.function.Function;

public interface Monad<T> {
    <R> Monad<R> flatMap(Function<? super T, ? extends Monad<R>> mapper);
}
