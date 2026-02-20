package com.pivovarit.raft;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step 3 — Log Replication
 *
 * The leader is the sole entry point for client writes. It appends each new
 * command to its own log as a LogEntry(term, command) and then replicates it
 * to followers via AppendEntries RPCs.
 *
 * An entry is "committed" once the leader has stored it on a majority of nodes.
 * Only after committing does the leader (and eventually all followers) apply the
 * entry to the state machine — the ordered list of applied commands.
 *
 * Key properties:
 *   Log Matching  – if two logs contain an entry with the same index and term,
 *                   then all preceding entries are identical (§5.3).
 *   Linearizability – every client sees a consistent, ordered history of writes
 *                   regardless of which node it talks to.
 */
class Step3_LogReplicationTest {

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
        n1.startElection();  // n1 is the leader for all tests in this class
    }

    @Test
    void only_the_leader_accepts_writes() {
        assertThat(n1.submit("SET x=1")).isTrue();   // leader
        assertThat(n2.submit("SET x=1")).isFalse();  // follower
        assertThat(n3.submit("SET x=1")).isFalse();  // follower
    }

    @Test
    void submitted_entry_appended_to_leaders_log() {
        n1.submit("SET x=1");

        assertThat(n1.log()).hasSize(1);
        assertThat(n1.log().get(0).command()).isEqualTo("SET x=1");
        assertThat(n1.log().get(0).term()).isEqualTo(1);
    }

    @Test
    void entries_replicated_to_all_followers() {
        n1.submit("SET x=1");
        n1.submit("SET y=2");

        assertThat(n2.log()).isEqualTo(n1.log());
        assertThat(n3.log()).isEqualTo(n1.log());
        assertThat(n1.log()).hasSize(2);
    }

    @Test
    void entry_committed_once_majority_has_it() {
        // With n3 partitioned the quorum is n1 + n2 = 2/3 — still a majority
        n3.partition();

        n1.submit("SET x=1");

        assertThat(n1.commitIndex()).isEqualTo(1);  // leader committed
        assertThat(n2.commitIndex()).isEqualTo(1);  // reachable follower committed
        assertThat(n3.commitIndex()).isEqualTo(0);  // partitioned — not yet committed
    }

    @Test
    void state_machine_reflects_committed_entries_in_order() {
        n1.submit("SET x=1");
        n1.submit("SET y=2");
        n1.submit("DEL x");

        assertThat(n1.stateMachine()).containsExactly("SET x=1", "SET y=2", "DEL x");
        assertThat(n2.stateMachine()).containsExactly("SET x=1", "SET y=2", "DEL x");
        assertThat(n3.stateMachine()).containsExactly("SET x=1", "SET y=2", "DEL x");
    }

    @Test
    void follower_catches_up_after_missing_entries() {
        // n3 misses two entries while partitioned
        n3.partition();
        n1.submit("SET x=1");
        n1.submit("SET y=2");

        assertThat(n3.log()).isEmpty();

        // n3 reconnects — the leader's next heartbeat delivers all missing entries
        n3.reconnect();
        n1.heartbeat();

        assertThat(n3.log()).hasSize(2);
        assertThat(n3.log()).isEqualTo(n1.log());
        assertThat(n3.commitIndex()).isEqualTo(2);
        assertThat(n3.stateMachine()).containsExactly("SET x=1", "SET y=2");
    }

    @Test
    void all_nodes_apply_commands_in_the_same_order() {
        // Log Matching property: the same sequence of commands is applied everywhere
        n1.submit("cmd-A");
        n1.submit("cmd-B");
        n1.submit("cmd-C");

        assertThat(n1.stateMachine())
            .isEqualTo(n2.stateMachine())
            .isEqualTo(n3.stateMachine())
            .containsExactly("cmd-A", "cmd-B", "cmd-C");
    }

    @Test
    void uncommitted_stale_entry_overwritten_by_new_leader() {
        // §5.4: an entry that was never committed (no quorum) may be overwritten if a
        // new leader wins the election and replicates a different entry at the same index.
        //
        // Scenario (own cluster, separate from setUp):
        //   a (leader term 1): appends "stale" but cannot commit it (b and c are partitioned)
        //   b (leader term 2): commits "correct" at the same log index
        //   → when a reconnects, "stale" is replaced by "correct"

        var freshCluster = new RaftCluster();
        var a = new RaftNode("a");
        var b = new RaftNode("b");
        var c = new RaftNode("c");
        freshCluster.add(a);
        freshCluster.add(b);
        freshCluster.add(c);

        a.startElection();           // a is leader, term 1
        a.submit("cmd1");            // committed to a, b, c

        // Isolate b and c — a can no longer reach a majority
        b.partition();
        c.partition();

        // a appends "stale" but cannot commit it (only 1/3 nodes = no quorum)
        a.submit("stale");
        assertThat(a.log()).hasSize(2);
        assertThat(a.commitIndex()).isEqualTo(1);  // only cmd1 committed; "stale" is not

        // a crashes; b and c elect b as new leader (term 2)
        a.partition();
        b.reconnect();
        c.reconnect();
        b.startElection();
        assertThat(b.role()).isEqualTo(NodeRole.LEADER);

        // b commits a different command at log index 2
        b.submit("correct");

        // a reconnects — the new leader overwrites a's uncommitted stale entry
        a.reconnect();
        b.heartbeat();

        assertThat(a.log().get(1).command()).isEqualTo("correct");  // stale entry replaced
        assertThat(a.stateMachine()).containsExactly("cmd1", "correct");
    }
}
