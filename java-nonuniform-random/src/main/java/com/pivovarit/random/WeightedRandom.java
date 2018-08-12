package com.pivovarit.random;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;

public class WeightedRandom {

    public static <T extends Weightable> Optional<T> draw(Set<T> items) {
        return items.stream()
          .min(comparing(e -> -Math.log(1 - ThreadLocalRandom.current().nextDouble()) / e.getWeight()));
    }

    public interface Weightable {
        int getWeight();
    }

    public static void main(String[] args) {
        Weightable i1 = () -> 1;
        var items = Set.of(i1, () -> 2);

        int draws = 100000000;
        var count = Stream.generate(() -> draw(items))
          .limit(draws)
          .filter(o -> o.orElse(null) == i1)
          .count();

        System.out.println("Picked i1 " + count + " times out of " + draws);
    }
}



