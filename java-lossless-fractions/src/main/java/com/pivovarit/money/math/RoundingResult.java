package com.pivovarit.money.math;

import java.math.BigDecimal;
import java.util.Objects;

public record RoundingResult(BigDecimal value, BigRational residual) {
    public RoundingResult {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(residual, "residual");
    }
}
