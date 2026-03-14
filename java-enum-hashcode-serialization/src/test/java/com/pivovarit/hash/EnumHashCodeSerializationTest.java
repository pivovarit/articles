package com.pivovarit.hash;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class EnumHashCodeSerializationTest {

    @Test
    void enumHashCodeIsIdentityBased() {
        assertThat(Type.A.ordinal()).isEqualTo(0);
        assertThat(Type.A.name().hashCode()).isEqualTo("A".hashCode());

        assertThat(Type.A.hashCode()).isNotEqualTo(Type.A.ordinal());
        assertThat(Type.A.hashCode()).isNotEqualTo(Type.A.name().hashCode());
    }

    @Test
    void singleEntryMapWorksWithinSameJvm() {
        var map = new SingleEntryMap<>(Type.A, "hello");

        assertThat(map.get(Type.A)).contains("hello");
        assertThat(map.get(Type.B)).isEmpty();
    }

    @Test
    void serializationRoundTripWithinSameJvmWorks() throws Exception {
        var original = new SingleEntryMap<>(Type.A, "hello");
        byte[] bytes = serialize(original);

        SingleEntryMap<Type, String> deserialized = deserialize(bytes);

        assertThat(deserialized.get(Type.A)).contains("hello");
    }

    @Test
    void jdkHashMapSurvivesSerializationBecauseItRehashes() throws Exception {
        var original = new HashMap<Type, String>();
        original.put(Type.A, "hello");

        byte[] bytes = serialize(original);

        HashMap<Type, String> deserialized = deserialize(bytes);

        assertThat(deserialized.get(Type.A)).isEqualTo("hello");
    }

    @Test
    void stringKeysAreSafeBecauseHashCodeIsDeterministic() throws Exception {
        var original = new SingleEntryMap<>("key", "value");
        byte[] bytes = serialize(original);

        SingleEntryMap<String, String> deserialized = deserialize(bytes);

        assertThat(deserialized.get("key")).contains("value");
    }

    private static byte[] serialize(Object obj) throws Exception {
        var baos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        return baos.toByteArray();
    }

    private static <T> T deserialize(byte[] bytes) throws Exception {
        try (var ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (T) ois.readObject();
        }
    }
}
