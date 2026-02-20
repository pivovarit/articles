package com.pivovarit.raft;

// Response to an AppendEntries RPC.
// term: the receiver's current term (so the leader can step down if stale).
// success: true if the follower's log matched prevLogIndex / prevLogTerm and the
//          entries were appended (or already present).
record AppendEntriesResponse(int term, boolean success) {}
