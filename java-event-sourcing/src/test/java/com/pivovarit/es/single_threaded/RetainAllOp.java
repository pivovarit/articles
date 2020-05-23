package com.pivovarit.es.single_threaded;

import java.util.Collection;
import java.util.List;

class RetainAllOp<T> implements ListOp<T> {

    private final Collection<?> elem;

    RetainAllOp(Collection<?> elem) {
        this.elem = elem;
    }

    @Override
    public Object apply(List<T> list) {
        return list.retainAll(elem);
    }

    @Override
    public String toString() {
        return String.format("retainAll(%s)", elem);
    }
}
