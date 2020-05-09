package com.pivovarit.es;

import java.util.List;

class SetOp<T> implements com.pivovarit.es.ListOp<T> {

    private final int idx;
    private final T elem;

    SetOp(int idx, T elem) {
        this.idx = idx;
        this.elem = elem;
    }

    @Override
    public Object apply(List<T> list) {
        return list.set(idx, elem);
    }

    @Override
    public String toString() {
        return String.format("set{idx=%d, elem=%s}", idx, elem);
    }
}
