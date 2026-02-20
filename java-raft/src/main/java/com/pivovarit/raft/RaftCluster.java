package com.pivovarit.raft;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/**
 * An in-memory cluster that wires RaftNodes together.
 *
 * In a real system each node would communicate over the network. Here, nodes
 * call each other's methods directly, which keeps the focus on the algorithm
 * rather than on networking concerns.
 */
class RaftCluster {

    private final LinkedHashMap<String, RaftNode> nodes = new LinkedHashMap<>();

    void add(RaftNode node) {
        nodes.put(node.id, node);
        node.setCluster(this);
    }

    /** Returns all nodes except the one with the given id. */
    List<RaftNode> peers(String nodeId) {
        return nodes.values().stream()
            .filter(n -> !n.id.equals(nodeId))
            .toList();
    }

    int size() {
        return nodes.size();
    }

    RaftNode node(String id) {
        return nodes.get(id);
    }

    Optional<RaftNode> leader() {
        return nodes.values().stream()
            .filter(n -> n.role() == NodeRole.LEADER)
            .findFirst();
    }

    Collection<RaftNode> nodes() {
        return nodes.values();
    }
}
