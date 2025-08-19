package com.pivovarit.collections

import org.junit.jupiter.api.Test
import java.util.Locale

internal class AntipatternsTest {

    @Test
    internal fun example_not_null() {
        val list = listOf(1, 2, 3, null)

        list
          .filterNotNull()
          .map { it.toString() }

        list
          .mapNotNull { it?.toString() }
    }

    @Test
    internal fun example_chained_maps() {
        val list = listOf(1, 2, 3)

        list
          .map { it.toString() }
          .map { it.uppercase(Locale.getDefault()) }

        list.map { it.toString().uppercase(Locale.getDefault()) }
    }

    @Test
    internal fun example_chained_map_flatmap() {
        val list = listOf("a", "bb", "ccc")

        list
          .map { it.toList() }
          .flatMap { it }

        list.flatMap { it.toList() }
    }

    @Test
    internal fun example_chained_filter_last() {
        val list = listOf(1, 2, 3)

        list
          .filter { it % 2 == 1 }
          .last()

        list.last { it % 2 == 1 }
    }

    @Test
    internal fun example_chained_map_first() {
        val list = listOf(1, 2, 3)

        list
          .map { it * 42 }
          .first()

        list
          .first()
          .let { it * 42 }
    }

    @Test
    internal fun example_chained_filter_count() {
        val list = listOf(1, 2, 3)

        list
          .filter { it % 2 == 1 }
          .count()

        list
          .count { it % 2 == 1 }
    }
}
