package com.pivovarit.lazy;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static com.pivovarit.lazy.ThrowingFunction.sneakyThrow;
import static com.pivovarit.lazy.ThrowingFunction.unchecked;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ThrowingFunctionTest {

    @Test
    void example_1() throws Exception {
        assertThrows(IOException.class, () -> {
            sneakyThrow(new IOException());
        });
    }

    @Test
    void example_2() throws Exception {
        assertThrows(IOException.class, () -> {
            Optional.of(42)
              //.map(ThrowingFunctionTest::throwException) // Unhandled exception: java.io.IOException
              .map(unchecked(ThrowingFunctionTest::throwException));
        });
    }

    private static String throwException(Integer i) throws IOException {
        throw new IOException("whoopsie.");
    }
}
