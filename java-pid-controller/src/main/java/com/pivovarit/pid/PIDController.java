package com.pivovarit.pid;

class PIDController {

    private final double kp;
    private final double ki;
    private final double kd;
    private final double outputMin;
    private final double outputMax;

    private double integral;
    private double previousError;

    PIDController(double kp, double ki, double kd, double outputMin, double outputMax) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
        this.outputMin = outputMin;
        this.outputMax = outputMax;
    }

    double compute(double setpoint, double measured, double dt) {
        double error = setpoint - measured;

        integral += error * dt;
        double derivative = (error - previousError) / dt;
        previousError = error;

        double output = kp * error + ki * integral + kd * derivative;

        if (output > outputMax) {
            integral -= error * dt; // windup protection
            return outputMax;
        }
        if (output < outputMin) {
            integral -= error * dt; // windup protection
            return outputMin;
        }

        return output;
    }
}
