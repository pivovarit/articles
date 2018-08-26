package com.pivovarit.grouping;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImmutableCollectorsTest {

    @Test
    void example_1() {
        assertThatThrownBy(() -> Stream.of(42).collect(Collectors.toCollection(List::of)))
          .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void example_2() {
        var unmodifiableList = Stream.of(42)
          .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));

        assertThat(unmodifiableList).containsExactly(42);
        assertThatThrownBy(() -> unmodifiableList.add(5))
          .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void example_3() {
        var unmodifiableList = Stream.of(42)
          .collect(CustomUnmodifiableListCollector.toUnmodifiableList());

        assertThat(unmodifiableList).containsExactly(42);
        assertThatThrownBy(() -> unmodifiableList.add(5))
          .isInstanceOf(UnsupportedOperationException.class);
    }
}



