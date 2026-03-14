package com.pivovarit.hash;

import java.io.Serializable;
import java.util.Optional;

class SingleEntryMap<K, V> implements Serializable {
    private final int bucket;

    private final K key;
    private final V value;

    SingleEntryMap(K key, V value) {
        this.bucket = key.hashCode();
        this.key = key;
        this.value = value;
    }

    public Optional<V> get(K key) {
        return key.hashCode() == bucket && key.equals(this.key)
          ? Optional.of(value)
          : Optional.empty();
    }

    K key() {
        return key;
    }

    V value() {
        return value;
    }

    int bucket() {
        return bucket;
    }
}
