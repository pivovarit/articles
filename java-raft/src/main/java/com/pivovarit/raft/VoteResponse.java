package com.pivovarit.raft;

// Response to a RequestVote RPC.
// term: the receiver's current term (so the candidate can update itself if stale).
// voteGranted: true if the receiver voted for the candidate.
record VoteResponse(int term, boolean voteGranted) {}
