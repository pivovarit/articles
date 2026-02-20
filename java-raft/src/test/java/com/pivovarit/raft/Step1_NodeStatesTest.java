package com.pivovarit.raft;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step 1 — Node States
 *
 * Every Raft node is always in exactly one of three states:
 *
 *   FOLLOWER  – the default starting state; passively accepts updates from a leader.
 *   CANDIDATE – temporarily assumed during leader election while collecting votes.
 *   LEADER    – drives the cluster; the only node that can accept writes.
 *
 * State transitions:
 *
 *   FOLLOWER  → CANDIDATE  when the election timer fires with no leader heartbeat
 *   CANDIDATE → LEADER     after winning a majority vote
 *   CANDIDATE → FOLLOWER   after seeing a higher term or losing the vote
 *   LEADER    → FOLLOWER   after seeing a higher term from another node
 */
class Step1_NodeStatesTest {

    @Test
    void new_node_starts_as_follower() {
        var node = new RaftNode("n1");

        assertThat(node.role()).isEqualTo(NodeRole.FOLLOWER);
        assertThat(node.currentTerm()).isEqualTo(0);
    }

    @Test
    void candidate_becomes_leader_after_winning_majority_vote() {
        // A single-node cluster is trivially a majority: 1 vote > 1/2
        var cluster = new RaftCluster();
        var n1 = new RaftNode("n1");
        cluster.add(n1);

        n1.startElection();

        assertThat(n1.role()).isEqualTo(NodeRole.LEADER);
        assertThat(n1.currentTerm()).isEqualTo(1);
    }

    @Test
    void candidate_stays_candidate_when_majority_is_unavailable() {
        // With 2 out of 3 peers partitioned the candidate only has 1 vote (itself),
        // which is not > 3/2 = 1, so it stays in the CANDIDATE state.
        var cluster = new RaftCluster();
        var n1 = new RaftNode("n1");
        var n2 = new RaftNode("n2");
        var n3 = new RaftNode("n3");
        cluster.add(n1);
        cluster.add(n2);
        cluster.add(n3);

        n2.partition();
        n3.partition();

        n1.startElection();

        assertThat(n1.role()).isEqualTo(NodeRole.CANDIDATE);
        assertThat(n1.currentTerm()).isEqualTo(1);
    }

    @Test
    void node_reverts_to_follower_on_higher_term() {
        // §5.1: any message carrying a higher term forces the receiver to step down.
        var cluster = new RaftCluster();
        var n1 = new RaftNode("n1");
        cluster.add(n1);

        n1.startElection();  // n1 becomes leader at term 1
        assertThat(n1.role()).isEqualTo(NodeRole.LEADER);

        // Simulate a message from a node that has advanced to a much higher term
        n1.becomeFollower(5);

        assertThat(n1.role()).isEqualTo(NodeRole.FOLLOWER);
        assertThat(n1.currentTerm()).isEqualTo(5);
    }

    @Test
    void term_increases_with_each_election_attempt() {
        // Terms are a logical clock: they increase monotonically with every election.
        var cluster = new RaftCluster();
        var n1 = new RaftNode("n1");
        var n2 = new RaftNode("n2");
        var n3 = new RaftNode("n3");
        cluster.add(n1);
        cluster.add(n2);
        cluster.add(n3);

        // Keep peers unreachable so n1 keeps losing (stays CANDIDATE)
        n2.partition();
        n3.partition();

        n1.startElection();
        assertThat(n1.currentTerm()).isEqualTo(1);

        n1.startElection();
        assertThat(n1.currentTerm()).isEqualTo(2);

        // Restore peers — n1 can now win
        n2.reconnect();
        n3.reconnect();
        n1.startElection();

        assertThat(n1.currentTerm()).isEqualTo(3);
        assertThat(n1.role()).isEqualTo(NodeRole.LEADER);
    }
}
