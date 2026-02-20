package com.pivovarit.raft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A single Raft node.
 *
 * This implementation is intentionally synchronous and single-threaded so that the
 * algorithm can be demonstrated and tested without concurrency noise. In production
 * Raft the same logic runs concurrently, with RPC calls made over the network and
 * election timeouts driven by real timers.
 *
 * Persistent state (survives restarts in a real node):
 *   currentTerm, votedFor, log
 *
 * Volatile state (reset on each restart):
 *   commitIndex, lastApplied, role, nextIndex, matchIndex
 *
 * Log index convention: 1-based (index 0 means "no entry").
 * The underlying ArrayList is 0-based, so Raft index i maps to log.get(i - 1).
 */
class RaftNode {

    final String id;

    // ── Persistent state (§2) ─────────────────────────────────────────────
    private int currentTerm = 0;
    private String votedFor = null;
    private final List<LogEntry> log = new ArrayList<>();

    // ── Volatile state ────────────────────────────────────────────────────
    private int commitIndex = 0;  // highest log index known to be committed (0 = none)
    private int lastApplied = 0;  // highest log index applied to the state machine (0 = none)
    private NodeRole role = NodeRole.FOLLOWER;

    // Leader-only volatile state (reinitialized after each election)
    private Map<String, Integer> nextIndex = new HashMap<>();   // next log index to send to each peer
    private Map<String, Integer> matchIndex = new HashMap<>();  // highest replicated index per peer

    // ── Simulation helpers ────────────────────────────────────────────────
    boolean partitioned = false;  // when true, the node drops all incoming/outgoing messages

    private final List<String> stateMachine = new ArrayList<>();  // ordered list of applied commands

    private RaftCluster cluster;

    RaftNode(String id) {
        this.id = id;
    }

    void setCluster(RaftCluster cluster) {
        this.cluster = cluster;
    }

    // ── Network simulation ────────────────────────────────────────────────

    void partition() { this.partitioned = true; }
    void reconnect() { this.partitioned = false; }

    // ── §5.2 Leader Election ──────────────────────────────────────────────

    /**
     * Called when the election timeout fires.
     * The node increments its term, becomes a candidate, votes for itself, then
     * asks every peer for a vote. If it collects a majority it becomes the leader.
     * If it receives a higher term in any response it immediately reverts to follower.
     * If it finishes without a majority it stays CANDIDATE (split vote).
     */
    void startElection() {
        if (partitioned) return;

        currentTerm++;
        role = NodeRole.CANDIDATE;
        votedFor = id;  // vote for self
        int votes = 1;

        int lastLogIndex = log.size();
        int lastLogTerm = lastLogIndex > 0 ? log.get(lastLogIndex - 1).term() : 0;
        var request = new VoteRequest(currentTerm, id, lastLogIndex, lastLogTerm);

        for (var peer : cluster.peers(id)) {
            var response = peer.handleVoteRequest(request);
            if (response.term() > currentTerm) {
                becomeFollower(response.term());  // stale — step down immediately
                return;
            }
            if (response.voteGranted()) {
                votes++;
            }
        }

        if (votes > cluster.size() / 2) {
            becomeLeader();
        }
        // else: split vote — caller is responsible for retrying after a new timeout
    }

    /**
     * §5.2, §5.4.1: Grant a vote if:
     *   1. The request term is at least as large as our own term.
     *   2. We haven't already voted for someone else in this term.
     *   3. The candidate's log is at least as up-to-date as ours (log safety check).
     *
     * "Up-to-date" means: higher last-log-term, or same last-log-term but longer log.
     */
    VoteResponse handleVoteRequest(VoteRequest req) {
        if (partitioned) return new VoteResponse(currentTerm, false);

        if (req.term() < currentTerm) {
            return new VoteResponse(currentTerm, false);
        }
        if (req.term() > currentTerm) {
            becomeFollower(req.term());
        }

        int lastLogIndex = log.size();
        int lastLogTerm = lastLogIndex > 0 ? log.get(lastLogIndex - 1).term() : 0;
        boolean logUpToDate = req.lastLogTerm() > lastLogTerm
            || (req.lastLogTerm() == lastLogTerm && req.lastLogIndex() >= lastLogIndex);

        if ((votedFor == null || votedFor.equals(req.candidateId())) && logUpToDate) {
            votedFor = req.candidateId();
            return new VoteResponse(currentTerm, true);
        }
        return new VoteResponse(currentTerm, false);
    }

    private void becomeLeader() {
        role = NodeRole.LEADER;
        nextIndex = new HashMap<>();
        matchIndex = new HashMap<>();
        // Optimistically assume peers are up-to-date; back up on rejection (§5.3)
        for (var peer : cluster.peers(id)) {
            nextIndex.put(peer.id, log.size() + 1);
            matchIndex.put(peer.id, 0);
        }
        heartbeat();  // assert leadership by immediately sending empty AppendEntries
    }

    void becomeFollower(int term) {
        currentTerm = term;
        role = NodeRole.FOLLOWER;
        votedFor = null;
    }

    // ── §5.3 Log Replication ──────────────────────────────────────────────

    /**
     * Send AppendEntries to every peer (carries pending entries or acts as heartbeat).
     * In a real system this runs on a timer; here it must be called explicitly.
     */
    void heartbeat() {
        if (role != NodeRole.LEADER || partitioned) return;
        for (var peer : cluster.peers(id)) {
            replicateTo(peer);
        }
    }

    /**
     * Accept a new client command (leader only).
     * Appends the entry to the leader's log, replicates to all reachable followers,
     * commits once a majority has acknowledged, then propagates the updated
     * leaderCommit so followers can advance their own commit index.
     */
    boolean submit(String command) {
        if (role != NodeRole.LEADER || partitioned) return false;
        log.add(new LogEntry(currentTerm, command));

        // Round 1: replicate new entry to all reachable peers
        for (var peer : cluster.peers(id)) {
            replicateTo(peer);
        }
        // Commit on leader once a quorum has the entry
        advanceCommitIndex();

        // Round 2: heartbeat carrying the updated leaderCommit so followers apply the entry
        for (var peer : cluster.peers(id)) {
            replicateTo(peer);
        }
        return true;
    }

    /**
     * Push the leader's log to a single peer, backing up nextIndex until the
     * follower's prevLog check passes, then advancing matchIndex on success.
     *
     * Loop exits when:
     *   - The follower accepts (success = true)
     *   - The follower reports a higher term (we step down)
     *   - The peer is unreachable (partitioned)
     */
    private void replicateTo(RaftNode peer) {
        while (role == NodeRole.LEADER && !partitioned && !peer.partitioned) {
            int ni = nextIndex.get(peer.id);
            int prevLogIndex = ni - 1;
            int prevLogTerm = prevLogIndex > 0 ? log.get(prevLogIndex - 1).term() : 0;
            var entries = new ArrayList<>(log.subList(ni - 1, log.size()));

            var req = new AppendEntriesRequest(currentTerm, id, prevLogIndex, prevLogTerm, entries, commitIndex);
            var resp = peer.handleAppendEntries(req);

            if (resp.term() > currentTerm) {
                becomeFollower(resp.term());
                return;
            }
            if (resp.success()) {
                nextIndex.put(peer.id, log.size() + 1);
                matchIndex.put(peer.id, log.size());
                return;
            } else {
                // §5.3: follower log inconsistency — back up by one and retry
                if (ni <= 1) return;  // safety guard: prevLogIndex=0 always succeeds
                nextIndex.put(peer.id, ni - 1);
            }
        }
    }

    /**
     * §5.3, §5.4.2: Advance commitIndex to the highest N such that:
     *   - a majority of nodes have matchIndex ≥ N, AND
     *   - log[N].term == currentTerm
     *
     * The second condition (leader completeness) prevents a new leader from directly
     * committing entries from a previous term that might later be overwritten by an
     * even newer leader. Entries from prior terms are committed implicitly once a
     * current-term entry that follows them is committed.
     */
    private void advanceCommitIndex() {
        for (int n = log.size(); n > commitIndex; n--) {
            if (log.get(n - 1).term() != currentTerm) continue;  // §5.4.2 safety
            int count = 1;  // self
            for (var peer : cluster.peers(id)) {
                if (matchIndex.getOrDefault(peer.id, 0) >= n) count++;
            }
            if (count > cluster.size() / 2) {
                commitIndex = n;
                applyEntries();
                break;
            }
        }
    }

    /**
     * §5.3: Process an AppendEntries RPC from the leader.
     *
     * Steps:
     *   1. Reject if term is stale.
     *   2. Accept the sender as the current leader (reset to follower).
     *   3. Verify log consistency at prevLogIndex.
     *   4. Merge incoming entries, truncating any conflicts.
     *   5. Advance commitIndex using leaderCommit.
     */
    AppendEntriesResponse handleAppendEntries(AppendEntriesRequest req) {
        if (partitioned) return new AppendEntriesResponse(currentTerm, false);

        // Step 1: reject stale leaders (§5.1)
        if (req.term() < currentTerm) {
            return new AppendEntriesResponse(currentTerm, false);
        }

        // Step 2: valid leader — reset to follower and update term
        becomeFollower(req.term());

        // Step 3: log consistency check (§5.3)
        if (req.prevLogIndex() > 0) {
            if (log.size() < req.prevLogIndex()
                || log.get(req.prevLogIndex() - 1).term() != req.prevLogTerm()) {
                return new AppendEntriesResponse(currentTerm, false);
            }
        }

        // Step 4: merge entries — overwrite any conflicting entries and append new ones (§5.3)
        for (int i = 0; i < req.entries().size(); i++) {
            int logIdx = req.prevLogIndex() + i + 1;  // 1-based Raft index
            LogEntry entry = req.entries().get(i);
            if (logIdx <= log.size()) {
                if (log.get(logIdx - 1).term() != entry.term()) {
                    // Conflict: delete this entry and everything after it, then append
                    log.subList(logIdx - 1, log.size()).clear();
                    log.add(entry);
                }
                // else: identical entry already present — no-op
            } else {
                log.add(entry);
            }
        }

        // Step 5: advance commit index (§5.3)
        if (req.leaderCommit() > commitIndex) {
            commitIndex = Math.min(req.leaderCommit(), log.size());
            applyEntries();
        }

        return new AppendEntriesResponse(currentTerm, true);
    }

    /** Apply all committed but not yet applied entries to the state machine in order. */
    private void applyEntries() {
        while (lastApplied < commitIndex) {
            lastApplied++;
            stateMachine.add(log.get(lastApplied - 1).command());
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────

    String id() { return id; }
    int currentTerm() { return currentTerm; }
    NodeRole role() { return role; }
    int commitIndex() { return commitIndex; }
    List<LogEntry> log() { return Collections.unmodifiableList(log); }
    List<String> stateMachine() { return Collections.unmodifiableList(stateMachine); }
}
