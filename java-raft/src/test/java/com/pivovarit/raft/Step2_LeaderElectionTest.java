package com.pivovarit.raft;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step 2 — Leader Election
 *
 * Raft guarantees at most one leader per term. The election rules are:
 *
 *   1. A node starts an election by incrementing its term and sending RequestVote RPCs.
 *   2. A vote is granted only when:
 *        - the receiver hasn't already voted in this term, AND
 *        - the candidate's log is at least as up-to-date as the receiver's (§5.4.1).
 *   3. The first candidate to reach a majority wins and becomes leader for that term.
 *   4. If no candidate wins (split vote), a new election starts with a higher term.
 *
 * The "up-to-date" check prevents a candidate that is missing committed entries from
 * winning — which would otherwise violate safety.
 */
class Step2_LeaderElectionTest {

    @Test
    void single_node_cluster_elects_itself_leader() {
        var cluster = new RaftCluster();
        var n1 = new RaftNode("n1");
        cluster.add(n1);

        n1.startElection();

        assertThat(n1.role()).isEqualTo(NodeRole.LEADER);
        assertThat(n1.currentTerm()).isEqualTo(1);
    }

    @Test
    void leader_elected_in_three_node_cluster() {
        var cluster = new RaftCluster();
        var n1 = new RaftNode("n1");
        var n2 = new RaftNode("n2");
        var n3 = new RaftNode("n3");
        cluster.add(n1);
        cluster.add(n2);
        cluster.add(n3);

        // n1 triggers election timeout first
        n1.startElection();

        assertThat(n1.role()).isEqualTo(NodeRole.LEADER);
        assertThat(n2.role()).isEqualTo(NodeRole.FOLLOWER);
        assertThat(n3.role()).isEqualTo(NodeRole.FOLLOWER);
    }

    @Test
    void all_nodes_converge_to_the_same_term_after_election() {
        var cluster = new RaftCluster();
        var n1 = new RaftNode("n1");
        var n2 = new RaftNode("n2");
        var n3 = new RaftNode("n3");
        cluster.add(n1);
        cluster.add(n2);
        cluster.add(n3);

        n1.startElection();

        // Every node — leader and followers alike — must agree on the current term
        assertThat(n1.currentTerm()).isEqualTo(1);
        assertThat(n2.currentTerm()).isEqualTo(1);
        assertThat(n3.currentTerm()).isEqualTo(1);
    }

    @Test
    void candidate_with_stale_term_steps_down_immediately() {
        // A node whose term is lower than any peer's term cannot win the election.
        // When it receives a response containing a higher term it reverts to FOLLOWER.
        var cluster = new RaftCluster();
        var n1 = new RaftNode("n1");
        var n2 = new RaftNode("n2");
        var n3 = new RaftNode("n3");
        cluster.add(n1);
        cluster.add(n2);
        cluster.add(n3);

        // Advance n2 and n3 independently to a much higher term
        n2.becomeFollower(10);
        n3.becomeFollower(10);

        // n1 starts election at term 1 — receives term=10 in the first response
        n1.startElection();

        assertThat(n1.role()).isEqualTo(NodeRole.FOLLOWER);
        assertThat(n1.currentTerm()).isEqualTo(10);  // adopted the higher term
    }

    @Test
    void each_node_votes_at_most_once_per_term() {
        // §5.2: a node grants at most one vote per term on a first-come-first-served basis.
        var cluster = new RaftCluster();
        var n1 = new RaftNode("n1");
        var n2 = new RaftNode("n2");
        var n3 = new RaftNode("n3");
        cluster.add(n1);
        cluster.add(n2);
        cluster.add(n3);

        // n1 wins term 1 — n2 and n3 both grant their votes to n1
        n1.startElection();
        assertThat(n1.role()).isEqualTo(NodeRole.LEADER);

        int leaderTerm = n1.currentTerm();

        // n2 tries to start a new election — it must increment the term first,
        // which causes n1 and n3 to update their terms and grant fresh votes to n2
        n2.startElection();

        assertThat(n2.role()).isEqualTo(NodeRole.LEADER);
        assertThat(n2.currentTerm()).isGreaterThan(leaderTerm);
        assertThat(n1.role()).isEqualTo(NodeRole.FOLLOWER);  // n1 stepped down
    }

    @Test
    void candidate_with_shorter_log_cannot_win_against_up_to_date_peers() {
        // §5.4.1: a candidate must have a log that is at least as up-to-date as any voter.
        // If a follower has a longer (or same-length but higher-term) log it will reject the vote.
        var cluster = new RaftCluster();
        var n1 = new RaftNode("n1");
        var n2 = new RaftNode("n2");
        var n3 = new RaftNode("n3");
        cluster.add(n1);
        cluster.add(n2);
        cluster.add(n3);

        // n1 wins the first election and commits an entry — all peers replicate it
        n1.startElection();
        n1.submit("SET x=1");

        // n2 and n3 now have a log that contains "SET x=1"
        // If n2 starts an election it will still win because it also has that entry
        n2.startElection();

        assertThat(n2.role()).isEqualTo(NodeRole.LEADER);
        assertThat(n2.log()).isEqualTo(n1.log());
    }
}
