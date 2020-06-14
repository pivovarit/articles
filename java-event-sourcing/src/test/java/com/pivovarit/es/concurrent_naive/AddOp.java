package com.pivovarit.es.concurrent_naive;

import java.util.List;

class AddOp<T> implements ListOp<T> {
    private final T elem;

    AddOp(T elem) {
        this.elem = elem;
    }

    @Override
    public Object apply(List<T> list) {
        return list.add(elem);
    }

    @Override
    public String toString() {
        return String.format("add(element = %s)", elem);
    }
}
