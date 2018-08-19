package com.pivovarit.random;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.counting;

public class WeightedRandom {

    public static <T extends Weightable> Optional<T> draw(Set<T> items) {
        return items.stream()
          .min(comparing(e -> -Math.log(ThreadLocalRandom.current().nextDouble()) / e.getWeight()));
    }

    public interface Weightable {
        int getWeight();
    }

    public static void main(String[] args) {
        var items = Set.of(
          new Item("i1", 1),
          new Item("i2", 1),
          new Item("i3", 1),
          new Item("i4", 1)
        );

        var results = Stream.generate(() -> draw(items))
          .limit(10000000)
          .flatMap(Optional::stream)
          .collect(Collectors.groupingBy(i -> i.name, counting()));

        System.out.println(results);
    }

    private static class Item implements Weightable {

        private final String name;
        private final int weight;

        public Item(String name, int weight) {
            this.name = name;
            this.weight = weight;
        }

        @Override
        public int getWeight() {
            return weight;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Item item = (Item) o;
            return weight == item.weight &&
              Objects.equals(name, item.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, weight);
        }

        @Override
        public String toString() {
            return String.format("name='%s'}", name);
        }
    }
}



