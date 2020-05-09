package com.pivovarit.es;

import java.util.List;

interface ListOp<R> {
    Object apply(List<R> list);
}
