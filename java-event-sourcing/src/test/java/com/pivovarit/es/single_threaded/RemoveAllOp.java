package com.pivovarit.es.single_threaded;

import java.util.Collection;
import java.util.List;

class RemoveAllOp<T> implements ListOp<T> {

    private final Collection<?> elem;

    RemoveAllOp(Collection<?> elem) {
        this.elem = elem;
    }

    @Override
    public Object apply(List<T> list) {
        return list.removeAll(elem);
    }

    @Override
    public String toString() {
        return String.format("removeAll(%s)", elem);
    }
}
