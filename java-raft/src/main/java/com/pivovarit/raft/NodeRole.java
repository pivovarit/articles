package com.pivovarit.raft;

enum NodeRole {
    FOLLOWER,   // default state — waits for heartbeats from the leader
    CANDIDATE,  // election in progress — collecting votes from peers
    LEADER      // won the election — drives log replication across the cluster
}
