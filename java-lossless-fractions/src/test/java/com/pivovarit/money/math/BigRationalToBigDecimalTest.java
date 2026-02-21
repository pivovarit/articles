package com.pivovarit.money.math;

import java.math.MathContext;
import java.math.RoundingMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BigRationalToBigDecimalTest {

    @Nested
    @DisplayName("toBigDecimal(int scale, Rounding)")
    class ToBigDecimalWithScale {

        @Test
        void shouldConvertExactWhenTerminating_decimal() {
            var r = BigRational.of(1, 4);

            assertThat(r.toBigDecimal(2, Rounding.FLOOR)).isEqualByComparingTo("0.25");
            assertThat(r.toBigDecimal(2, Rounding.CEIL)).isEqualByComparingTo("0.25");
            assertThat(r.toBigDecimal(2, Rounding.HALF_UP)).isEqualByComparingTo("0.25");
            assertThat(r.toBigDecimal(2, Rounding.HALF_EVEN)).isEqualByComparingTo("0.25");
        }

        @Test
        void shouldRoundPositive_floorAndCeil() {
            var r = BigRational.of(1, 3);

            assertThat(r.toBigDecimal(2, Rounding.FLOOR)).isEqualByComparingTo("0.33");
            assertThat(r.toBigDecimal(2, Rounding.CEIL)).isEqualByComparingTo("0.34");
        }

        @Test
        void shouldRoundNegativeFloorAndCeilMathematically() {
            var r = BigRational.of(-1, 3);

            assertThat(r.toBigDecimal(2, Rounding.FLOOR)).isEqualByComparingTo("-0.34");
            assertThat(r.toBigDecimal(2, Rounding.CEIL)).isEqualByComparingTo("-0.33");
        }

        @Test
        void shouldRoundHalfUp_atHalf() {
            var r = BigRational.of(201, 200);

            assertThat(r.toBigDecimal(2, Rounding.HALF_UP)).isEqualByComparingTo("1.01");
        }

        @Test
        void shouldRoundHalfEven_tieGoesToEven() {
            var r = BigRational.of(201, 200);

            assertThat(r.toBigDecimal(2, Rounding.HALF_EVEN)).isEqualByComparingTo("1.00");
        }

        @Test
        void shouldThrowWhenScaleNegative() {
            var r = BigRational.of(1, 3);

            assertThatThrownBy(() -> r.toBigDecimal(-1, Rounding.HALF_UP))
              .isInstanceOf(ArithmeticException.class);
        }

        @Test
        void shouldReturnZeroForZero() {
            var r = BigRational.ZERO;

            assertThat(r.toBigDecimal(5, Rounding.HALF_UP)).isEqualByComparingTo("0.00000");
        }
    }

    @Nested
    @DisplayName("toBigDecimal(MathContext)")
    class ToBigDecimalWithMathContext {

        @Test
        void shouldRespectPrecisionAndRoundingMode() {
            var r = BigRational.of(1, 3);
            var mc = new MathContext(2, RoundingMode.HALF_UP);

            assertThat(r.toBigDecimal(mc)).isEqualByComparingTo("0.33");
        }

        @Test
        void shouldWorkWithHighPrecision() {
            var r = BigRational.of(1, 3);
            var mc = new MathContext(10, RoundingMode.HALF_UP);

            assertThat(r.toBigDecimal(mc)).isEqualByComparingTo("0.3333333333");
        }

        @Test
        void shouldReturnExactForTerminatingEvenWithMathContext() {
            var r = BigRational.of(1, 8);
            var mc = new MathContext(20, RoundingMode.HALF_EVEN);

            assertThat(r.toBigDecimal(mc)).isEqualByComparingTo("0.125");
        }
    }
}
