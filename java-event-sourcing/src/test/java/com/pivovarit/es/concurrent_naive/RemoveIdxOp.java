package com.pivovarit.es.concurrent_naive;

import java.util.List;

class RemoveIdxOp<T> implements ListOp<T> {

    private final int idx;

    RemoveIdxOp(int idx) {
        this.idx = idx;
    }

    @Override
    public Object apply(List<T> list) {
        return list.remove((int) idx);
    }

    @Override
    public String toString() {
        return String.format("remove(index = %d)", idx);
    }
}
