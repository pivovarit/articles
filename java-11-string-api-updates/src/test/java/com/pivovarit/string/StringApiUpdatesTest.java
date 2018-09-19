package com.pivovarit.string;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringApiUpdatesTest {

    @Test
    void shouldRepeat() {
        var string = "foo bar ";

        var result = string.repeat(2);

        assertThat(result).isEqualTo(string + string);
    }

    @Test
    void shouldRepeatEmpty() {
        var string = "";

        var result = string.repeat(Integer.MAX_VALUE);

        assertThat(result).isEqualTo("");
    }

    @Test
    void shouldRepeatZeroTimes() {
        var string = "foo";

        var result = string.repeat(0);

        assertThat(result).isEqualTo("");
    }

    @Test
    void shouldCheckIfBlank() {
        assertThat(" ".isBlank()).isTrue();
    }

    @Test
    void shouldStrip() {
        assertThat(" f oo  ".strip()).isEqualTo("f oo");
        assertThat("  foo  ".stripLeading()).isEqualTo("foo  ");
        assertThat("  foo  ".stripTrailing()).isEqualTo("  foo");
    }

    @Test
    void shouldStreamLines() {
        "foo\nbar".lines().forEach(System.out::println);
    }
}
