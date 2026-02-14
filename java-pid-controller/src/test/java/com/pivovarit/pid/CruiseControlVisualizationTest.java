package com.pivovarit.pid;

import org.junit.jupiter.api.Test;

class CruiseControlVisualizationTest {

    private static final double TARGET_SPEED = 100.0;
    private static final double DRAG = 0.1;
    private static final double DT = 0.1;
    private static final int BAR_WIDTH = 50;

    @Test
    void visualize_cruise_control() {
        System.out.println();
        System.out.println("=== Cruise Control Simulation — target: 100 km/h ===");

        System.out.println();
        System.out.println("--- P Controller (kp=1.0) ---");
        simulateP();

        System.out.println();
        System.out.println("--- PD Controller (kp=1.0, kd=0.5) ---");
        simulatePD();

        System.out.println();
        System.out.println("--- PID Controller (kp=1.0, ki=0.5, kd=0.1) ---");
        simulatePID();
    }

    private void simulateP() {
        var controller = new PController(1.0);
        double speed = 0;
        for (int step = 0; step < 200; step++) {
            double throttle = controller.compute(TARGET_SPEED, speed);
            speed += (throttle - DRAG * speed) * DT;
            if (step % 10 == 0) printBar(step * DT, speed);
        }
    }

    private void simulatePD() {
        var controller = new PDController(1.0, 0.5);
        double speed = 0;
        for (int step = 0; step < 200; step++) {
            double throttle = controller.compute(TARGET_SPEED, speed, DT);
            speed += (throttle - DRAG * speed) * DT;
            if (step % 10 == 0) printBar(step * DT, speed);
        }
    }

    private void simulatePID() {
        var controller = new PIDController(1.0, 0.5, 0.1, 0, 500);
        double speed = 0;
        for (int step = 0; step < 200; step++) {
            double throttle = controller.compute(TARGET_SPEED, speed, DT);
            speed += (throttle - DRAG * speed) * DT;
            if (step % 10 == 0) printBar(step * DT, speed);
        }
    }

    private void printBar(double time, double speed) {
        int targetPos = BAR_WIDTH;
        int speedPos = (int) Math.round(speed / TARGET_SPEED * BAR_WIDTH);
        speedPos = Math.max(0, Math.min(speedPos, BAR_WIDTH + 10));

        var bar = new StringBuilder();
        for (int i = 0; i <= BAR_WIDTH + 10; i++) {
            if (i == speedPos && i == targetPos) {
                bar.append("X"); // speed is exactly at target
            } else if (i == speedPos) {
                bar.append(">");
            } else if (i == targetPos) {
                bar.append("|");
            } else if (i < speedPos) {
                bar.append("=");
            } else {
                bar.append(" ");
            }
        }

        String label;
        double error = TARGET_SPEED - speed;
        if (Math.abs(error) < 0.5) {
            label = "ON TARGET";
        } else if (error > 0) {
            label = String.format("%.1f km/h below target", error);
        } else {
            label = String.format("%.1f km/h ABOVE target", -error);
        }

        System.out.printf("  t=%4.1fs  %5.1f km/h [%s] %s%n", time, speed, bar, label);
    }
}
