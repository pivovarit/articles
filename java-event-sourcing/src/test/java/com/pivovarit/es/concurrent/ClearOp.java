package com.pivovarit.es.concurrent;

import java.util.List;

class ClearOp<T> implements ListOp<T> {
    @Override
    public Object apply(List<T> list) {
        list.clear();
        return null;
    }

    @Override
    public String toString() {
        return "clear()";
    }
}
