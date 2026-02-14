package com.pivovarit.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilsTest {

    @Test
    void shouldReverseString() {
        assertThat(StringUtils.reverse("hello")).isEqualTo("olleh");
    }

    @Test
    void shouldReturnNullForNullReverse() {
        assertThat(StringUtils.reverse(null)).isNull();
    }

    @Test
    void shouldDetectPalindrome() {
        assertThat(StringUtils.isPalindrome("racecar")).isTrue();
    }

    @Test
    void shouldDetectNonPalindrome() {
        assertThat(StringUtils.isPalindrome("hello")).isFalse();
    }
}
