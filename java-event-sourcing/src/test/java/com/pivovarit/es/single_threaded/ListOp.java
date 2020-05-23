package com.pivovarit.es.single_threaded;

import java.util.List;

interface ListOp<R> {
    Object apply(List<R> list);
}
