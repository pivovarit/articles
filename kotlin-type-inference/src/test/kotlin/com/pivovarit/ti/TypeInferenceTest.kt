package com.pivovarit.ti

import org.junit.jupiter.api.Test

open class FooService {
    fun foo(int: Int?) = {
        println(int)
    }

    fun foo2(int: Int?) {
        println(int)
    }
}

class TypeInferenceTest {

    private val service = FooService()

    @Test
    fun shouldNotCall() {
        listOf(42)
          .forEach { service.foo(it) }
    }

    @Test
    fun shouldCall() {
        listOf(42)
          .forEach { service.foo2(it) }
    }
}
