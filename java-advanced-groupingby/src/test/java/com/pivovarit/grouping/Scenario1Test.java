package com.pivovarit.grouping;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.filtering;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

class Scenario1Test {

    private List<String> strings = List.of("a", "bb", "cc", "ddd");

    @Test
    void standardGrouping() {
        Map<Integer, List<String>> result = strings.stream()
          .collect(groupingBy(String::length));

        System.out.println(result);
    }

    @Test
    void customMapImplementation() {
        TreeMap<Integer, List<String>> result = strings.stream()
          .collect(groupingBy(String::length, TreeMap::new, toList()));

        System.out.println(result);
    }

    @Test
    void customAggregateImplementation() {
        Map<Integer, TreeSet<String>> result = strings.stream()
          .collect(groupingBy(String::length, toCollection(TreeSet::new)));

        System.out.println(result);
    }

    @Test
    void customAggregation_counting() {
        Map<Integer, Long> result = strings.stream()
          .collect(groupingBy(String::length, counting()));

        System.out.println(result);
    }

    @Test
    void customAggregation_filtering() {
        Map<Integer, List<String>> result = strings.stream()
          .collect(groupingBy(String::length, filtering(s -> !s.contains("c"), toList())));

        System.out.println(result);
    }

    @Test
    void customAggregation_joining() {
        Map<Integer, String> result = strings.stream()
          .collect(groupingBy(String::length, joining(",", "[", "]")));

        System.out.println(result);
    }

    private static Function<String, List<Character>> toStringList() {
        return s -> s.chars().mapToObj(c -> (char) c).collect(toList());
    }
}



