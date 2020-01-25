package com.pivovarit.parallel;

import java.util.concurrent.ThreadLocalRandom;

class Utils {
    static <T> T process(T input) {
        try {
            Thread.sleep(100 + ThreadLocalRandom.current().nextInt(100));
        } catch (InterruptedException e) {
            // uninterruptible
        }
        return input;
    }
}
