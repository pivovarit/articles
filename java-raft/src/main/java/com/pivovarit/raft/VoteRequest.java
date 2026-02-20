package com.pivovarit.raft;

// RequestVote RPC — sent by a CANDIDATE to collect votes from peers.
// lastLogIndex / lastLogTerm allow peers to check whether the candidate's
// log is at least as up-to-date as their own (§5.4.1).
record VoteRequest(int term, String candidateId, int lastLogIndex, int lastLogTerm) {}
