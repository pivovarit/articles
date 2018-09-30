package com.pivovarit.template_method.lambda;

import java.time.Duration;
import java.time.LocalTime;

final class TemplateMethodUtil {

    private TemplateMethodUtil() {
    }

    static void runWithExecutionTimeLogging(Runnable action) {
        var before = LocalTime.now();
        action.run();
        var after = LocalTime.now();
        System.out.printf("Execution took: %d ms%n", Duration.between(before, after).toMillis());
    }

    static void orchestrate(Runnable step1, Runnable step2) {
        System.out.println("starting...");
        step1.run();
        step2.run();
        System.out.println("ending...");
    }
}
