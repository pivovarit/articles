package com.pivovarit.pid;

class NaivePIDController {

    private final double kp;
    private final double ki;
    private final double kd;
    private final double outputMin;
    private final double outputMax;

    private double accumulatedError;
    private double previousError;

    NaivePIDController(double kp, double ki, double kd, double outputMin, double outputMax) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
        this.outputMin = outputMin;
        this.outputMax = outputMax;
    }

    double compute(double setpoint, double measured, double dt) {
        double error = setpoint - measured;

        accumulatedError += error * dt;
        double changeRate = (error - previousError) / dt;
        previousError = error;

        double output = kp * error + ki * accumulatedError + kd * changeRate;

        return Math.clamp(output, outputMin, outputMax);
    }
}
