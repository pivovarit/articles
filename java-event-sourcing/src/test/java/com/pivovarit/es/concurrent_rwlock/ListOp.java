package com.pivovarit.es.concurrent_rwlock;

import java.util.List;

interface ListOp<R> {
    Object apply(List<R> list);
}
