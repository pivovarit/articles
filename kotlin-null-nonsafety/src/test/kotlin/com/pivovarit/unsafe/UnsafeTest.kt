package com.pivovarit.unsafe

import com.google.gson.Gson
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import sun.misc.Unsafe

data class Foo(val nonNullable: String)

class UnsafeTest {

    companion object {
        private fun getUnsafe(): Unsafe =
          Unsafe::class.java.getDeclaredField("theUnsafe")
                .apply { isAccessible = true }
                .let { it.get(null) as Unsafe }
    }

    private val unsafe = getUnsafe()

    @Test()
    fun unsafe_0() {
        assertThatThrownBy { Unsafe.getUnsafe() }
          .isInstanceOf(SecurityException::class.java)

    }

    @Test
    fun unsafe_1() {
        val foo = unsafe
          .allocateInstance(Foo::class.java) as Foo

        assertThat(foo.nonNullable).isNull()
    }

    @Test
    fun unsafe_2() {
        val foo = Gson().fromJson("{}", Foo::class.java)

        assertThat(foo.nonNullable).isNull()
    }
}
