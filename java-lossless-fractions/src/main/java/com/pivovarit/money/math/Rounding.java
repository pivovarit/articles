package com.pivovarit.money.math;

import java.math.RoundingMode;

public enum Rounding {
    FLOOR(RoundingMode.FLOOR),
    CEIL(RoundingMode.CEILING),
    HALF_UP(RoundingMode.HALF_UP),
    HALF_EVEN(RoundingMode.HALF_EVEN);

    final RoundingMode mode;

    Rounding(RoundingMode mode) {
        this.mode = mode;
    }
}
