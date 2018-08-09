package com.pivovarit.random;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * No, don't use that :)
 */
public class NaiveWeightedRandom {

    public static <T extends WeightedRandom.Weightable> Optional<T> draw(Set<T> items) {
        List<T> list = items.stream()
          .flatMap(item -> Stream.generate(() -> item).limit(item.getWeight()))
          .collect(Collectors.toList());

        return list.isEmpty()
          ? Optional.empty()
          : Optional.of(list.get(ThreadLocalRandom.current().nextInt(list.size())));
    }

    public interface Weightable {
        int getWeight();
    }
}



