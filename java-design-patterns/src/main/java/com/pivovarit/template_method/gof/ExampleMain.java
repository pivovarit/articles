package com.pivovarit.template_method.gof;

import java.util.concurrent.ThreadLocalRandom;

public class ExampleMain {

    public static void main(String[] args) {
        var fetchAndLog = new AbstractTimeLoggingMethod() {
            @Override
            void run() {
                findById(42);
            }
        };
    }


    static int findById(int id) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return ThreadLocalRandom.current().nextInt();
    }
}
