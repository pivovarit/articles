package com.pivovarit.hamming.domain

internal fun Int.isPowerOfTwo() = this != 0 && this and this - 1 == 0