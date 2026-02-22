package com.pivovarit.money.math;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import org.junit.jupiter.api.Test;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TEN;
import static org.assertj.core.api.Assertions.assertThat;

class BigRationalToBigDecimalWithLossTest {

    @Test
    void shouldReturnZeroLossWhenExactAtScale() {
        // 1/8 = 0.125 exactly at scale 3
        var r = BigRational.of(1, 8);

        var res = r.toDecimal(3, Rounding.HALF_UP);

        assertThat(res.value()).isEqualByComparingTo(new BigDecimal("0.125"));
        assertThat(res.residual()).isEqualTo(BigRational.ZERO);
    }

    @Test
    void shouldReturnPositiveLossForFloorWhenRoundsDownPositive() {
        // 1/3 -> 0.33 (scale 2) ; exact - rounded = 1/3 - 33/100 = 1/300
        var r = BigRational.of(1, 3);

        var res = r.toDecimal(2, Rounding.FLOOR);

        assertThat(res.value()).isEqualByComparingTo(new BigDecimal("0.33"));
        assertThat(res.residual()).isEqualTo(BigRational.of(1, 300));
        assertThat(res.residual().signum()).isPositive();
    }

    @Test
    void shouldReturnNegativeLossForCeilWhenRoundsUpPositive() {
        // 1/3 -> 0.34 (scale 2, CEIL) ; exact - rounded = 1/3 - 34/100 = -1/150
        var r = BigRational.of(1, 3);

        var res = r.toDecimal(2, Rounding.CEIL);

        assertThat(res.value()).isEqualByComparingTo(new BigDecimal("0.34"));
        assertThat(res.residual()).isEqualTo(BigRational.of(-1, 150));
        assertThat(res.residual().signum()).isNegative();
    }

    @Test
    void floorAndCeilShouldBeMathematicalForNegativeNumbers() {
        // -1/3 at scale 2:
        // FLOOR (towards -inf) => -0.34 ; loss = -1/3 - (-34/100) = 1/150 (positive)
        // CEIL (towards +inf)  => -0.33 ; loss = -1/3 - (-33/100) = -1/300 (negative)
        var r = BigRational.of(-1, 3);

        var floor = r.toDecimal(2, Rounding.FLOOR);
        assertThat(floor.value()).isEqualByComparingTo(new BigDecimal("-0.34"));
        assertThat(floor.residual()).isEqualTo(BigRational.of(1, 150));
        assertThat(floor.residual().signum()).isPositive();

        var ceil = r.toDecimal(2, Rounding.CEIL);
        assertThat(ceil.value()).isEqualByComparingTo(new BigDecimal("-0.33"));
        assertThat(ceil.residual()).isEqualTo(BigRational.of(-1, 300));
        assertThat(ceil.residual().signum()).isNegative();
    }

    @Test
    void halfUpShouldRoundHalfAwayFromZeroAndExposeResidual() {
        // 1/8 = 0.125 at scale 2 is exactly halfway between 0.12 and 0.13
        // HALF_UP => 0.13 ; loss = 1/8 - 13/100 = -1/200
        var r = BigRational.of(1, 8);

        var res = r.toDecimal(2, Rounding.HALF_UP);

        assertThat(res.value()).isEqualByComparingTo(new BigDecimal("0.13"));
        assertThat(res.residual()).isEqualTo(BigRational.of(-1, 200));
    }

    @Test
    void halfEvenShouldRoundHalfToEvenAndExposeResidual() {
        // 1/8 = 0.125 at scale 2: tie between 0.12 and 0.13
        // HALF_EVEN => 0.12 (since 12 is even) ; loss = 1/8 - 12/100 = 1/200
        var r = BigRational.of(1, 8);

        var res = r.toDecimal(2, Rounding.HALF_EVEN);

        assertThat(res.value()).isEqualByComparingTo(new BigDecimal("0.12"));
        assertThat(res.residual()).isEqualTo(BigRational.of(1, 200));
    }

    @Test
    void halfEvenShouldRoundHalfToEvenForNegativeNumbersToo() {
        // -1/8 = -0.125 at scale 2: tie between -0.12 and -0.13
        // HALF_EVEN => -0.12 (since 12 even) ; loss = -1/8 - (-12/100) = -1/200
        var r = BigRational.of(-1, 8);

        var res = r.toDecimal(2, Rounding.HALF_EVEN);

        assertThat(res.value()).isEqualByComparingTo(new BigDecimal("-0.12"));
        assertThat(res.residual()).isEqualTo(BigRational.of(-1, 200));
    }

    @Test
    void mathContextVariantShouldReturnValueAndLoss() {
        var r = BigRational.of(1, 3);
        var mc = new MathContext(3, RoundingMode.HALF_UP); // ~0.333

        var res = r.toDecimal(mc);

        assertThat(res.value()).isEqualByComparingTo(new BigDecimal("0.333"));
        // exact - 333/1000 = 1/3 - 333/1000 = 1/3000
        assertThat(res.residual()).isEqualTo(BigRational.of(1, 3000));
    }

    @Test
    void roundingLossShouldAlwaysEqualOriginalMinusReturnedValueAsRational() {
        // property-style check for a few modes on one input
        var r = BigRational.of(22, 7);

        for (var mode : Rounding.values()) {
            var res = r.toDecimal(4, mode);

            var v = res.value();
            var asRational = bigDecimalToRationalExact(v);

            assertThat(res.residual())
              .as("loss must be exact: original - returnedValue")
              .isEqualTo(r.subtract(asRational));
        }
    }

    private static BigRational bigDecimalToRationalExact(BigDecimal bd) {
        var unscaled = bd.unscaledValue();
        int scale = bd.scale();

        return scale >= 0
          ? BigRational.of(unscaled, TEN.pow(scale))
          : BigRational.of(unscaled.multiply(TEN.pow(-scale)), ONE);
    }
}
