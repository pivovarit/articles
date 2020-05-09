package com.pivovarit.es;

import java.util.Collection;
import java.util.List;

class RemoveAllOp<T> implements com.pivovarit.es.ListOp<T> {

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
