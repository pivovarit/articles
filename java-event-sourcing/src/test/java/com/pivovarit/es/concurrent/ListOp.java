package com.pivovarit.es.concurrent;

import java.util.List;

interface ListOp<R> {
    Object apply(List<R> list);
}
