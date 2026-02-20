package com.pivovarit.raft;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Generates four visualizations of the Raft algorithm lifecycle.
 *
 * Scenario (9 steps across a 3-node cluster):
 *   0. initial      – all nodes start as FOLLOWER
 *   1. n1 elected   – n1 wins election, becomes LEADER (term 1)
 *   2. +cmd 1       – first entry committed to all nodes
 *   3. +cmd 2       – second entry committed to all nodes
 *   4. n3 down      – n3 partitioned; cmd-3 committed by n1+n2 only
 *      +cmd 3
 *   5. n3 back      – n3 reconnects and catches up; cmd-4 committed to all
 *      +cmd 4
 *   6. n1 down      – n1 partitioned; n2 wins re-election (term 2)
 *      n2 elected
 *   7. +cmd 5       – cmd-5 committed by n2+n3
 *   8. n1 recovered – n1 reconnects, receives n2's heartbeat, catches up
 *
 * Run any of the four inner records via `java ExampleXxx` (Java 25+).
 */
class RaftVisualizations {

    // ── Color palette ─────────────────────────────────────────────────────
    private static final Color BG            = Color.WHITE;
    private static final Color TEXT          = new Color(0x222222);
    private static final Color TEXT_MUTED    = new Color(0x777777);
    private static final Color GRID_LINE     = new Color(0, 0, 0, 25);
    private static final Color FOLLOWER_CLR  = new Color(0x4a90d9);
    private static final Color CANDIDATE_CLR = new Color(0xe6a020);
    private static final Color LEADER_CLR    = new Color(0x27ae60);
    private static final Color COMMIT_CLR    = new Color(0x2471a3);
    private static final Color PRESENT_CLR   = new Color(0x85c1e9);
    private static final Color ABSENT_CLR    = new Color(0xe8ecef);

    // ── Snapshot ──────────────────────────────────────────────────────────

    record Snapshot(
        String label,
        Map<String, NodeRole>  roles,
        Map<String, Boolean>   partitioned,
        Map<String, Integer>   commitIndex,
        Map<String, Integer>   logSize
    ) {}

    private static Snapshot capture(String label, RaftNode... nodes) {
        var roles = new LinkedHashMap<String, NodeRole>();
        var part  = new LinkedHashMap<String, Boolean>();
        var ci    = new LinkedHashMap<String, Integer>();
        var ls    = new LinkedHashMap<String, Integer>();
        for (var n : nodes) {
            roles.put(n.id, n.role());
            part.put(n.id,  n.partitioned);
            ci.put(n.id,    n.commitIndex());
            ls.put(n.id,    n.log().size());
        }
        return new Snapshot(label, roles, part, ci, ls);
    }

    // ── Shared scenario ───────────────────────────────────────────────────

    static List<Snapshot> runScenario() {
        var cluster = new RaftCluster();
        var n1 = new RaftNode("n1");
        var n2 = new RaftNode("n2");
        var n3 = new RaftNode("n3");
        cluster.add(n1); cluster.add(n2); cluster.add(n3);

        var snaps = new ArrayList<Snapshot>();
        snaps.add(capture("initial", n1, n2, n3));

        n1.startElection();
        snaps.add(capture("n1 elected", n1, n2, n3));

        n1.submit("cmd-1");
        snaps.add(capture("+cmd 1", n1, n2, n3));

        n1.submit("cmd-2");
        snaps.add(capture("+cmd 2", n1, n2, n3));

        n3.partition();
        n1.submit("cmd-3");
        snaps.add(capture("n3 down\n+cmd 3", n1, n2, n3));

        n3.reconnect();
        n1.submit("cmd-4");
        snaps.add(capture("n3 back\n+cmd 4", n1, n2, n3));

        n1.partition();
        n2.startElection();
        snaps.add(capture("n1 down\nn2 elected", n1, n2, n3));

        n2.submit("cmd-5");
        snaps.add(capture("+cmd 5", n1, n2, n3));

        n1.reconnect();
        n2.heartbeat();
        snaps.add(capture("n1 recovered", n1, n2, n3));

        return snaps;
    }

    // ── Visualization 1: Node Role Timeline ───────────────────────────────

    /**
     * Generates raft-roles-timeline.png — a coloured grid showing each node's role
     * (FOLLOWER / CANDIDATE / LEADER) at every step of the scenario.
     * Partitioned cells are overlaid with diagonal stripes.
     */
    record ExampleRoleTimeline() {
        void main() throws IOException {
            var snaps  = runScenario();
            var nodes  = List.of("n1", "n2", "n3");
            int steps  = snaps.size();   // 9
            int cellW  = 108, cellH = 64;
            int lblW   = 58, topH = 54, botH = 82, legendH = 36;
            int W = lblW + steps * cellW + 20;
            int H = topH + nodes.size() * cellH + botH + legendH;

            var img = newImage(W, H);
            var g   = img.createGraphics();
            setHints(g);
            g.setColor(BG); g.fillRect(0, 0, W, H);

            // title
            drawCentred(g, "Node State Timeline  —  3-node Raft Cluster",
                new Font("SansSerif", Font.BOLD, 15), TEXT, W, 32);

            // cells
            for (int si = 0; si < steps; si++) {
                var snap = snaps.get(si);
                int cx = lblW + si * cellW;
                for (int ni = 0; ni < nodes.size(); ni++) {
                    String nid = nodes.get(ni);
                    int cy = topH + ni * cellH;
                    NodeRole role = snap.roles().get(nid);
                    boolean isPartitioned = snap.partitioned().get(nid);

                    // background
                    g.setColor(roleColor(role));
                    g.fillRect(cx, cy, cellW, cellH);

                    // partition overlay
                    if (isPartitioned) drawStripes(g, cx, cy, cellW, cellH);

                    // role text
                    g.setFont(new Font("SansSerif", Font.BOLD, 11));
                    g.setColor(Color.WHITE);
                    int textY = isPartitioned ? cy + cellH / 2 - 4 : cy + cellH / 2 + 5;
                    drawCentred(g, role.name(), cx, textY, cellW);

                    // "(partitioned)" sub-label
                    if (isPartitioned) {
                        g.setFont(new Font("SansSerif", Font.PLAIN, 9));
                        drawCentred(g, "(partitioned)", cx, cy + cellH / 2 + 10, cellW);
                    }

                    // commit index hint (bottom-right)
                    g.setFont(new Font("SansSerif", Font.PLAIN, 9));
                    g.setColor(new Color(255, 255, 255, 170));
                    int ci = snap.commitIndex().get(nid);
                    g.drawString("ci=" + ci, cx + cellW - 32, cy + cellH - 5);

                    // cell border
                    g.setColor(new Color(255, 255, 255, 60));
                    g.drawRect(cx, cy, cellW - 1, cellH - 1);
                }
            }

            // node labels (left column)
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            for (int ni = 0; ni < nodes.size(); ni++) {
                int cy = topH + ni * cellH;
                drawCentred(g, nodes.get(ni), new Font("SansSerif", Font.BOLD, 13),
                    TEXT, lblW, cy + cellH / 2 + 5);
            }

            // step labels (below grid)
            g.setFont(new Font("SansSerif", Font.PLAIN, 10));
            int lblY0 = topH + nodes.size() * cellH + 10;
            for (int si = 0; si < steps; si++) {
                int cx = lblW + si * cellW;
                String[] lines = snaps.get(si).label().split("\n");
                int lh = 14, y = lblY0 + (botH - lines.length * lh) / 2;
                for (var line : lines) {
                    g.setColor(TEXT);
                    drawCentred(g, line, cx, y, cellW);
                    y += lh;
                }
                g.setFont(new Font("SansSerif", Font.PLAIN, 9));
                g.setColor(TEXT_MUTED);
                drawCentred(g, "(" + si + ")", cx, lblY0 + botH - 8, cellW);
                g.setFont(new Font("SansSerif", Font.PLAIN, 10));
            }

            // legend
            int lx = 20, ly = H - legendH + 10;
            Object[][] items = {
                {FOLLOWER_CLR,  "FOLLOWER"},
                {LEADER_CLR,    "LEADER"},
                {CANDIDATE_CLR, "CANDIDATE"},
                {FOLLOWER_CLR,  "partitioned (striped)"}
            };
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            for (var item : items) {
                g.setColor((Color) item[0]);
                g.fillRect(lx, ly, 14, 14);
                if (item[1].toString().contains("partitioned")) drawStripes(g, lx, ly, 14, 14);
                g.setColor(TEXT);
                String txt = (String) item[1];
                g.drawString(txt, lx + 18, ly + 11);
                lx += 18 + g.getFontMetrics().stringWidth(txt) + 22;
            }

            g.dispose();
            save(img, "raft-roles-timeline.png");
        }
    }

    // ── Visualization 2: Commit Index Progress ────────────────────────────

    /**
     * Generates raft-commit-progress.png — a line chart comparing the commit index
     * of each node across the 9 scenario steps. Divergences during partitions and
     * convergences after recovery are clearly visible.
     */
    record ExampleCommitProgress() {
        void main() throws IOException {
            var snaps   = runScenario();
            int steps   = snaps.size();
            int maxCI   = 5;
            int W = 1100, H = 430;
            int PL = 65, PR = 30, PT = 55, PB = 85;
            int pW = W - PL - PR, pH = H - PT - PB;

            var img = newImage(W, H);
            var g   = img.createGraphics();
            setHints(g);
            g.setColor(BG); g.fillRect(0, 0, W, H);

            drawCentred(g, "Commit Index per Node  —  9-step Raft Lifecycle",
                new Font("SansSerif", Font.BOLD, 15), TEXT, W, 32);

            // Y-axis grid + labels
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            for (int v = 0; v <= maxCI; v++) {
                int y = PT + pH - (int)((double) v / maxCI * pH);
                g.setColor(GRID_LINE);
                g.drawLine(PL, y, PL + pW, y);
                g.setColor(TEXT_MUTED);
                var lbl = String.valueOf(v);
                g.drawString(lbl, PL - g.getFontMetrics().stringWidth(lbl) - 5, y + 4);
            }

            // Y-axis rotated title
            var oldAt = g.getTransform();
            g.rotate(-Math.PI / 2, 18, PT + pH / 2);
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g.setColor(TEXT_MUTED);
            drawCentred(g, "commit index", 18 - 40, PT + pH / 2 + 4, 80);
            g.setTransform(oldAt);

            // X-axis grid + step labels
            for (int si = 0; si < steps; si++) {
                int x = PL + (int)((double) si / (steps - 1) * pW);
                g.setColor(GRID_LINE);
                g.drawLine(x, PT, x, PT + pH);
                g.setFont(new Font("SansSerif", Font.PLAIN, 10));
                String[] lines = snaps.get(si).label().split("\n");
                int sy = PT + pH + 14;
                for (var line : lines) {
                    g.setColor(TEXT);
                    drawCentred(g, line, x - 50, sy, 100);
                    sy += 13;
                }
            }

            // Plot border
            g.setColor(new Color(0, 0, 0, 50));
            g.setStroke(new BasicStroke(1));
            g.drawRect(PL, PT, pW, pH);

            // Data series
            var nodeIds = List.of("n1", "n2", "n3");
            var colours = List.of(new Color(0x4a90d9), new Color(0x27ae60), new Color(0xe67e22));
            g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int ni = 0; ni < nodeIds.size(); ni++) {
                String nid = nodeIds.get(ni);
                g.setColor(colours.get(ni));
                int px = -1, py = -1;
                for (int si = 0; si < steps; si++) {
                    int ci = snaps.get(si).commitIndex().get(nid);
                    int x  = PL + (int)((double) si / (steps - 1) * pW);
                    int y  = PT + pH - (int)((double) ci / maxCI * pH);
                    if (px >= 0) g.drawLine(px, py, x, y);
                    g.fillOval(x - 5, y - 5, 10, 10);
                    px = x; py = y;
                }
            }

            // Legend (top-left of plot area)
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            int lx = PL + 16, ly = PT + 16;
            for (int ni = 0; ni < nodeIds.size(); ni++) {
                g.setColor(colours.get(ni));
                g.fillRect(lx, ly - 10, 16, 16);
                g.setColor(TEXT);
                g.drawString(nodeIds.get(ni), lx + 22, ly + 2);
                lx += 72;
            }

            g.setStroke(new BasicStroke(1));
            g.dispose();
            save(img, "raft-commit-progress.png");
        }
    }

    // ── Visualization 3: Log State Grid ───────────────────────────────────

    /**
     * Generates raft-log-state.png — two side-by-side grids comparing the log state
     * of every node at two key moments:
     *   Left:  "During partition" (step 4) — n3 is isolated and missing cmd-3
     *   Right: "After recovery"  (step 8) — all nodes hold all 5 committed entries
     *
     * Cell colours:
     *   Dark blue  – entry present AND committed
     *   Light blue – entry present but not yet committed
     *   Light grey – entry absent
     *   Stripes    – node is currently partitioned
     */
    record ExampleLogStateGrid() {
        void main() throws IOException {
            var snaps    = runScenario();
            var nodes    = List.of("n1", "n2", "n3");
            int maxEntry = 5;
            int cW = 82, cH = 52;     // cell dimensions
            int hdrH = 46, lblW = 56; // column-header height, row-label width
            int pW = lblW + maxEntry * cW;
            int pH = hdrH + nodes.size() * cH;
            int gap = 50, pad = 30, topH = 52, botH = 60;
            int W = pad + pW + gap + pW + pad;
            int H = topH + pH + botH;

            var img = newImage(W, H);
            var g   = img.createGraphics();
            setHints(g);
            g.setColor(BG); g.fillRect(0, 0, W, H);

            drawCentred(g, "Log State per Node  —  Replication & Recovery",
                new Font("SansSerif", Font.BOLD, 15), TEXT, W, 32);

            // Draw two panels
            int[] panelX = {pad, pad + pW + gap};
            int[] snapIdx = {4, 8};  // step 4 = "n3 down +cmd3", step 8 = "n1 recovered"
            String[] panelTitles = {
                "During partition  (step 4: n3 is down)",
                "After recovery  (step 8: all nodes in sync)"
            };

            for (int p = 0; p < 2; p++) {
                int ox = panelX[p];
                int oy = topH;
                var snap = snaps.get(snapIdx[p]);

                // Panel title
                g.setFont(new Font("SansSerif", Font.BOLD, 12));
                g.setColor(TEXT);
                drawCentred(g, panelTitles[p], ox, oy - 10, pW);

                // Column headers (entry indices)
                g.setFont(new Font("SansSerif", Font.BOLD, 11));
                for (int ei = 1; ei <= maxEntry; ei++) {
                    int cx = ox + lblW + (ei - 1) * cW;
                    g.setColor(new Color(0xf0f4f8));
                    g.fillRect(cx, oy, cW, hdrH);
                    g.setColor(TEXT_MUTED);
                    g.drawRect(cx, oy, cW - 1, hdrH - 1);
                    g.setColor(TEXT);
                    drawCentred(g, "cmd-" + ei, cx, oy + hdrH / 2 + 5, cW);
                }

                // Row labels + cells
                for (int ni = 0; ni < nodes.size(); ni++) {
                    String nid = nodes.get(ni);
                    int cy = oy + hdrH + ni * cH;
                    boolean isPartitioned = snap.partitioned().get(nid);
                    int ci = snap.commitIndex().get(nid);
                    int ls = snap.logSize().get(nid);

                    // row label
                    g.setColor(new Color(0xf0f4f8));
                    g.fillRect(ox, cy, lblW, cH);
                    g.setColor(TEXT_MUTED);
                    g.drawRect(ox, cy, lblW - 1, cH - 1);
                    g.setFont(new Font("SansSerif", Font.BOLD, 12));
                    g.setColor(TEXT);
                    drawCentred(g, nid, ox, cy + cH / 2 + 5, lblW);

                    // entry cells
                    for (int ei = 1; ei <= maxEntry; ei++) {
                        int cx = ox + lblW + (ei - 1) * cW;
                        Color bg;
                        String cellText;
                        if (ei <= ci) {
                            bg = COMMIT_CLR;
                            cellText = "committed";
                        } else if (ei <= ls) {
                            bg = PRESENT_CLR;
                            cellText = "pending";
                        } else {
                            bg = ABSENT_CLR;
                            cellText = "—";
                        }
                        g.setColor(bg);
                        g.fillRect(cx, cy, cW, cH);
                        if (isPartitioned) drawStripes(g, cx, cy, cW, cH);
                        g.setColor(ei <= ls ? Color.WHITE : TEXT_MUTED);
                        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
                        drawCentred(g, cellText, cx, cy + cH / 2 + 4, cW);
                        g.setColor(new Color(255, 255, 255, 80));
                        g.drawRect(cx, cy, cW - 1, cH - 1);
                    }
                }

                // Panel border
                g.setColor(new Color(0, 0, 0, 30));
                g.drawRect(ox, oy, pW, pH);
            }

            // Legend
            int lx = pad, ly = topH + pH + 18;
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            Object[][] legendItems = {
                {COMMIT_CLR,  "committed"},
                {PRESENT_CLR, "present (not yet committed)"},
                {ABSENT_CLR,  "absent"},
                {FOLLOWER_CLR, "partitioned node (striped)"}
            };
            for (var item : legendItems) {
                g.setColor((Color) item[0]);
                g.fillRect(lx, ly, 14, 14);
                if (item[1].toString().contains("partitioned")) drawStripes(g, lx, ly, 14, 14);
                g.setColor(TEXT);
                String txt = (String) item[1];
                g.drawString(txt, lx + 18, ly + 11);
                lx += 18 + g.getFontMetrics().stringWidth(txt) + 24;
            }

            g.dispose();
            save(img, "raft-log-state.png");
        }
    }

    // ── Visualization 4: Interactive HTML Lifecycle Chart ─────────────────

    /**
     * Generates raft-lifecycle.html — an interactive Chart.js page with two charts:
     *   - Commit index of each node over all 9 steps
     *   - Log size of each node over all 9 steps
     */
    record ExampleLifecycle() {
        void main() throws IOException {
            var snaps = runScenario();
            var labels = snaps.stream().map(s -> s.label().replace("\n", " / ")).toList();
            var ci_n1  = snaps.stream().map(s -> s.commitIndex().get("n1")).toList();
            var ci_n2  = snaps.stream().map(s -> s.commitIndex().get("n2")).toList();
            var ci_n3  = snaps.stream().map(s -> s.commitIndex().get("n3")).toList();
            var ls_n1  = snaps.stream().map(s -> s.logSize().get("n1")).toList();
            var ls_n2  = snaps.stream().map(s -> s.logSize().get("n2")).toList();
            var ls_n3  = snaps.stream().map(s -> s.logSize().get("n3")).toList();

            var html = """
                <!DOCTYPE html>
                <html>
                <head>
                  <title>Raft Cluster Lifecycle</title>
                  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
                  <style>
                    body   { font-family: system-ui, sans-serif; margin: 30px 40px;
                             background: #1a1a2e; color: #eee; }
                    h1     { text-align: center; font-size: 1.4em; margin-bottom: 4px; }
                    p      { text-align: center; color: #aaa; margin-top: 0; font-size: 0.9em; }
                    .grid  { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; margin-top: 20px; }
                    canvas { background: #16213e; border-radius: 10px; padding: 12px; }
                  </style>
                </head>
                <body>
                  <h1>Raft Cluster Lifecycle  —  3 nodes, 9 steps</h1>
                  <p>
                    Steps: initial → leader election → log replication →
                    partition (n3 down) → recovery (n3) → leader failure (n1) →
                    re-election (n2) → recovery (n1)
                  </p>
                  <div class="grid">
                    <canvas id="commitChart"></canvas>
                    <canvas id="logChart"></canvas>
                  </div>
                  <script>
                    const labels = %s;
                    const chartOpts = (title, yLabel) => ({
                      responsive: true,
                      interaction: { mode: 'index' },
                      plugins: {
                        legend: { labels: { color: '#eee' } },
                        title:  { display: true, text: title, color: '#eee', font: { size: 14 } }
                      },
                      scales: {
                        x: { ticks: { color: '#aaa', maxRotation: 30 }, grid: { color: '#ffffff11' } },
                        y: { title: { display: true, text: yLabel, color: '#aaa' },
                             ticks: { color: '#aaa', stepSize: 1 },
                             grid:  { color: '#ffffff11' }, min: 0, max: 6 }
                      }
                    });
                    const ds = (label, color, data) => ({
                      label, data,
                      borderColor: color, backgroundColor: color + '33',
                      borderWidth: 2.5, pointRadius: 5, tension: 0.15
                    });
                    new Chart(document.getElementById('commitChart'), {
                      type: 'line',
                      data: { labels, datasets: [
                        ds('n1  commit index', '#4a90d9', %s),
                        ds('n2  commit index', '#27ae60', %s),
                        ds('n3  commit index', '#e67e22', %s)
                      ]},
                      options: chartOpts('Commit Index per Node', 'committed entries')
                    });
                    new Chart(document.getElementById('logChart'), {
                      type: 'line',
                      data: { labels, datasets: [
                        ds('n1  log size', '#4a90d9', %s),
                        ds('n2  log size', '#27ae60', %s),
                        ds('n3  log size', '#e67e22', %s)
                      ]},
                      options: chartOpts('Log Size per Node', 'log entries')
                    });
                  </script>
                </body>
                </html>
                """.formatted(
                    toJson(labels),
                    toJson(ci_n1), toJson(ci_n2), toJson(ci_n3),
                    toJson(ls_n1), toJson(ls_n2), toJson(ls_n3)
                );

            var path = Path.of("raft-lifecycle.html");
            Files.writeString(path, html);
            System.out.println("Written to " + path.toAbsolutePath());
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private static Color roleColor(NodeRole role) {
        return switch (role) {
            case FOLLOWER  -> FOLLOWER_CLR;
            case CANDIDATE -> CANDIDATE_CLR;
            case LEADER    -> LEADER_CLR;
        };
    }

    /** Draws diagonal stripes over [x,y,w,h] using the current stroke/colour. */
    private static void drawStripes(Graphics2D g, int x, int y, int w, int h) {
        var oldClip = g.getClip();
        g.clipRect(x, y, w, h);
        g.setColor(new Color(0, 0, 0, 85));
        g.setStroke(new BasicStroke(1.5f));
        for (int s = -h; s <= w + h; s += 7) {
            g.drawLine(x + s, y, x + s + h, y + h);
        }
        g.setClip(oldClip);
        g.setStroke(new BasicStroke(1));
    }

    /** Draw text horizontally centred within the band [ox, ox+width]. */
    private static void drawCentred(Graphics2D g, String text, int ox, int y, int width) {
        int tw = g.getFontMetrics().stringWidth(text);
        g.drawString(text, ox + (width - tw) / 2, y);
    }

    /** Draw text horizontally centred across the full image width. */
    private static void drawCentred(Graphics2D g, String text, Font font, Color colour, int imgW, int y) {
        g.setFont(font);
        g.setColor(colour);
        int tw = g.getFontMetrics().stringWidth(text);
        g.drawString(text, (imgW - tw) / 2, y);
    }

    private static void setHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
    }

    private static BufferedImage newImage(int w, int h) {
        return new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    }

    private static void save(BufferedImage img, String filename) throws IOException {
        var path = Path.of(filename);
        ImageIO.write(img, "PNG", path.toFile());
        System.out.println("Written to " + path.toAbsolutePath());
    }

    private static String toJson(List<?> list) {
        var sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            var v = list.get(i);
            sb.append(v instanceof String s ? "\"" + s + "\"" : v);
        }
        return sb.append("]").toString();
    }
}
