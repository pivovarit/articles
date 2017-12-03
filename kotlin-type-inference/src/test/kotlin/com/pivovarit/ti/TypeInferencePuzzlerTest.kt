package com.pivovarit.ti


fun main(args: Array<String>) {

    fun foo(int: Int?) = {
        println(int)
    }

    listOf(42)
      .forEach { foo(it) }
}
