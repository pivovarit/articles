package com.pivovarit.money.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Objects;

// inspired by https://introcs.cs.princeton.edu/java/92symbolic/BigRational.java.html and org.apache.commons.math3
public record BigRational(BigInteger numerator, BigInteger denominator) implements Comparable<BigRational> {

    public BigRational {
        Objects.requireNonNull(numerator, "numerator");
        Objects.requireNonNull(denominator, "denominator");

        if (denominator.signum() == 0) {
            throw new IllegalArgumentException("denominator must not be 0");
        } else if (denominator.signum() < 0) {
            numerator = numerator.negate();
            denominator = denominator.negate();
        }

        var g = numerator.gcd(denominator);
        numerator = numerator.divide(g);
        denominator = denominator.divide(g);
    }

    public static final BigRational ZERO = new BigRational(BigInteger.ZERO, BigInteger.ONE);
    public static final BigRational ONE = new BigRational(BigInteger.ONE, BigInteger.ONE);

    public static BigRational of(String s) {
        return BigRationalParser.of(s);
    }

    public static BigRational of(long numerator, long denominator) {
        return of(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
    }

    public static BigRational of(long numerator) {
        return of(BigInteger.valueOf(numerator), BigInteger.ONE);
    }

    public static BigRational of(BigInteger numerator, BigInteger denominator) {
        return new BigRational(numerator, denominator);
    }

    public static BigRational of(BigDecimal bigDecimal) {
        Objects.requireNonNull(bigDecimal, "bigDecimal");

        BigDecimal amount = bigDecimal.stripTrailingZeros();
        BigInteger n = amount.unscaledValue();

        int scale = amount.scale();

        return scale >= 0
          ? new BigRational(n, BigInteger.TEN.pow(scale))
          : new BigRational(n.multiply(BigInteger.TEN.pow(-scale)), BigInteger.ONE);
    }

    public boolean isZero() {
        return numerator.signum() == 0;
    }

    public boolean isOne() {
        return numerator.equals(BigInteger.ONE) && denominator.equals(BigInteger.ONE);
    }

    public int signum() {
        return numerator.signum();
    }

    public BigRational negate() {
        return new BigRational(numerator.negate(), denominator);
    }

    public BigRational abs() {
        return signum() < 0 ? negate() : this;
    }

    public BigRational add(BigRational o) {
        Objects.requireNonNull(o, "o");
        BigInteger g = this.denominator.gcd(o.denominator);
        BigInteger dd = this.denominator.divide(g).multiply(o.denominator);
        BigInteger nn = this.numerator.multiply(o.denominator.divide(g))
                        .add(o.numerator.multiply(this.denominator.divide(g)));
        return of(nn, dd);
    }

    public BigRational subtract(BigRational o) {
        Objects.requireNonNull(o, "o");
        return add(o.negate());
    }

    public BigRational multiply(BigRational o) {
        Objects.requireNonNull(o, "o");
        BigInteger g1 = this.numerator.gcd(o.denominator);
        BigInteger g2 = o.numerator.gcd(this.denominator);
        return of(
            this.numerator.divide(g1).multiply(o.numerator.divide(g2)),
            this.denominator.divide(g2).multiply(o.denominator.divide(g1))
        );
    }

    public BigRational divide(long o) {
        return divide(BigRational.of(o));
    }

    public BigRational divide(BigRational o) {
        Objects.requireNonNull(o, "o");
        if (o.numerator.signum() == 0) {
            throw new ArithmeticException("division by zero");
        } else {
            return of(this.numerator.multiply(o.denominator), this.denominator.multiply(o.numerator));
        }
    }

    public BigRational pow(int n) {
        if (n == 0) return ONE;
        if (n > 0) return of(numerator.pow(n), denominator.pow(n));
        if (numerator.signum() == 0) throw new ArithmeticException("zero cannot be raised to a negative power");
        return of(denominator.pow(-n), numerator.pow(-n));
    }

    public BigInteger floor() {
        BigInteger[] qr = numerator.divideAndRemainder(denominator);
        BigInteger q = qr[0];
        BigInteger r = qr[1];
        if (r.signum() == 0) {
            return q;
        } else if (numerator.signum() < 0) {
            return q.subtract(BigInteger.ONE);
        } else {
            return q;
        }
    }

    public BigInteger ceil() {
        BigInteger[] qr = numerator.divideAndRemainder(denominator);
        BigInteger q = qr[0];
        BigInteger r = qr[1];
        if (r.signum() == 0) {
            return q;
        } else if (numerator.signum() > 0) {
            return q.add(BigInteger.ONE);
        } else {
            return q;
        }
    }

    public BigInteger truncate() {
        return numerator.divide(denominator);
    }

    public BigInteger roundHalfUp() {
        if (numerator.signum() == 0) {
            return BigInteger.ZERO;
        }
        BigInteger q = numerator.divide(denominator);
        BigInteger r = numerator.remainder(denominator).abs();
        BigInteger twoR = r.shiftLeft(1);
        int cmp = twoR.compareTo(denominator);
        if (cmp < 0) {
            return q;
        } else {
            // exactly half: half-up => away from 0
            return q.add(BigInteger.valueOf(numerator.signum()));
        }
    }

    public BigInteger roundHalfEven() {
        if (numerator.signum() == 0) {
            return BigInteger.ZERO;
        }
        BigInteger q = numerator.divide(denominator);
        BigInteger r = numerator.remainder(denominator).abs();
        BigInteger twoR = r.shiftLeft(1);
        int cmp = twoR.compareTo(denominator);
        if (cmp < 0) {
            return q;
        } else if (cmp > 0) {
            return q.add(BigInteger.valueOf(numerator.signum()));
        } else {
            return q.and(BigInteger.ONE).equals(BigInteger.ZERO)
              ? q
              : q.add(BigInteger.valueOf(numerator.signum()));
        }
    }

    public BigDecimal toBigDecimal(int scale, Rounding rounding) {
        Objects.requireNonNull(rounding, "rounding");

        if (scale < 0) {
            throw new ArithmeticException("negative scale");
        }

        BigDecimal n = new BigDecimal(numerator);
        BigDecimal d = new BigDecimal(denominator);

        return n.divide(d, scale, rounding.mode);
    }

    public BigDecimal toBigDecimal(MathContext mathContext) {
        Objects.requireNonNull(mathContext, "mathContext");

        BigDecimal n = new BigDecimal(numerator);
        BigDecimal d = new BigDecimal(denominator);

        return n.divide(d, mathContext);
    }

    public RoundingResult toDecimal(int scale, Rounding rounding) {
        BigDecimal value = toBigDecimal(scale, rounding);
        BigRational asRational = fromBigDecimalExact(value);
        return new RoundingResult(value, this.subtract(asRational));
    }

    public RoundingResult toDecimal(MathContext mathContext) {
        BigDecimal value = toBigDecimal(mathContext);
        BigRational asRational = fromBigDecimalExact(value);
        return new RoundingResult(value, this.subtract(asRational));
    }

    private static BigRational fromBigDecimalExact(BigDecimal bd) {
        BigInteger unscaled = bd.unscaledValue();
        int scale = bd.scale();

        if (scale >= 0) {
            BigInteger den = BigInteger.TEN.pow(scale);
            return BigRational.of(unscaled, den);
        } else {
            BigInteger mul = BigInteger.TEN.pow(-scale);
            return BigRational.of(unscaled.multiply(mul), BigInteger.ONE);
        }
    }

    public BigRational inverse() {
        if (numerator.signum() == 0) {
            throw new ArithmeticException("inverse of zero is undefined");
        }
        return new BigRational(denominator, numerator);
    }

    @Override
    public int compareTo(BigRational o) {
        Objects.requireNonNull(o, "o");
        return this.numerator.multiply(o.denominator).compareTo(o.numerator.multiply(this.denominator));
    }

    public BigRational min(BigRational o) {
        Objects.requireNonNull(o, "o");
        return compareTo(o) <= 0 ? this : o;
    }

    public BigRational max(BigRational o) {
        Objects.requireNonNull(o, "o");
        return compareTo(o) >= 0 ? this : o;
    }

    @Override
    public String toString() {
        return denominator.equals(BigInteger.ONE) ? numerator.toString() : numerator + "/" + denominator;
    }

    public String toDecimalString() {
        return toDecimalString(10);
    }

    public String toDecimalString(int scale) {
        RoundingResult result = toDecimal(scale, Rounding.HALF_EVEN);
        String plain = result.value().toPlainString();
        return result.residual().isZero() ? plain : "~" + plain;
    }
}
