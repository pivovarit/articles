package com.pivovarit.ct;

public interface Semigroup<T> {
    T combine(T t1, T t2);
}
