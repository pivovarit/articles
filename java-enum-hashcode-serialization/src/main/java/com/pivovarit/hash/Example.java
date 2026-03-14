package com.pivovarit.hash;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

class Example {
    static class Serialize {
        void main() throws Exception {
            var map = new SingleEntryMap<>(Type.A, "hello");
            try (var out = new ObjectOutputStream(new FileOutputStream("/tmp/map.bin"))) {
                out.writeObject(map);
                IO.println("Type.A.hashCode() = " + Type.A.hashCode());
            }
        }
    }

    static class Deserialize {
        void main() throws Exception {
            try (var in = new ObjectInputStream(new FileInputStream("/tmp/map.bin"))) {
                SingleEntryMap<Type, String> map = (SingleEntryMap<Type, String>) in.readObject();

                IO.println("Type.A.hashCode() = " + Type.A.hashCode());
                IO.println("stored bucket     = " + map.bucket());
                IO.println("map contains: " + map.key() + " -> " + map.value());
                IO.println("map.get(Type.A) = " + map.get(Type.A));
            }
        }
    }
}
