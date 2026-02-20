package com.pivovarit.raft;

import java.util.List;

// AppendEntries RPC — sent by the LEADER to replicate log entries and as a heartbeat.
// When entries is empty it acts as a heartbeat (no new log entries, but leaderCommit
// still advances the follower's commit index).
record AppendEntriesRequest(
    int term,           // leader's current term
    String leaderId,    // so followers can redirect clients
    int prevLogIndex,   // index of the log entry immediately before the new ones (1-based)
    int prevLogTerm,    // term of that entry — follower rejects if it doesn't match
    List<LogEntry> entries,   // new entries to append (empty for heartbeat)
    int leaderCommit    // leader's current commitIndex
) {}
