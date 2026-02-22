package com.pivovarit.money.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;
import java.util.regex.Pattern;

final class BigRationalParser {

    private BigRationalParser() {
    }

    private static final Pattern PLAIN_NUMBER = Pattern.compile("[+-]?\\d+(?:\\.\\d+)?");

    static BigRational of(String s) {
        Objects.requireNonNull(s, "s");
        String input = s.trim();

        if (input.isEmpty()) {
            throw new IllegalArgumentException("empty input");
        } else if (input.indexOf(',') >= 0) {
            throw new IllegalArgumentException("commas are not supported: " + s);
        } else if (input.indexOf('e') >= 0 || input.indexOf('E') >= 0) {
            throw new IllegalArgumentException("scientific notation is not supported: " + s);
        }

        int slash = input.indexOf('/');
        if (slash < 0) {
            return parsePlainNumber(input, s);
        } else if (slash != input.lastIndexOf('/')) {
            throw new IllegalArgumentException("invalid rational (too many '/'): " + s);
        }

        String left = input.substring(0, slash).trim();
        String right = input.substring(slash + 1).trim();

        if (left.isEmpty() || right.isEmpty()) {
            throw new IllegalArgumentException("invalid rational (missing numerator/denominator): " + s);
        }

        return parsePlainNumber(left, s).divide(parsePlainNumber(right, s));
    }

    private static BigRational parsePlainNumber(String part, String original) {
        if (!PLAIN_NUMBER.matcher(part).matches()) {
            throw new IllegalArgumentException("invalid number: '" + part + "' (from: " + original + ")");
        }
        return part.indexOf('.') >= 0
          ? BigRational.of(new BigDecimal(part))
          : BigRational.of(new BigInteger(part), BigInteger.ONE);
    }
}
