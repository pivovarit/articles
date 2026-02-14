package com.pivovarit.pid;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class CruiseControlSimulationTest {

    private static final double TARGET_SPEED = 100.0; // km/h
    private static final double DRAG = 0.1; // linear drag coefficient
    private static final double DT = 0.1; // seconds
    private static final int STEPS = 2000;

    @Test
    void p_controller_reaches_near_target() {
        var controller = new PController(1.0);
        double speed = 0;

        for (int i = 0; i < STEPS; i++) {
            double throttle = controller.compute(TARGET_SPEED, speed);
            speed += (throttle - DRAG * speed) * DT;
        }

        // P-only controller will have steady-state error due to drag
        assertThat(speed).isGreaterThan(90.0).isLessThan(TARGET_SPEED);
    }

    @Test
    void pd_controller_reduces_overshoot() {
        var controller = new PDController(1.0, 0.5);
        double speed = 0;
        double maxOvershoot = 0;

        for (int i = 0; i < STEPS; i++) {
            double throttle = controller.compute(TARGET_SPEED, speed, DT);
            speed += (throttle - DRAG * speed) * DT;
            if (speed > TARGET_SPEED) {
                maxOvershoot = Math.max(maxOvershoot, speed - TARGET_SPEED);
            }
        }

        // PD still has steady-state error, but overshoot is controlled
        assertThat(speed).isGreaterThan(90.0);
        assertThat(maxOvershoot).isLessThan(5.0);
    }

    @Test
    void pid_controller_eliminates_steady_state_error() {
        var controller = new PIDController(1.0, 0.5, 0.1, 0, 500);
        double speed = 0;

        for (int i = 0; i < STEPS; i++) {
            double throttle = controller.compute(TARGET_SPEED, speed, DT);
            speed += (throttle - DRAG * speed) * DT;
        }

        // Full PID should converge very close to target
        assertThat(speed).isCloseTo(TARGET_SPEED, within(0.1));
    }

    @Test
    void pid_controller_handles_disturbance() {
        var controller = new PIDController(1.0, 0.5, 0.1, 0, 500);
        double speed = 0;

        // Reach target first
        for (int i = 0; i < STEPS; i++) {
            double throttle = controller.compute(TARGET_SPEED, speed, DT);
            speed += (throttle - DRAG * speed) * DT;
        }
        assertThat(speed).isCloseTo(TARGET_SPEED, within(0.1));

        // Simulate a hill (sudden speed drop)
        speed -= 20;

        // Recover
        for (int i = 0; i < STEPS; i++) {
            double throttle = controller.compute(TARGET_SPEED, speed, DT);
            speed += (throttle - DRAG * speed) * DT;
        }

        assertThat(speed).isCloseTo(TARGET_SPEED, within(0.1));
    }
}
