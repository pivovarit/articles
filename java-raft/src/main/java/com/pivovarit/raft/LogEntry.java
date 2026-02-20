package com.pivovarit.raft;

// A single entry in the replicated log.
// Each entry carries the term in which it was created and the command to apply.
record LogEntry(int term, String command) {}
