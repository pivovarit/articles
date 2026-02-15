package com.pivovarit.pid;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.BiFunction;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

class PidFineTuningVisualizationTest {

    private static final double TARGET_SPEED = 100.0;
    private static final double DRAG = 0.1;
    private static final double DT = 0.1;
    private static final int BAR_WIDTH = 50;
    private static final int STEPS = 2000;

    static Stream<Arguments> pidParameters() {
        return Stream.of(
          Arguments.of(1.0, 0.5, 0.1, "balanced, well-tuned"),
          Arguments.of(2.0, 0.0, 0.0, "P-only, moderate gain"),
          Arguments.of(5.0, 0.0, 0.0, "P-only, high gain"),
          Arguments.of(1.0, 0.3, 0.0, "PI, moderate integral"),
          Arguments.of(1.0, 1.0, 0.0, "PI, aggressive integral"),
          Arguments.of(2.0, 0.0, 0.5, "PD, moderate derivative"),
          Arguments.of(2.0, 0.0, 2.0, "PD, heavy damping"),
          Arguments.of(5.0, 2.0, 1.0, "aggressive PID"),
          Arguments.of(0.5, 0.1, 0.05, "conservative PID"),
          Arguments.of(0.5, 3.0, 0.1, "high integral, wind-up prone"),
          Arguments.of(10.0, 0.5, 0.01, "oscillation-prone, high P"),
          Arguments.of(1.0, 0.5, 5.0, "overdamped, sluggish response")
        );
    }

    @ParameterizedTest(name = "PID Controller ({3}): kp={0}, ki={1}, kd={2}")
    @MethodSource("pidParameters")
    void pidWithDisturbances(double kp, double ki, double kd, String description) {
        var controller = new PIDController(kp, ki, kd, -500, 500);
        simulate("PID Controller (%s): kp=%.1f, ki=%.1f, kd=%.1f".formatted(description, kp, ki, kd),
          (target, speed) -> controller.compute(target, speed, DT));
    }

    private void simulate(String name, BiFunction<Double, Double, Double> controller) {
        var random = RandomGenerator.getDefault();

        double speed = 0;
        double disturbance = 0;

        System.out.println("=== Continuous Cruise Control — target: 100 km/h (" + name + ") ===");
        System.out.println();

        for (int step = 0; step < STEPS; step++) {
            double time = step * DT;

            if (step == 300) {
                disturbance = -40;
                System.out.println("  *** DISTURBANCE: steep uphill (-40 drag) ***");
            } else if (step == 500) {
                disturbance = 0;
                System.out.println("  *** DISTURBANCE ENDED: flat road ***");
            } else if (step == 800) {
                disturbance = 25;
                System.out.println("  *** DISTURBANCE: downhill slope (+25 boost) ***");
            } else if (step == 1000) {
                disturbance = 0;
                System.out.println("  *** DISTURBANCE ENDED: flat road ***");
            } else if (step == 1300) {
                disturbance = -60;
                System.out.println("  *** DISTURBANCE: strong headwind (-60 drag) ***");
            } else if (step == 1500) {
                disturbance = 0;
                System.out.println("  *** DISTURBANCE ENDED: calm weather ***");
            } else if (step == 1700) {
                disturbance = 15;
                System.out.println("  *** DISTURBANCE: tailwind (+15 boost) ***");
            } else if (step == 1850) {
                disturbance = 0;
                System.out.println("  *** DISTURBANCE ENDED: calm weather ***");
            }

            double noise = random.nextGaussian() * 0.5;

            double throttle = controller.apply(TARGET_SPEED, speed);
            speed += (throttle + disturbance - DRAG * speed + noise) * DT;
            speed = Math.max(0, speed);

            if (step % 5 == 0) {
                printBar(time, speed, disturbance);
            }
        }
    }

    private void printBar(double time, double speed, double disturbance) {
        int targetPos = BAR_WIDTH;
        int speedPos = (int) Math.round(speed / TARGET_SPEED * BAR_WIDTH);
        speedPos = Math.max(0, Math.min(speedPos, BAR_WIDTH + 10));

        var bar = new StringBuilder();
        for (int i = 0; i <= BAR_WIDTH + 10; i++) {
            if (i == speedPos && i == targetPos) {
                bar.append("X");
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
            label = String.format("%.1f km/h below", error);
        } else {
            label = String.format("%.1f km/h ABOVE", -error);
        }

        String distLabel = disturbance != 0
          ? String.format(" [disturbance: %+.0f]", disturbance)
          : "";

        System.out.printf("  t=%5.1fs  %5.1f km/h [%s] %s%s%n", time, speed, bar, label, distLabel);
    }
}
