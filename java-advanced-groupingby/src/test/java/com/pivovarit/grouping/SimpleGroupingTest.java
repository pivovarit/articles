package com.pivovarit.grouping;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.averagingInt;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.filtering;
import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.summarizingInt;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

class SimpleGroupingTest {

    @Test
    void standardGrouping() {
        List<String> strings = List.of("a", "bb", "cc", "ddd");

        Map<Integer, List<String>> result = strings.stream()
          .collect(groupingBy(String::length));

        System.out.println(result);
    }

    @Test
    void customMapImplementation() {
        List<String> strings = List.of("a", "bb", "cc", "ddd");

        TreeMap<Integer, List<String>> result = strings.stream()
          .collect(groupingBy(String::length, TreeMap::new, toList()));

        System.out.println(result);
    }

    @Test
    void customAggregateImplementation() {
        List<String> strings = List.of("a", "bb", "cc", "ddd");

        Map<Integer, TreeSet<String>> result = strings.stream()
          .collect(groupingBy(String::length, toCollection(TreeSet::new)));

        System.out.println(result);
    }

    @Test
    void customAggregation_counting() {
        List<String> strings = List.of("a", "bb", "cc", "ddd");

        Map<Integer, Long> result = strings.stream()
          .collect(groupingBy(String::length, counting()));

        System.out.println(result);
    }

    @Test
    void customAggregation_filtering() {
        List<String> strings = List.of("a", "bb", "cc", "ddd");

        Map<Integer, List<String>> result = strings.stream()
          .collect(groupingBy(String::length, filtering(s -> !s.contains("c"), toList())));

        System.out.println(result);
    }

    @Test
    void customAggregation_joining() {
        List<String> strings = List.of("a", "bb", "cc", "ddd");

        Map<Integer, String> result = strings.stream()
          .collect(groupingBy(String::length, joining(",", "[", "]")));

        System.out.println(result); // {1=[a], 2=[bb,cc], 3=[ddd]}
    }

    @Test
    void customAggregation_averaging() {
        List<String> strings = List.of("a", "bb", "cc", "ddd");

        Map<Integer, Double> result = strings.stream()
          .collect(groupingBy(String::length, averagingInt(String::hashCode)));

        System.out.println(result);
    }

    @Test
    void customAggregation_summarizing() {
        List<String> strings = List.of("a", "bb", "cc", "ddd");

        Map<Integer, IntSummaryStatistics> result = strings.stream()
          .collect(groupingBy(String::length, summarizingInt(String::hashCode)));

        System.out.println(result);
    }

    @Test
    void customAggregation_flatmapping() {
        List<String> strings = List.of("a", "bb", "cc", "ddd");

        Map<Integer, List<Character>> result = strings.stream()
          .map(toStringList())
          .collect(groupingBy(List::size, flatMapping(Collection::stream, Collectors.toList())));

        System.out.println(result);
    }

    @Test
    void customAggregation_mapping() {
        List<String> strings = List.of("a", "bb", "cc", "ddd");

        Map<Integer, List<String>> result = strings.stream()
          .collect(groupingBy(String::length, Collectors.mapping(String::toUpperCase, Collectors.toList())));

        System.out.println(result);
    }

    @Test
    void customAggregation_reducing() {
        List<String> strings = List.of("a", "bb", "cc", "ddd");

        Map<Integer, List<Character>> result = strings.stream()
          .map(toStringList())
          .collect(groupingBy(List::size, reducing(List.of(), (l1, l2) -> Stream.concat(l1.stream(), l2.stream())
            .collect(Collectors.toList()))));

        System.out.println(result);
    }

    @Test
    void customAggregation_reducing_optional() {
        List<String> strings = List.of("a", "bb", "cc", "ddd");

        Map<Integer, Optional<List<Character>>> result = strings.stream()
          .map(toStringList())
          .collect(groupingBy(List::size, reducing((l1, l2) -> Stream.concat(l1.stream(), l2.stream()).collect(Collectors.toList()))));

        System.out.println(result);
    }

    @Test
    void customAggregation_summing() {
        List<String> strings = List.of("a", "bb", "cc", "ddd");

        Map<Integer, Integer> result = strings.stream()
          .collect(groupingBy(String::length, summingInt(String::hashCode)));

        System.out.println(result);
    }

    @Test
    void customAggregation_max() {
        List<String> strings = List.of("a", "bb", "cc", "ddd");

        Map<Integer, Optional<String>> result = strings.stream()
          .collect(groupingBy(String::length, Collectors.maxBy(Comparator.comparing(String::toUpperCase))));

        System.out.println(result);
    }

    @Test
    void customAggregation_min() {
        List<String> strings = List.of("a", "bb", "cc", "ddd");

        Map<Integer, Optional<String>> result = strings.stream()
          .collect(groupingBy(String::length, Collectors.minBy(Comparator.comparing(String::toUpperCase))));

        System.out.println(result);
    }

    private static Function<String, List<Character>> toStringList() {
        return s -> s.chars().mapToObj(c -> (char) c).collect(toList());
    }
}



