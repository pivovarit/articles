package com.pivovarit.es.concurrent_naive;

import java.util.List;

interface ListOp<R> {
    Object apply(List<R> list);
}
