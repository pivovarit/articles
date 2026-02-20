package com.pivovarit.raft;

import org.junit.jupiter.api.Test;

/**
 * Generates all Raft visualizations into the module root directory.
 *
 *   raft-roles-timeline.png  – node-role colour grid across the 9-step lifecycle
 *   raft-commit-progress.png – commit-index line chart per node
 *   raft-log-state.png       – log-entry grid at two key moments (partition / recovery)
 *   raft-lifecycle.html      – interactive Chart.js page (commit index + log size)
 */
class RaftVisualizationsTest {

    @Test
    void roles_timeline() throws Exception {
        new RaftVisualizations.ExampleRoleTimeline().main();
    }

    @Test
    void commit_progress() throws Exception {
        new RaftVisualizations.ExampleCommitProgress().main();
    }

    @Test
    void log_state_grid() throws Exception {
        new RaftVisualizations.ExampleLogStateGrid().main();
    }

    @Test
    void lifecycle_html() throws Exception {
        new RaftVisualizations.ExampleLifecycle().main();
    }
}
