package com.pivovarit.es.single_threaded;

import java.util.List;

class RemoveOp<T> implements ListOp<T> {

    private final Object elem;

    RemoveOp(Object elem) {
        this.elem = elem;
    }

    @Override
    public Object apply(List<T> list) {
        return list.remove(elem);
    }

    @Override
    public String toString() {
        return String.format("remove(%s)", elem);
    }
}
