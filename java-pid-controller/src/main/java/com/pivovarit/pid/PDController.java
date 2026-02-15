package com.pivovarit.pid;

class PDController {

    private final double kp;
    private final double kd;

    private double previousError;

    PDController(double kp, double kd) {
        this.kp = kp;
        this.kd = kd;
    }

    double compute(double setpoint, double measured, double dt) {
        double error = setpoint - measured;
        double changeRate = (error - previousError) / dt;
        previousError = error;
        return kp * error + kd * changeRate;
    }
}
