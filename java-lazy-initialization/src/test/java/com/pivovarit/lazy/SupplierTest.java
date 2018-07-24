package com.pivovarit.lazy;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Supplier;

class SupplierTest {

    @Test
    void example_1() {
        Integer value = compute();
    }

    @Test
    void example_2() {
        Supplier<Integer> value = () -> compute();
    }

    @Test
    void example_3() {
        Optional<Integer> foo = Optional.of(1);
        foo.orElse(compute()); // eager
    }

    @Test
    void example_4() {
        Optional<Integer> foo = Optional.of(1);
        foo.orElseGet(() -> compute()); // lazy
    }

    private static int compute() {
        System.out.println("Computing...");
        return 42;
    }
}
