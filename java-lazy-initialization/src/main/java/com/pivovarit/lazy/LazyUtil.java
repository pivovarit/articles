package com.pivovarit.lazy;

import java.util.concurrent.ThreadLocalRandom;

final class LazyUtil {

    private LazyUtil() {
    }

    public static void main(String[] args) {
        Lazy.of(() -> compute(42))
          .map(s -> compute(13))
          .flatMap(s -> lazyCompute(15))
          .filter(v -> v > 0);
        //.get();
    }

    private static int compute(int val) {
        int result = ThreadLocalRandom.current().nextInt() % val;
        System.out.println("Computed: " + result);
        return result;
    }

    private static Lazy<Integer> lazyCompute(int val) {
        return Lazy.of(() -> {
            int result = ThreadLocalRandom.current().nextInt() % val;
            System.out.println("Computed: " + result);
            return result;
        });
    }
}
