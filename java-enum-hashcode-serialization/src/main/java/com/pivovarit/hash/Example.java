package com.pivovarit.hash;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

class Example {
    static class Serialize {
        void main() throws Exception {
            var map = new SingleEntryMap<>(Type.A, "hello");
            try (var out = new DataOutputStream(new FileOutputStream("/tmp/map.bin"))) {
                out.writeInt(map.bucket());
                out.writeUTF(map.key().name());
                out.writeUTF(map.value());
                IO.println("Type.A.hashCode() = " + Type.A.hashCode());
            }
        }
    }

    static class Deserialize {
        void main() throws Exception {
            // Read only the plain fields we wrote (int + two Strings) instead of
            // deserializing a full object graph, avoiding unsafe object deserialization.
            try (var in = new DataInputStream(new FileInputStream("/tmp/map.bin"))) {
                int bucket = in.readInt();
                Type key = Type.valueOf(in.readUTF());
                String value = in.readUTF();
                var map = new SingleEntryMap<>(bucket, key, value);

                IO.println("Type.A.hashCode() = " + Type.A.hashCode());
                IO.println("stored bucket     = " + map.bucket());
                IO.println("map contains: " + map.key() + " -> " + map.value());
                IO.println("map.get(Type.A) = " + map.get(Type.A));
            }
        }
    }
}
