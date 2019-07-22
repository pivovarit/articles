package com.pivovarit.collectors;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollectorsTest {

    @Test
    void toList() {
        List<Integer> list = List.of(1, 2, 3);

        List<Integer> result = list.stream()
          .collect(Collectors.toList());

        assertThat(result)
          .hasSize(3)
          .containsOnly(1, 2, 3);
    }

    @Test
    void toUnmodifiableList() {
        List<Integer> list = List.of(1, 2, 3);

        List<Integer> result = list.stream()
          .collect(Collectors.toUnmodifiableList());

        assertThat(result)
          .hasSize(3)
          .containsOnly(1, 2, 3);

        assertThatThrownBy(() -> result.add(42))
          .isExactlyInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void toSet() {
        List<Integer> list = List.of(1, 2, 3, 3);

        Set<Integer> result = list.stream()
          .collect(Collectors.toSet());

        assertThat(result)
          .hasSize(3)
          .containsOnly(1, 2, 3);
    }

    @Test
    void toUnmodifiableSet() {
        List<Integer> list = List.of(1, 2, 3, 3);

        Set<Integer> result = list.stream()
          .collect(Collectors.toUnmodifiableSet());

        assertThat(result)
          .hasSize(3)
          .containsOnly(1, 2, 3);

        assertThatThrownBy(() -> result.add(42))
          .isExactlyInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void toCollection() {
        List<Integer> list = List.of(1, 2, 3);

        Collection<Integer> result = list.stream()
          .collect(Collectors.toCollection(LinkedList::new));

        assertThat(result)
          .isInstanceOf(LinkedList.class)
          .hasSize(3)
          .containsOnly(1, 2, 3);
    }


}
