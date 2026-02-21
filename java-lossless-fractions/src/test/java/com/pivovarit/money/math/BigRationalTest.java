package com.pivovarit.money.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BigRationalTest {

    private static BigDecimal toBigDecimal(BigRational x, MathContext mc) {
        return new BigDecimal(x.numerator()).divide(new BigDecimal(x.denominator()), mc);
    }

    @Nested
    @DisplayName("Constructor normalization & invariants")
    class ConstructorNormalization {

        @Test
        void nullNumeratorThrows() {
            assertThatThrownBy(() -> new BigRational(null, BigInteger.valueOf(1)))
              .isInstanceOf(NullPointerException.class);
        }

        @Test
        void nullDenominatorThrows() {
            assertThatThrownBy(() -> new BigRational(BigInteger.valueOf(1), null))
              .isInstanceOf(NullPointerException.class);
        }

        @Test
        void zeroDenominatorThrows() {
            assertThatThrownBy(() -> new BigRational(BigInteger.valueOf(1), BigInteger.valueOf(0)))
              .isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("denominator");
        }

        @Test
        void denominatorAlwaysPositive_signMovesToNumerator() {
            BigRational x = new BigRational(BigInteger.valueOf(2), BigInteger.valueOf(-5));
            assertThat(x.numerator()).isEqualTo(BigInteger.valueOf(-2));
            assertThat(x.denominator()).isEqualTo(BigInteger.valueOf(5));
        }

        @Test
        void reducesByGcd_positive() {
            BigRational x = new BigRational(BigInteger.valueOf(6), BigInteger.valueOf(8));
            assertThat(x.numerator()).isEqualTo(BigInteger.valueOf(3));
            assertThat(x.denominator()).isEqualTo(BigInteger.valueOf(4));
        }

        @Test
        void reducesByGcd_negative() {
            BigRational x = new BigRational(BigInteger.valueOf(-6), BigInteger.valueOf(8));
            assertThat(x.numerator()).isEqualTo(BigInteger.valueOf(-3));
            assertThat(x.denominator()).isEqualTo(BigInteger.valueOf(4));
        }

        @Test
        void reducesByGcd_doubleNegative() {
            BigRational x = new BigRational(BigInteger.valueOf(-6), BigInteger.valueOf(-8));
            assertThat(x.numerator()).isEqualTo(BigInteger.valueOf(3));
            assertThat(x.denominator()).isEqualTo(BigInteger.valueOf(4));
        }

        @Test
        void zeroNormalizesTo0Over1() {
            BigRational x = new BigRational(BigInteger.ZERO, BigInteger.valueOf(999));
            assertThat(x.numerator()).isEqualTo(BigInteger.ZERO);
            assertThat(x.denominator()).isEqualTo(BigInteger.ONE);
        }

        @Test
        void alreadyNormalizedStaysSame() {
            BigRational x = new BigRational(BigInteger.valueOf(7), BigInteger.valueOf(13));
            assertThat(x.numerator()).isEqualTo(BigInteger.valueOf(7));
            assertThat(x.denominator()).isEqualTo(BigInteger.valueOf(13));
        }
    }

    @Nested
    @DisplayName("Equality / hashCode / canonicalization")
    class EqualityProps {

        @Test
        void equivalentFractionsBecomeEqualAfterNormalization() {
            BigRational a = BigRational.of(1, 2);
            BigRational b = BigRational.of(2, 4);
            BigRational c = BigRational.of(-3, -6);

            assertThat(a).isEqualTo(b).isEqualTo(c);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
            assertThat(b.hashCode()).isEqualTo(c.hashCode());
        }

        @Test
        void differentValuesNotEqual() {
            assertThat(BigRational.of(1, 2)).isNotEqualTo(BigRational.of(2, 3));
            assertThat(BigRational.of(-1, 2)).isNotEqualTo(BigRational.of(1, 2));
        }

        @Test
        void equalsConsistentWithCompareTo() {
            BigRational a = BigRational.of(2, 4);
            BigRational b = BigRational.of(1, 2);
            assertThat(a.compareTo(b)).isZero();
            assertThat(a).isEqualTo(b);
        }
    }

    @Nested
    @DisplayName("Ordering / compareTo")
    class Ordering {

        @Test
        void compareTo_basic() {
            assertThat(BigRational.of(1, 3)).isLessThan(BigRational.of(1, 2));
            assertThat(BigRational.of(2, 3)).isGreaterThan(BigRational.of(1, 2));
            assertThat(BigRational.of(2, 4)).isEqualByComparingTo(BigRational.of(1, 2));
        }

        @Test
        void compareTo_handlesNegatives() {
            assertThat(BigRational.of(-1, 2)).isLessThan(BigRational.of(0, 1));
            assertThat(BigRational.of(-1, 2)).isGreaterThan(BigRational.of(-2, 3)); // -0.5 > -0.666..
        }

        @Test
        void compareTo_transitivity_sample() {
            BigRational a = BigRational.of(-1, 2);
            BigRational b = BigRational.of(0, 1);
            BigRational c = BigRational.of(1, 2);

            assertThat(a).isLessThan(b);
            assertThat(b).isLessThan(c);
            assertThat(a).isLessThan(c);
        }
    }

    @Nested
    @DisplayName("Arithmetic: add/subtract/multiply/divide")
    class Arithmetic {

        @Test
        void add_sameDenominator() {
            assertThat(BigRational.of(1, 5).add(BigRational.of(2, 5))).isEqualTo(BigRational.of(3, 5));
        }

        @Test
        void add_differentDenominators() {
            assertThat(BigRational.of(1, 2).add(BigRational.of(1, 3))).isEqualTo(BigRational.of(5, 6));
        }

        @Test
        void add_withNegatives() {
            assertThat(BigRational.of(1, 2).add(BigRational.of(-1, 2))).isEqualTo(BigRational.of(0, 1));
            assertThat(BigRational.of(1, 3).add(BigRational.of(-1, 2))).isEqualTo(BigRational.of(-1, 6));
        }

        @Test
        void subtract_basic() {
            assertThat(BigRational.of(1, 2).subtract(BigRational.of(1, 3))).isEqualTo(BigRational.of(1, 6));
            assertThat(BigRational.of(1, 3).subtract(BigRational.of(1, 2))).isEqualTo(BigRational.of(-1, 6));
        }

        @Test
        void multiply_basicAndReduces() {
            assertThat(BigRational.of(1, 2).multiply(BigRational.of(1, 3))).isEqualTo(BigRational.of(1, 6));
            assertThat(BigRational.of(2, 3).multiply(BigRational.of(3, 4))).isEqualTo(BigRational.of(1, 2)); // reduces
            assertThat(BigRational.of(-1, 2).multiply(BigRational.of(1, 3))).isEqualTo(BigRational.of(-1, 6));
        }

        @Test
        void dividie_basic() {
            assertThat(BigRational.of(1, 2).divide(BigRational.of(3, 4))).isEqualTo(BigRational.of(2, 3));
        }

        @Test
        void dividie_negative() {
            assertThat(BigRational.of(1, 2).divide(BigRational.of(-3, 4))).isEqualTo(BigRational.of(-2, 3));
        }

        @Test
        void dividie_byZeroThrows() {
            assertThatThrownBy(() -> BigRational.of(1, 2).divide(BigRational.of(0, 1)))
              .isInstanceOf(ArithmeticException.class);
        }

        @Test
        void identities_additiveAndMultiplicative() {
            BigRational a = BigRational.of(7, 9);
            assertThat(a.add(BigRational.of(0, 1))).isEqualTo(a);
            assertThat(BigRational.of(0, 1).add(a)).isEqualTo(a);

            assertThat(a.multiply(BigRational.of(1, 1))).isEqualTo(a);
            assertThat(BigRational.of(1, 1).multiply(a)).isEqualTo(a);
        }

        @Test
        void distributiveLaw_sample() {
            BigRational a = BigRational.of(2, 5);
            BigRational b = BigRational.of(3, 7);
            BigRational c = BigRational.of(-11, 13);

            BigRational left = a.multiply(b.add(c));
            BigRational right = a.multiply(b).add(a.multiply(c));
            assertThat(left).isEqualTo(right);
        }

        @Test
        void divisionInverseProperty_sample_nonZero() {
            BigRational a = BigRational.of(7, 5);
            BigRational b = BigRational.of(-9, 2);

            BigRational res = a.divide(b).multiply(b);
            assertThat(res).isEqualTo(a);
        }
    }

    @Nested
    @DisplayName("inverse()")
    class Inverse {

        @Test
        void swapsNumeratorAndDenominator_forPositive() {
            BigRational x = BigRational.of(3, 7);

            BigRational inv = x.inverse();

            assertThat(inv.numerator()).isEqualTo(BigInteger.valueOf(7));
            assertThat(inv.denominator()).isEqualTo(BigInteger.valueOf(3));
        }

        @Test
        void normalizesSign_denominatorMustStayPositive() {
            // x = -3/7 => inverse should be -7/3 (NOT 7/-3)
            BigRational x = BigRational.of(-3, 7);

            BigRational inv = x.inverse();

            assertThat(inv.numerator()).isEqualTo(BigInteger.valueOf(-7));
            assertThat(inv.denominator()).isEqualTo(BigInteger.valueOf(3));
        }

        @Test
        void normalizesWhenSwapped_fractionIsReduced() {
            // x = 2/4 normalizes to 1/2; inverse should be 2/1 (normalized)
            BigRational x = BigRational.of(2, 4);

            BigRational inv = x.inverse();

            assertThat(inv).isEqualTo(BigRational.of(2, 1));
            assertThat(inv.numerator()).isEqualTo(BigInteger.valueOf(2));
            assertThat(inv.denominator()).isEqualTo(BigInteger.ONE);
        }

        @Test
        void inverseOfInverse_isOriginal_forNonZero() {
            BigRational x = BigRational.of(-11, 13);

            BigRational y = x.inverse().inverse();

            assertThat(y).isEqualTo(x);
        }

        @Test
        void multiplicationByInverse_isOne_forNonZero() {
            BigRational x = BigRational.of(-11, 13);

            BigRational prod = x.multiply(x.inverse());

            assertThat(prod).isEqualTo(BigRational.of(1, 1));
        }

        @Test
        void inverseOfZero_throws() {
            BigRational zero = BigRational.of(0, 7); // normalizes to 0/1

            assertThatThrownBy(zero::inverse)
              .isInstanceOf(ArithmeticException.class)
              .hasMessageContaining("zero");
        }
    }

    @Nested
    @DisplayName("negate / abs")
    class SignHelpers {

        @Test
        void negate_basic() {
            assertThat(BigRational.of(1, 2).negate()).isEqualTo(BigRational.of(-1, 2));
            assertThat(BigRational.of(-1, 2).negate()).isEqualTo(BigRational.of(1, 2));
            assertThat(BigRational.of(0, 3).negate()).isEqualTo(BigRational.of(0, 1));
        }

        @Test
        void abs_basic() {
            assertThat(BigRational.of(-1, 2).abs()).isEqualTo(BigRational.of(1, 2));
            assertThat(BigRational.of(1, 2).abs()).isEqualTo(BigRational.of(1, 2));
            assertThat(BigRational.of(0, 7).abs()).isEqualTo(BigRational.of(0, 1));
        }
    }

    @Nested
    @DisplayName("Integer conversions: floor/ceil/truncate")
    class IntegerConversions {

        @Test
        void floor_positive() {
            assertThat(BigRational.of(3, 2).floor()).isEqualTo(BigInteger.valueOf(1));
            assertThat(BigRational.of(2, 1).floor()).isEqualTo(BigInteger.valueOf(2));
            assertThat(BigRational.of(1, 2).floor()).isEqualTo(BigInteger.valueOf(0));
        }

        @Test
        void floor_negative() {
            assertThat(BigRational.of(-3, 2).floor()).isEqualTo(BigInteger.valueOf(-2));
            assertThat(BigRational.of(-1, 1).floor()).isEqualTo(BigInteger.valueOf(-1));
            assertThat(BigRational.of(-1, 2).floor()).isEqualTo(BigInteger.valueOf(-1));
        }

        @Test
        void ceil_positive() {
            assertThat(BigRational.of(3, 2).ceil()).isEqualTo(BigInteger.valueOf(2));
            assertThat(BigRational.of(2, 1).ceil()).isEqualTo(BigInteger.valueOf(2));
            assertThat(BigRational.of(1, 2).ceil()).isEqualTo(BigInteger.valueOf(1));
        }

        @Test
        void ceil_negative() {
            assertThat(BigRational.of(-3, 2).ceil()).isEqualTo(BigInteger.valueOf(-1));
            assertThat(BigRational.of(-1, 1).ceil()).isEqualTo(BigInteger.valueOf(-1));
            assertThat(BigRational.of(-1, 2).ceil()).isEqualTo(BigInteger.valueOf(0));
        }

        @Test
        void truncate_towardZero() {
            assertThat(BigRational.of(3, 2).truncate()).isEqualTo(BigInteger.valueOf(1));
            assertThat(BigRational.of(-3, 2).truncate()).isEqualTo(BigInteger.valueOf(-1));
            assertThat(BigRational.of(1, 2).truncate()).isEqualTo(BigInteger.valueOf(0));
            assertThat(BigRational.of(-1, 2).truncate()).isEqualTo(BigInteger.valueOf(0));
        }
    }

    @Nested
    @DisplayName("Rounding: half-up / half-even")
    class Rounding {

        @Test
        void roundHalfUp_examples() {
            assertThat(BigRational.of(3, 2).roundHalfUp()).isEqualTo(BigInteger.valueOf(2));   // 1.5 -> 2
            assertThat(BigRational.of(-3, 2)
              .roundHalfUp()).isEqualTo(BigInteger.valueOf(-2)); // -1.5 -> -2 (away from 0)
            assertThat(BigRational.of(4, 3).roundHalfUp()).isEqualTo(BigInteger.valueOf(1));   // 1.333 -> 1
            assertThat(BigRational.of(-4, 3).roundHalfUp()).isEqualTo(BigInteger.valueOf(-1)); // -1.333 -> -1
        }

        @Test
        void roundHalfEven_tiesToEven() {
            assertThat(BigRational.of(5, 2).roundHalfEven()).isEqualTo(BigInteger.valueOf(2));    // 2.5 -> 2
            assertThat(BigRational.of(7, 2).roundHalfEven()).isEqualTo(BigInteger.valueOf(4));    // 3.5 -> 4
            assertThat(BigRational.of(-5, 2).roundHalfEven()).isEqualTo(BigInteger.valueOf(-2));  // -2.5 -> -2
            assertThat(BigRational.of(-7, 2).roundHalfEven()).isEqualTo(BigInteger.valueOf(-4));  // -3.5 -> -4
        }
    }

    @Nested
    @DisplayName("BigDecimal conversion sanity (via components)")
    class BigDecimalConversion {

        @Test
        void terminating_exact_unlimited() {
            BigRational x = BigRational.of(1, 8);
            BigDecimal bd = toBigDecimal(x, MathContext.UNLIMITED);
            assertThat(bd).isEqualByComparingTo(new BigDecimal("0.125"));
        }

        @Test
        void nonTerminating_requiresContext() {
            BigRational x = BigRational.of(1, 3);
            BigDecimal bd = toBigDecimal(x, new MathContext(10, RoundingMode.HALF_UP));
            assertThat(bd).isEqualByComparingTo(new BigDecimal("0.3333333333"));
        }

        @Test
        void sign_isPreserved() {
            BigRational x = BigRational.of(-1, 4);
            BigDecimal bd = toBigDecimal(x, new MathContext(20));
            assertThat(bd).isEqualByComparingTo(new BigDecimal("-0.25"));
        }
    }

    @Nested
    @DisplayName("Large values / stress sanity")
    class LargeValues {

        @Test
        void constructor_handlesHugeNumbers_andReduces() {
            BigInteger n = new BigInteger("123456789012345678901234567890");
            BigInteger d = new BigInteger("987654321098765432109876543210");

            BigInteger g = n.gcd(d);
            BigInteger expectedN = n.divide(g);
            BigInteger expectedD = d.divide(g);

            BigRational x = new BigRational(n, d);

            assertThat(x.numerator()).isEqualTo(expectedN);
            assertThat(x.denominator()).isEqualTo(expectedD);

            assertThat(x.denominator().signum()).isPositive();
            assertThat(x.numerator().gcd(x.denominator())).isEqualTo(BigInteger.ONE);
        }

        @Test
        void arithmetic_largeNumbers_sanity() {
            BigInteger n1 = new BigInteger("99999999999999999999999999999");
            BigInteger d1 = new BigInteger("100000000000000000000000000000");
            BigInteger n2 = new BigInteger("88888888888888888888888888888");
            BigInteger d2 = new BigInteger("100000000000000000000000000000");

            BigRational a = BigRational.of(n1, d1);
            BigRational b = BigRational.of(n2, d2);
            BigRational sum = a.add(b);

            assertThat(sum).isGreaterThan(BigRational.of(188, 100));
            assertThat(sum).isLessThan(BigRational.of(189, 100));
        }
    }

    @Nested
    @DisplayName("of(BigDecimal)")
    class FromBigDecimal {

        @Test
        void integerBecomesOverOne() {
            assertThat(BigRational.of(new BigDecimal("42"))).isEqualTo(BigRational.of(42, 1));
            assertThat(BigRational.of(new BigDecimal("-42"))).isEqualTo(BigRational.of(-42, 1));
            assertThat(BigRational.of(new BigDecimal("0"))).isEqualTo(BigRational.of(0, 1));
        }

        @Test
        void simpleDecimal() {
            assertThat(BigRational.of(new BigDecimal("12.34"))).isEqualTo(BigRational.of(617, 50));
            assertThat(BigRational.of(new BigDecimal("-0.25"))).isEqualTo(BigRational.of(-1, 4));
            assertThat(BigRational.of(new BigDecimal("0.1"))).isEqualTo(BigRational.of(1, 10));
        }

        @Test
        void stripsTrailingZeros() {
            assertThat(BigRational.of(new BigDecimal("12.3400"))).isEqualTo(BigRational.of(617, 50));
            assertThat(BigRational.of(new BigDecimal("0.1000"))).isEqualTo(BigRational.of(1, 10));
            assertThat(BigRational.of(new BigDecimal("1000.00"))).isEqualTo(BigRational.of(1000, 1));
        }

        @Test
        void scientificNotation_positiveExponent_resultsInInteger() {
            assertThat(BigRational.of(new BigDecimal("1E+3"))).isEqualTo(BigRational.of(1000, 1));
            assertThat(BigRational.of(new BigDecimal("-12E+2"))).isEqualTo(BigRational.of(-1200, 1));
        }

        @Test
        void scientificNotation_negativeExponent() {
            assertThat(BigRational.of(new BigDecimal("1E-3"))).isEqualTo(BigRational.of(1, 1000));
            assertThat(BigRational.of(new BigDecimal("-25E-2"))).isEqualTo(BigRational.of(-1, 4));
        }

        @Test
        void preservesValue_roundTripViaBigDecimalString() {
            BigDecimal a = new BigDecimal("12345678901234567890.0000012300");
            BigRational r = BigRational.of(a);
            BigDecimal b = new BigDecimal(r.numerator()).divide(new BigDecimal(r.denominator()));
            assertThat(b).isEqualByComparingTo(a.stripTrailingZeros());
        }

        @Test
        void zeroWithScaleNormalizesToZeroOverOne() {
            assertThat(BigRational.of(new BigDecimal("0.00"))).isEqualTo(BigRational.of(0, 1));
            assertThat(BigRational.of(new BigDecimal("-0.000"))).isEqualTo(BigRational.of(0, 1));
        }
    }
}
