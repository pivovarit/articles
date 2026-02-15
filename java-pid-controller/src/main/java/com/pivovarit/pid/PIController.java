package com.pivovarit.pid;

class PIController {

    private final double kp;
    private final double ki;

    private double accumulatedError;

    PIController(double kp, double ki) {
        this.kp = kp;
        this.ki = ki;
    }

    double compute(double setpoint, double measured, double dt) {
        double error = setpoint - measured;
        accumulatedError += error * dt;
        return kp * error + ki * accumulatedError;
    }
}
