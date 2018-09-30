package com.pivovarit.template_method.gof;

import java.time.Duration;
import java.time.LocalTime;

abstract class AbstractTimeLoggingMethod {

    abstract void run();

    public void runWithTimeLogging() {
        var before = LocalTime.now();
        run();
        var after = LocalTime.now();
        System.out.printf("Execution took: %d ms%n", Duration.between(before, after).toMillis());
    }
}
