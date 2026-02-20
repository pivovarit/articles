package com.pivovarit.raft;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step 4 — Fault Tolerance
 *
 * Raft tolerates the failure of up to ⌊(n−1)/2⌋ nodes. In a 3-node cluster any
 * single failure leaves 2 nodes alive — still a majority.
 *
 * The system provides two guarantees:
 *
 *   Safety   – the cluster never returns an inconsistent result, even during or
 *               immediately after a failure.
 *   Liveness – the cluster keeps making progress as long as a majority is healthy
 *               and can communicate.
 *
 * Key scenarios demonstrated here:
 *   1. Continued availability despite a minority failure.
 *   2. Automatic leader re-election after the current leader fails.
 *   3. A recovered node catching up with entries it missed.
 *   4. An old leader stepping down when it rejoins a cluster with a higher term.
 *   5. Safety guarantee: no committed entry is ever lost across a leader change.
 */
class Step4_FaultToleranceTest {

    private RaftCluster cluster;
    private RaftNode n1, n2, n3;

    @BeforeEach
    void setUp() {
        cluster = new RaftCluster();
        n1 = new RaftNode("n1");
        n2 = new RaftNode("n2");
        n3 = new RaftNode("n3");
        cluster.add(n1);
        cluster.add(n2);
        cluster.add(n3);
        n1.startElection();  // n1 is the leader, term 1
    }

    // ── Availability ──────────────────────────────────────────────────────

    @Test
    void cluster_remains_available_with_one_node_down() {
        // n1 + n2 = 2/3 — still a majority, so writes can be committed
        n3.partition();

        assertThat(n1.submit("SET x=1")).isTrue();
        assertThat(n1.commitIndex()).isEqualTo(1);
        assertThat(n2.commitIndex()).isEqualTo(1);
    }

    // ── Leader Re-election ────────────────────────────────────────────────

    @Test
    void new_leader_elected_after_current_leader_is_partitioned() {
        n1.submit("SET x=1");

        // Simulate leader crash / network partition
        n1.partition();

        // n2 detects missing heartbeats and starts an election
        n2.startElection();

        assertThat(n2.role()).isEqualTo(NodeRole.LEADER);
        assertThat(n2.currentTerm()).isGreaterThan(1);
        assertThat(n3.role()).isEqualTo(NodeRole.FOLLOWER);
    }

    @Test
    void new_leader_continues_accepting_writes_after_old_leader_fails() {
        n1.submit("SET x=1");

        n1.partition();
        n2.startElection();  // n2 becomes leader (term 2)

        n2.submit("SET y=2");

        assertThat(n2.commitIndex()).isEqualTo(2);
        assertThat(n3.stateMachine()).containsExactly("SET x=1", "SET y=2");
    }

    // ── Recovery ──────────────────────────────────────────────────────────

    @Test
    void reconnected_node_catches_up_with_missed_entries() {
        // n3 is partitioned and misses two committed entries
        n3.partition();
        n1.submit("SET x=1");
        n1.submit("SET y=2");

        assertThat(n3.log()).isEmpty();

        // n3 comes back online — the next heartbeat delivers all missing entries
        n3.reconnect();
        n1.heartbeat();

        assertThat(n3.log()).hasSize(2);
        assertThat(n3.log()).isEqualTo(n1.log());
        assertThat(n3.stateMachine()).containsExactly("SET x=1", "SET y=2");
    }

    @Test
    void old_leader_steps_down_after_reconnecting_to_higher_term_cluster() {
        // n1 is partitioned at term 1
        n1.partition();

        // The remaining nodes elect n2 as the new leader (term 2)
        n2.startElection();
        assertThat(n2.currentTerm()).isEqualTo(2);

        // n1 reconnects and receives an AppendEntries from n2 carrying term 2
        n1.reconnect();
        n2.heartbeat();

        // n1 sees term 2 > its own term 1 and immediately steps down
        assertThat(n1.role()).isEqualTo(NodeRole.FOLLOWER);
        assertThat(n1.currentTerm()).isEqualTo(2);
    }

    // ── Safety ────────────────────────────────────────────────────────────

    @Test
    void committed_entries_are_never_lost_across_a_leader_change() {
        // §5.4 Leader Completeness: any entry committed under one leader must appear
        // in the log of every future leader.

        // Commit an entry under n1 — all three nodes store it
        n1.submit("SET x=1");

        // n1 fails — its replacement must win because it has the committed entry
        n1.partition();
        n2.startElection();

        assertThat(n2.log().get(0).command()).isEqualTo("SET x=1");  // not lost!

        // Continue writing under the new leader
        n2.submit("SET y=2");

        // Reconnect n1 — it catches up via the new leader's heartbeat
        n1.reconnect();
        n2.heartbeat();

        // All nodes converge on the complete, consistent history
        assertThat(n1.stateMachine()).containsExactly("SET x=1", "SET y=2");
        assertThat(n2.stateMachine()).containsExactly("SET x=1", "SET y=2");
        assertThat(n3.stateMachine()).containsExactly("SET x=1", "SET y=2");
    }

    @Test
    void stale_leader_cannot_commit_entries_without_a_quorum() {
        // A partitioned leader (n1) is isolated from n2 and n3.
        // It can append entries to its own log but cannot reach a majority,
        // so nothing it appends gets committed.

        n2.partition();
        n3.partition();

        // n1 tries to submit but cannot reach a majority
        n1.submit("SET x=isolated");

        // The entry is in n1's log but was NOT committed
        assertThat(n1.log()).hasSize(1);
        assertThat(n1.commitIndex()).isEqualTo(0);  // never committed

        // Meanwhile n2 and n3 (with quorum between themselves) elect a new leader
        n2.reconnect();
        n3.reconnect();
        n2.startElection();  // n2 becomes leader (term 2)

        assertThat(n2.role()).isEqualTo(NodeRole.LEADER);

        // n2 commits a fresh entry
        n2.submit("SET x=committed");

        // When n1 reconnects, the new leader overwrites n1's uncommitted stale entry
        n1.reconnect();
        n2.heartbeat();

        assertThat(n1.stateMachine()).containsExactly("SET x=committed");
        assertThat(n1.log().get(0).command()).isEqualTo("SET x=committed");
    }
}
