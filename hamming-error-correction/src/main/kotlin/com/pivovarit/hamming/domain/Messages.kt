package com.pivovarit.hamming.domain

data class EncodedString(val value: String) {
    operator fun get(index: Int): Char = value[index]
    val length get() = value.length
}

data class BinaryString(val value: String) {
    operator fun get(index: Int): Char = value[index]
    val length get() = value.length
}