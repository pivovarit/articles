package com.pivovarit.money.math;

import java.math.BigInteger;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BigRationalOfStringTest {

    @Nested
    class Integers {

        @Test
        void parsesPositiveInteger() {
            assertThat(BigRational.of("100"))
                .isEqualTo(BigRational.of(100));
        }

        @Test
        void parsesNegativeInteger() {
            assertThat(BigRational.of("-42"))
                .isEqualTo(BigRational.of(-42));
        }

        @Test
        void parsesWithWhitespace() {
            assertThat(BigRational.of("  100  "))
                .isEqualTo(BigRational.of(100));
        }

        @Test
        void normalizesNegativeDenominatorInFraction() {
            assertThat(BigRational.of("1/-2")).isEqualTo(BigRational.of(BigInteger.valueOf(-1), BigInteger.valueOf(2)));
        }
    }

    @Nested
    class Decimals {

        @Test
        void parsesDecimal() {
            assertThat(BigRational.of("100.25"))
                .isEqualTo(BigRational.of(BigInteger.valueOf(401), BigInteger.valueOf(4)));
        }

        @Test
        void parsesDecimalWithTrailingZeros() {
            assertThat(BigRational.of("100.50"))
                .isEqualTo(BigRational.of(BigInteger.valueOf(201), BigInteger.valueOf(2)));
        }

        @Test
        void parsesNegativeDecimal() {
            assertThat(BigRational.of("-0.5"))
                .isEqualTo(BigRational.of(BigInteger.valueOf(-1), BigInteger.valueOf(2)));
        }

        @Test
        void rejectsLeadingDotDecimal() {
            assertThatThrownBy(() -> BigRational.of(".5"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class Fractions {

        @Test
        void parsesIntegerFraction() {
            assertThat(BigRational.of("100/2"))
                .isEqualTo(BigRational.of(50));
        }

        @Test
        void parsesDecimalOverIntegerFraction() {
            // 100.50/2 = (201/2)/2 = 201/4
            assertThat(BigRational.of("100.50/2"))
                .isEqualTo(BigRational.of(BigInteger.valueOf(201), BigInteger.valueOf(4)));
        }

        @Test
        void parsesIntegerOverDecimalFraction() {
            // 1/0.5 = 2
            assertThat(BigRational.of("1/0.5"))
                .isEqualTo(BigRational.of(2));
        }

        @Test
        void parsesWithWhitespaceAroundSlash() {
            assertThat(BigRational.of(" 100.50 /  2 "))
                .isEqualTo(BigRational.of(BigInteger.valueOf(201), BigInteger.valueOf(4)));
        }

        @Test
        void rejectsDivisionByZeroInteger() {
            assertThatThrownBy(() -> BigRational.of("1/0"))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("division by zero");
        }

        @Test
        void rejectsDivisionByZeroDecimal() {
            assertThatThrownBy(() -> BigRational.of("1/0.0"))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("division by zero");
        }
    }

    @Nested
    class Validation {

        @Test
        void rejectsEmptyString() {
            assertThatThrownBy(() -> BigRational.of(""))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejectsBlankString() {
            assertThatThrownBy(() -> BigRational.of("   "))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejectsCommas() {
            assertThatThrownBy(() -> BigRational.of("1,000"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("commas");
        }

        @Test
        void rejectsScientificNotationLowercaseE() {
            assertThatThrownBy(() -> BigRational.of("1e3"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("scientific notation");
        }

        @Test
        void rejectsScientificNotationUppercaseE() {
            assertThatThrownBy(() -> BigRational.of("1E3"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("scientific notation");
        }

        @Test
        void rejectsMultipleSlashes() {
            assertThatThrownBy(() -> BigRational.of("1/2/3"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("too many");
        }

        @Test
        void rejectsMissingNumerator() {
            assertThatThrownBy(() -> BigRational.of("/2"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing numerator/denominator");
        }

        @Test
        void rejectsMissingDenominator() {
            assertThatThrownBy(() -> BigRational.of("2/"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing numerator/denominator");
        }

        @Test
        void rejectsGarbageInput() {
            assertThatThrownBy(() -> BigRational.of("abc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid number");
        }

        @Test
        void rejectsDecimalWithMultipleDots() {
            assertThatThrownBy(() -> BigRational.of("1.2.3"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid number");
        }

        @Test
        void rejectsPlusSignOnly() {
            assertThatThrownBy(() -> BigRational.of("+"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
