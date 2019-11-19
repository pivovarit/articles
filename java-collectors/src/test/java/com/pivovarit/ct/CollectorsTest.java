package com.pivovarit.ct;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollectorsTest {

    @Test
    void E_toList() {
        List<Integer> list = List.of(1, 2, 3);

        List<Integer> result = list.stream()
          .collect(toList());

        assertThat(result)
          .hasSize(3)
          .containsOnly(1, 2, 3);
    }

    @Test
    void E_toUnmodifiableList() {
        List<Integer> list = List.of(1, 2, 3);

        List<Integer> result = list.stream()
          .collect(toUnmodifiableList());

        assertThat(result)
          .hasSize(3)
          .containsOnly(1, 2, 3);

        assertThatThrownBy(() -> result.add(42))
          .isExactlyInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void E_toSet() {
        List<Integer> list = List.of(1, 2, 3, 3);

        Set<Integer> result = list.stream()
          .collect(toSet());

        assertThat(result)
          .hasSize(3)
          .containsOnly(1, 2, 3);
    }

    @Test
    void E_toUnmodifiableSet() {
        List<Integer> list = List.of(1, 2, 3, 3);

        Set<Integer> result = list.stream()
          .collect(toUnmodifiableSet());

        assertThat(result)
          .hasSize(3)
          .containsOnly(1, 2, 3);

        assertThatThrownBy(() -> result.add(42))
          .isExactlyInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void E_toCollection() {
        List<Integer> list = List.of(1, 2, 3);

        Collection<Integer> result = list.stream()
          .collect(toCollection(LinkedList::new));

        assertThat(result)
          .isInstanceOf(LinkedList.class)
          .hasSize(3)
          .containsOnly(1, 2, 3);
    }

    @Test
    void E_toMap() {
        List<String> list = List.of("one", "two", "three");

        Map<String, Integer> result = list.stream()
          .collect(toMap(e -> e, e -> e.length()));

        assertThat(result)
          .hasSize(3)
          .containsEntry("one", 3)
          .containsEntry("two", 3)
          .containsEntry("three", 5);
    }

    @Test
    void E_toMap_conflict_resolution() {
        List<String> list = List.of("one", "two", "three");

        Map<Integer, String> result = list.stream()
          .collect(toMap(String::length, e -> e, String::concat));

        assertThat(result)
          .hasSize(2)
          .containsEntry(3, "onetwo")
          .containsEntry(5, "three");
    }

    @Test
    void E_toMap_conflict_resolution_custom_map() {
        List<String> list = List.of("one", "two", "three");

        Map<Integer, String> result = list.stream()
          .collect(toMap(String::length, e -> e, String::concat, TreeMap::new));

        assertThat(result)
          .isExactlyInstanceOf(TreeMap.class)
          .hasSize(2)
          .containsEntry(3, "onetwo")
          .containsEntry(5, "three");
    }

    @Test
    void E_collectingAndThen() {
        List<String> list = List.of("one", "two", "three");

        List<String> result = list.stream()
          .collect(collectingAndThen(toList(), Collections::unmodifiableList));

        assertThat(result)
          .hasSize(3)
          .containsExactly("one", "two", "three");

        assertThatThrownBy(() -> result.add(""))
          .isExactlyInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void E_joining() {
        List<String> list = List.of("one", "two", "three");

        String result = list.stream()

          .collect(Collectors.joining());

        assertThat(result).isEqualTo("onetwothree");
    }

    @Test
    void E_joining_separator() {
        List<String> list = List.of("one", "two", "three");

        String result = list.stream()
          .collect(Collectors.joining(","));

        assertThat(result).isEqualTo("one,two,three");
    }

    @Test
    void E_joining_separator_prefix_suffix() {
        List<String> list = List.of("one", "two", "three");

        String result = list.stream()
          .collect(Collectors.joining(",", "[", "]"));

        assertThat(result).isEqualTo("[one,two,three]");
    }

    @Test
    void E_partitioningBy() {
        List<String> list = List.of("one", "two", "three");

        Map<Boolean, List<String>> result = list.stream()
          .collect(partitioningBy(i -> i.length() == 3));

        assertThat(result)
          .hasSize(2)
          .containsEntry(true, List.of("one", "two"))
          .containsEntry(false, List.of("three"));
    }

    @Test
    void E_partitioningBy_downstream() {
        List<String> list = List.of("one", "two", "three");

        Map<Boolean, Set<String>> result = list.stream()
          .collect(partitioningBy(i -> i.length() == 3, toSet()));

        assertThat(result)
          .hasSize(2)
          .containsEntry(true, Set.of("one", "two"));
    }
}
