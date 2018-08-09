package com.pivovarit.random;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Comparator.comparing;

public class WeightedRandom {

    public static <T extends Weightable> Optional<T> draw(Set<T> items) {
        return items.stream()
          .min(comparing(e -> -Math.log(1 - ThreadLocalRandom.current().nextDouble()) / e.getWeight()));
    }

    public interface Weightable {
        int getWeight();
    }
}



