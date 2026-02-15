package com.pivovarit.pid;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import javax.imageio.ImageIO;

class Visualizations {

    private static final double TARGET_SPEED = 100.0;
    private static final double DRAG = 0.1;
    private static final double DT = 0.1;
    private static final int STEPS = 1000;

    private static final int WIDTH = 1200;
    private static final int HEIGHT = 500;
    private static final int PADDING_LEFT = 70;
    private static final int PADDING_RIGHT = 30;
    private static final int PADDING_TOP = 50;
    private static final int PADDING_BOTTOM = 50;
    private static final double Y_MIN = 0;
    private static final double Y_MAX = 160;
    private static final Color BG = Color.WHITE;
    private static final Color GRID = new Color(0, 0, 0, 25);
    private static final Color TEXT = new Color(0x333333);
    private static final Color TARGET_LINE_COLOR = new Color(0, 0, 0, 100);

    record ExampleChart() {
        void main() throws IOException {
            var pSpeeds = simulateWithDisturbances((target, speed) -> new PController(1.0).compute(target, speed));
            var piController = new PIController(1.0, 0.5);
            var piSpeeds = simulateWithDisturbances((target, speed) -> piController.compute(target, speed, DT));
            var pdController = new PDController(1.0, 0.5);
            var pdSpeeds = simulateWithDisturbances((target, speed) -> pdController.compute(target, speed, DT));
            var pidController = new PIDController(1.0, 0.5, 0.1, -500, 500);
            var pidSpeeds = simulateWithDisturbances((target, speed) -> pidController.compute(target, speed, DT));

            var html = """
              <!DOCTYPE html>
              <html>
              <head>
                <title>PID Controller Comparison</title>
                <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
                <style>
                  body { font-family: system-ui, sans-serif; margin: 40px; background: #1a1a2e; color: #eee; }
                  canvas { background: #16213e; border-radius: 8px; }
                  h1 { text-align: center; }
                </style>
              </head>
              <body>
                <h1>Cruise Control: P vs PI vs PD vs PID</h1>
                <canvas id="chart"></canvas>
                <script>
                  const labels = %s;
                  new Chart(document.getElementById('chart'), {
                    type: 'line',
                    data: {
                      labels,
                      datasets: [
                        { label: 'Target (100 km/h)', data: labels.map(() => 100), borderColor: '#ffffff44', borderDash: [5, 5], borderWidth: 1, pointRadius: 0 },
                        { label: 'P (kp=1.0)', data: %s, borderColor: '#e74c3c', borderWidth: 1.5, pointRadius: 0, tension: 0.3 },
                        { label: 'PI (kp=1.0, ki=0.5)', data: %s, borderColor: '#3498db', borderWidth: 1.5, pointRadius: 0, tension: 0.3 },
                        { label: 'PD (kp=1.0, kd=0.5)', data: %s, borderColor: '#f39c12', borderWidth: 1.5, pointRadius: 0, tension: 0.3 },
                        { label: 'PID (kp=1.0, ki=0.5, kd=0.1)', data: %s, borderColor: '#2ecc71', borderWidth: 1.5, pointRadius: 0, tension: 0.3 }
                      ]
                    },
                    options: {
                      responsive: true,
                      plugins: {
                        legend: { labels: { color: '#eee' } },
                        title: { display: true, text: 'Speed over time (with disturbances)', color: '#eee', font: { size: 16 } }
                      },
                      scales: {
                        x: { title: { display: true, text: 'Time (s)', color: '#aaa' }, ticks: { color: '#aaa', maxTicksLimit: 20 }, grid: { color: '#ffffff11' } },
                        y: { title: { display: true, text: 'Speed (km/h)', color: '#aaa' }, ticks: { color: '#aaa' }, grid: { color: '#ffffff11' }, min: 0, max: 160 }
                      }
                    }
                  });
                </script>
              </body>
              </html>
              """.formatted(toJsonArray(timeLabels()), toJsonArray(pSpeeds), toJsonArray(piSpeeds), toJsonArray(pdSpeeds), toJsonArray(pidSpeeds));

            var path = Path.of("pid-comparison.html");
            Files.writeString(path, html);
            System.out.println("Chart written to " + path.toAbsolutePath());
        }

        private static ArrayList<String> timeLabels() {
            var labels = new ArrayList<String>();
            for (int i = 0; i < STEPS; i++) {
                labels.add("%.1f".formatted(i * DT));
            }
            return labels;
        }

        private static String toJsonArray(ArrayList<?> list) {
            var sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(list.get(i) instanceof String s ? "\"" + s + "\"" : list.get(i));
            }
            return sb.append("]").toString();
        }
    }

    record ExampleImageSimple() {
        void main() throws IOException {
            var pSpeeds = simulate((target, speed) -> new PController(1.0).compute(target, speed));
            var piController = new PIController(1.0, 0.5);
            var piSpeeds = simulate((target, speed) -> piController.compute(target, speed, DT));
            var pdController = new PDController(1.0, 0.5);
            var pdSpeeds = simulate((target, speed) -> pdController.compute(target, speed, DT));
            var pidController = new PIDController(1.0, 0.5, 0.1, -500, 500);
            var pidSpeeds = simulate((target, speed) -> pidController.compute(target, speed, DT));

            writeImage("pid-simple-p.png", "P Controller (kp=1.0)", List.of(), List.of(
              new Series("P", new Color(0xe74c3c), pSpeeds)));
            writeImage("pid-simple-pi.png", "PI Controller (kp=1.0, ki=0.5)", List.of(), List.of(
              new Series("PI", new Color(0x3498db), piSpeeds)));
            writeImage("pid-simple-pd.png", "PD Controller (kp=1.0, kd=0.5)", List.of(), List.of(
              new Series("PD", new Color(0xf39c12), pdSpeeds)));
            writeImage("pid-simple-pid.png", "PID Controller (kp=1.0, ki=0.5, kd=0.1)", List.of(), List.of(
              new Series("PID", new Color(0x2ecc71), pidSpeeds)));
            writeImage("pid-simple-comparison.png", "P vs PI vs PD vs PID (no disturbances)", List.of(), List.of(
              new Series("P", new Color(0xe74c3c), pSpeeds),
              new Series("PI", new Color(0x3498db), piSpeeds),
              new Series("PD", new Color(0xf39c12), pdSpeeds),
              new Series("PID", new Color(0x2ecc71), pidSpeeds)));
        }
    }

    record ExampleImage() {
        void main() throws IOException {
            var pSpeeds = simulateWithDisturbances((target, speed) -> new PController(1.0).compute(target, speed));
            var piController = new PIController(1.0, 0.5);
            var piSpeeds = simulateWithDisturbances((target, speed) -> piController.compute(target, speed, DT));
            var pdController = new PDController(1.0, 0.5);
            var pdSpeeds = simulateWithDisturbances((target, speed) -> pdController.compute(target, speed, DT));
            var pidController = new PIDController(1.0, 0.5, 0.1, -500, 500);
            var pidSpeeds = simulateWithDisturbances((target, speed) -> pidController.compute(target, speed, DT));

            var disturbanceZones = List.of(
              new DisturbanceZone(300, 500, new Color(255, 80, 80, 40), "uphill"),
              new DisturbanceZone(800, 1000, new Color(80, 140, 255, 40), "downhill"));

            writeImage("pid-p-controller.png", "P Controller (kp=1.0)", disturbanceZones, List.of(
              new Series("P", new Color(0xe74c3c), pSpeeds)));
            writeImage("pid-pi-controller.png", "PI Controller (kp=1.0, ki=0.5)", disturbanceZones, List.of(
              new Series("PI", new Color(0x3498db), piSpeeds)));
            writeImage("pid-pd-controller.png", "PD Controller (kp=1.0, kd=0.5)", disturbanceZones, List.of(
              new Series("PD", new Color(0xf39c12), pdSpeeds)));
            writeImage("pid-pid-controller.png", "PID Controller (kp=1.0, ki=0.5, kd=0.1)", disturbanceZones, List.of(
              new Series("PID", new Color(0x2ecc71), pidSpeeds)));
            writeImage("pid-comparison.png", "P vs PI vs PD vs PID", disturbanceZones, List.of(
              new Series("P", new Color(0xe74c3c), pSpeeds),
              new Series("PI", new Color(0x3498db), piSpeeds),
              new Series("PD", new Color(0xf39c12), pdSpeeds),
              new Series("PID", new Color(0x2ecc71), pidSpeeds)));
        }
    }

    record ExampleWindup() {
        void main() throws IOException {
            // PI windup charts (kd=0) — tight output limits + high ki
            var naivePi = new NaivePIDController(0.5, 2.0, 0, 0, 80);
            var naivePiSpeeds = simulate((target, speed) -> naivePi.compute(target, speed, DT));
            var protectedPi = new PIDController(0.5, 2.0, 0, 0, 80);
            var protectedPiSpeeds = simulate((target, speed) -> protectedPi.compute(target, speed, DT));

            writeImage("pid-pi-windup-none.png", "PI without windup protection", List.of(), List.of(
              new Series("PI (no windup protection)", new Color(0xe74c3c), naivePiSpeeds)));
            writeImage("pid-pi-windup-protected.png", "PI with windup protection", List.of(), List.of(
              new Series("PI (with windup protection)", new Color(0x2ecc71), protectedPiSpeeds)));
            writeImage("pid-pi-windup-comparison.png", "PI: with vs without windup protection", List.of(), List.of(
              new Series("PI (no windup protection)", new Color(0xe74c3c), naivePiSpeeds),
              new Series("PI (with windup protection)", new Color(0x2ecc71), protectedPiSpeeds)));

            // PID windup charts
            var naivePid = new NaivePIDController(0.5, 2.0, 0.1, 0, 80);
            var naivePidSpeeds = simulate((target, speed) -> naivePid.compute(target, speed, DT));
            var protectedPid = new PIDController(0.5, 2.0, 0.1, 0, 80);
            var protectedPidSpeeds = simulate((target, speed) -> protectedPid.compute(target, speed, DT));

            writeImage("pid-windup-none.png", "PID without windup protection", List.of(), List.of(
              new Series("PID (no windup protection)", new Color(0xe74c3c), naivePidSpeeds)));
            writeImage("pid-windup-protected.png", "PID with windup protection", List.of(), List.of(
              new Series("PID (with windup protection)", new Color(0x2ecc71), protectedPidSpeeds)));
            writeImage("pid-windup-comparison.png", "PID: with vs without windup protection", List.of(), List.of(
              new Series("PID (no windup protection)", new Color(0xe74c3c), naivePidSpeeds),
              new Series("PID (with windup protection)", new Color(0x2ecc71), protectedPidSpeeds)));
        }
    }

    record ExampleTuning() {
        void main() throws IOException {
            var defaultPid = new PIDController(1.0, 0.5, 0.1, -500, 500);
            var defaultSpeeds = simulate((target, speed) -> defaultPid.compute(target, speed, DT));

            var tuned1 = new PIDController(2.0, 0.3, 0.2, -500, 500);
            var tuned1Speeds = simulate((target, speed) -> tuned1.compute(target, speed, DT));

            var tuned2 = new PIDController(3.0, 0.5, 0.3, -500, 500);
            var tuned2Speeds = simulate((target, speed) -> tuned2.compute(target, speed, DT));

            var tuned3 = new PIDController(5.0, 0.8, 0.5, -500, 500);
            var tuned3Speeds = simulate((target, speed) -> tuned3.compute(target, speed, DT));

            writeImage("pid-tuning-exploration.png", "PID Tuning Exploration", List.of(), List.of(
              new Series("kp=1.0 ki=0.5 kd=0.1 (default)", new Color(0xe74c3c), defaultSpeeds),
              new Series("kp=2.0 ki=0.3 kd=0.2", new Color(0x3498db), tuned1Speeds),
              new Series("kp=3.0 ki=0.5 kd=0.3", new Color(0xf39c12), tuned2Speeds),
              new Series("kp=5.0 ki=0.8 kd=0.5", new Color(0x2ecc71), tuned3Speeds)));

            // with disturbances — default vs best tuned
            var defaultDist = simulateWithDisturbances((target, speed) -> defaultPid.compute(target, speed, DT));
            var tunedPid = new PIDController(5.0, 0.8, 0.5, -500, 500);
            var tunedDist = simulateWithDisturbances((target, speed) -> tunedPid.compute(target, speed, DT));

            var disturbanceZones = List.of(
              new DisturbanceZone(300, 500, new Color(255, 80, 80, 40), "uphill"),
              new DisturbanceZone(800, 1000, new Color(80, 140, 255, 40), "downhill"));

            writeImage("pid-tuning-disturbances.png", "Default vs Tuned PID (with disturbances)", disturbanceZones, List.of(
              new Series("kp=1.0 ki=0.5 kd=0.1 (default)", new Color(0xe74c3c), defaultDist),
              new Series("kp=5.0 ki=0.8 kd=0.5 (tuned)", new Color(0x2ecc71), tunedDist)));

            writeImage("pid-tuned-disturbances.png", "Tuned PID (kp=5.0, ki=0.8, kd=0.5) with disturbances", disturbanceZones, List.of(
              new Series("PID (tuned)", new Color(0x2ecc71), tunedDist)));
        }
    }

    record Series(String name, Color color, List<Double> data) {}

    record DisturbanceZone(int startStep, int endStep, Color color, String label) {}

    private static ArrayList<Double> simulate(BiFunction<Double, Double, Double> controller) {
        var speeds = new ArrayList<Double>();
        double speed = 0;

        for (int step = 0; step < STEPS; step++) {
            double throttle = controller.apply(TARGET_SPEED, speed);
            speed += (throttle - DRAG * speed) * DT;
            speed = Math.max(0, speed);
            speeds.add(speed);
        }
        return speeds;
    }

    private static ArrayList<Double> simulateWithDisturbances(BiFunction<Double, Double, Double> controller) {
        var speeds = new ArrayList<Double>();
        double speed = 0;
        double disturbance = 0;

        for (int step = 0; step < STEPS; step++) {
            if (step == 300) {
                disturbance = -40;
            } else if (step == 500) {
                disturbance = 0;
            } else if (step == 800) {
                disturbance = 25;
            } else if (step == 1000) {
                disturbance = 0;
            }

            double throttle = controller.apply(TARGET_SPEED, speed);
            speed += (throttle + disturbance - DRAG * speed) * DT;
            speed = Math.max(0, speed);
            speeds.add(speed);
        }
        return speeds;
    }

    private static void writeImage(String filename, String title, List<DisturbanceZone> zones, List<Series> seriesList) throws IOException {
        var image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        var g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        g.setColor(BG);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        int plotW = WIDTH - PADDING_LEFT - PADDING_RIGHT;
        int plotH = HEIGHT - PADDING_TOP - PADDING_BOTTOM;

        // grid lines and Y axis labels
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (int speed = 0; speed <= (int) Y_MAX; speed += 20) {
            int y = PADDING_TOP + plotH - (int) ((speed - Y_MIN) / (Y_MAX - Y_MIN) * plotH);
            g.setColor(GRID);
            g.drawLine(PADDING_LEFT, y, PADDING_LEFT + plotW, y);
            g.setColor(TEXT);
            g.drawString(speed + " km/h", 5, y + 4);
        }

        // X axis labels
        for (int t = 0; t <= (int) (STEPS * DT); t += 10) {
            int x = PADDING_LEFT + (int) ((double) t / (STEPS * DT) * plotW);
            g.setColor(GRID);
            g.drawLine(x, PADDING_TOP, x, PADDING_TOP + plotH);
            g.setColor(TEXT);
            g.drawString(t + "s", x - 8, HEIGHT - PADDING_BOTTOM + 18);
        }

        // target line
        int targetY = PADDING_TOP + plotH - (int) ((TARGET_SPEED - Y_MIN) / (Y_MAX - Y_MIN) * plotH);
        g.setColor(TARGET_LINE_COLOR);
        g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{6, 4}, 0));
        g.drawLine(PADDING_LEFT, targetY, PADDING_LEFT + plotW, targetY);

        // disturbance zones
        for (var zone : zones) {
            int x1 = PADDING_LEFT + (int) ((double) zone.startStep / STEPS * plotW);
            int x2 = PADDING_LEFT + (int) ((double) zone.endStep / STEPS * plotW);
            g.setColor(zone.color);
            g.fillRect(x1, PADDING_TOP, x2 - x1, plotH);
            g.setColor(new Color(zone.color.getRed(), zone.color.getGreen(), zone.color.getBlue(), 120));
            g.setFont(new Font("SansSerif", Font.ITALIC, 10));
            g.drawString(zone.label, x1 + 4, PADDING_TOP + 14);
        }

        // data series
        g.setStroke(new BasicStroke(2));
        for (var series : seriesList) {
            g.setColor(series.color);
            for (int i = 1; i < series.data.size(); i++) {
                int x1 = PADDING_LEFT + (int) ((double) (i - 1) / STEPS * plotW);
                int y1 = PADDING_TOP + plotH - (int) ((series.data.get(i - 1) - Y_MIN) / (Y_MAX - Y_MIN) * plotH);
                int x2 = PADDING_LEFT + (int) ((double) i / STEPS * plotW);
                int y2 = PADDING_TOP + plotH - (int) ((series.data.get(i) - Y_MIN) / (Y_MAX - Y_MIN) * plotH);
                g.drawLine(x1, y1, x2, y2);
            }
        }

        // title
        g.setColor(new Color(0x222222));
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        var titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (WIDTH - titleWidth) / 2, 28);

        // legend
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        int legendX = PADDING_LEFT + 10;
        int legendY = PADDING_TOP + 20;
        for (var series : seriesList) {
            g.setColor(series.color);
            g.fillRect(legendX, legendY - 10, 14, 14);
            g.setColor(TEXT);
            g.drawString(series.name, legendX + 20, legendY + 2);
            legendY += 20;
        }

        g.dispose();

        var path = Path.of(filename);
        ImageIO.write(image, "PNG", path.toFile());
        System.out.println("Image written to " + path.toAbsolutePath());
    }
}
