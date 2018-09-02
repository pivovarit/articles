package com.pivovarit.collections

import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalTime

internal class CollectionsTest {

    @Test
    fun example_1() {

        val list = listOf(1, 2, 3, 4, 5)

        println("Collection:")

        logExecutionTimeOf {
            list
              .map(expensiveOperation())
              .map(anotherExpensiveOperation())
              .first()
        }
    }

    @Test
    fun example_1_sequence() {

        val list = listOf(1, 2, 3, 4, 5).asSequence()
        println("Sequence:")

        logExecutionTimeOf {
            list
              .map(expensiveOperation())
              .map(anotherExpensiveOperation())
              .first()
        }
    }

    private fun anotherExpensiveOperation(): (Int) -> Int {
        return {
            Thread.sleep(1000)
            it + 2
        }
    }

    private fun expensiveOperation(): (Int) -> Int {
        return {
            Thread.sleep(1000)
            it * 2
        }
    }
}

fun logExecutionTimeOf(task: () -> Unit) {
    val before = LocalTime.now()
    task.invoke()
    val after = LocalTime.now()
    println("Time taken: ${Duration.between(before, after).seconds} seconds")
}
