package com.pivovarit.pid;

record PController(double kp) {

    double compute(double setpoint, double measured) {
        double error = setpoint - measured;
        return kp * error;
    }
}
