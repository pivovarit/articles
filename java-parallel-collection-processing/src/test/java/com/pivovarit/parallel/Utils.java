package com.pivovarit.parallel;

class Utils {
    static <T> T process(T input) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // uninterruptible
        }
        return input;
    }
}
