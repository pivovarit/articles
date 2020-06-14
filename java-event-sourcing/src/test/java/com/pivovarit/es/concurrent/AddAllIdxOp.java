package com.pivovarit.es.concurrent;

import java.util.Collection;
import java.util.List;

class AddAllIdxOp<T> implements ListOp<T> {

    private final int idx;
    private final Collection<? extends T> elem;

    AddAllIdxOp(int idx, Collection<? extends T> elem) {
        this.idx = idx;
        this.elem = elem;
    }

    @Override
    public Object apply(List<T> list) {
        return list.addAll(idx, elem);
    }

    @Override
    public String toString() {
        return String.format("addAll(%d, %s)", idx, elem);
    }
}
