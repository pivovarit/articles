package com.pivovarit.money.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BigRationalDivisionPrecisionTest {

    private static final int n = 200;

    private static final BigInteger THREE_BI = BigInteger.valueOf(3);
    private static final BigDecimal THREE_BD = BigDecimal.valueOf(3);

    @Nested
    class BigRationalTests {

        @Test
        void repeatedDivisionShouldBeExact() {
            BigRational r = BigRational.ONE;
            for (int i = 0; i < n; i++) {
                r = r.divide(3);
            }

            BigRational expected = BigRational.of(BigInteger.ONE, THREE_BI.pow(n));

            assertThat(r)
              .as("1 divided by 3, %s times should be exactly 1/3^%s", n, n)
              .isEqualTo(expected);
        }

        @Test
        void divideManyTimesThenMultiplyBackShouldReturnExactlyOne() {
            BigRational r = BigRational.ONE;
            for (int i = 0; i < n; i++) {
                r = r.divide(3);
            }

            BigRational factor = BigRational.of(THREE_BI.pow(n), BigInteger.ONE);
            BigRational back = r.multiply(factor);

            assertThat(back).isEqualTo(BigRational.ONE);
        }
    }

    @Nested
    class BigDecimalTests {

        @Test
        void divideManyTimesThenMultiplyBackShouldAccumulateRoundingError() {
            MathContext mc = new MathContext(20, RoundingMode.HALF_EVEN);

            BigDecimal x = BigDecimal.ONE;
            for (int i = 0; i < n; i++) {
                x = x.divide(THREE_BD, mc);
            }

            BigDecimal back = x.multiply(THREE_BD.pow(n));

            assertThat(back)
              .as("With repeated rounding, dividing by 3 %s times and multiplying back by 3^%s should drift", n, n)
              .isNotEqualByComparingTo(BigDecimal.ONE);
        }

        @Test
        void repeatedDivisionShouldDifferFromSingleDivisionAtFixedPrecision() {
            MathContext mc = new MathContext(20, RoundingMode.HALF_EVEN);

            BigDecimal repeated = BigDecimal.ONE;
            for (int i = 0; i < n; i++) {
                repeated = repeated.divide(THREE_BD, mc);
            }

            BigDecimal single = BigDecimal.ONE.divide(THREE_BD.pow(n), mc);

            assertThat(repeated)
              .as("Rounding at every step should generally differ from rounding once at the end")
              .isNotEqualByComparingTo(single);
        }
    }
}
