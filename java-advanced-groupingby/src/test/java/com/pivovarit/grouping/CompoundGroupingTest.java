package com.pivovarit.grouping;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Function;

import static java.util.stream.Collectors.filtering;
import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

class CompoundGroupingTest {
    private List<String> strings = List.of("a", "bb", "cc", "ddd");

    @Test
    void example_1() {
        Map<Integer, TreeSet<String>> result = strings.stream()
          .collect(
            groupingBy(String::length,
              mapping(String::toUpperCase,
                filtering(s -> s.length() > 1,
                  toCollection(TreeSet::new)))));

        System.out.println(result);
    }

    @Test
    void example_2() {
        Map<Integer, String> result = strings.stream()
          .collect(
            groupingBy(String::length,
              mapping(toStringList(),
                flatMapping(s -> s.stream().distinct(),
                  filtering(s -> s.length() > 0,
                    mapping(String::toUpperCase,
                      reducing("", String::concat)))))));

        System.out.println(result);
    }

    private static Function<String, List<String>> toStringList() {
        return s -> s.chars()
          .mapToObj(c -> (char) c)
          .map(Object::toString)
          .collect(toList());
    }
}



