package com.pivovarit.exception;

import java.util.Collections;
import java.util.IdentityHashMap;

class ExceptionCacheExample {

    public static void main(String[] args) {
        var exceptions = Collections.newSetFromMap(new IdentityHashMap<>());

        String foo = null;
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            try {
                foo.toUpperCase();
            } catch (NullPointerException e) {
                exceptions.add(e);
            }
        }

        System.out.println(exceptions.size());
    }
}
