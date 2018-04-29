package com.pivovarit.collections;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class StreamTest {

    @Test
    void example_1() throws Exception {
        List<Integer> list = Arrays.asList(1, 2, 3);

        Optional<Integer> resultStream = list.stream()
          .map(i -> i * 2)
          .filter(i -> i > 1)
          .findAny();

        Optional<Integer> resultLoop = Optional.empty();
        for (Integer i : list) {
            Integer integer = i * 2;
            if (integer > 1) {
                resultLoop = Optional.of(integer);
                break;
            }
        }
    }
}
