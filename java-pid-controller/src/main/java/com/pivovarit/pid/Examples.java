package com.pivovarit.pid;

class Examples {

    private static final double TARGET_SPEED = 100.0;
    private static final double DRAG = 0.1;
    private static final double DT = 0.1;
    private static final int STEPS = 1000;

    record ExampleP() {
        void main() {
            var controller = new PController(1.0);
            double speed = 0;

            for (int i = 0; i < STEPS; i++) {
                double throttle = controller.compute(TARGET_SPEED, speed);
                speed += (throttle - DRAG * speed) * DT;
                System.out.println(i + ": speed = " + speed);
            }
        }
    }

    record ExamplePI() {
        void main() {
            var controller = new PIController(1.0, 0.5);
            double speed = 0;

            for (int i = 0; i < STEPS; i++) {
                double throttle = controller.compute(TARGET_SPEED, speed, DT);
                speed += (throttle - DRAG * speed) * DT;
                System.out.println(i + ": speed = " + speed);
            }
        }
    }

    record ExamplePD() {
        void main() {
            var controller = new PDController(1.0, 0.5);
            double speed = 0;

            for (int i = 0; i < STEPS; i++) {
                double throttle = controller.compute(TARGET_SPEED, speed, DT);
                speed += (throttle - DRAG * speed) * DT;
                System.out.println(i + ": speed = " + speed);
            }
        }
    }

    record ExamplePID() {
        void main() {
            var controller = new PIDController(1.0, 0.5, 0.1, -500, 500);
            double speed = 0;

            for (int i = 0; i < STEPS; i++) {
                double throttle = controller.compute(TARGET_SPEED, speed, DT);
                speed += (throttle - DRAG * speed) * DT;
                System.out.println(i + ": speed = " + speed);
            }
        }
    }
}
