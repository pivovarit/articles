package com.pivovarit.parallel;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class ParallelCollectorsTest {
    private static final List<Integer> integers = IntStream.range(0, 100)
      .boxed()
      .collect(Collectors.toList());
}
