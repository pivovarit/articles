package com.pivovarit.hamming.vavr;

class BinaryString {
    private final String value;

    static BinaryString of(String val) {
        return new BinaryString(val);
    }

    private BinaryString(String value) {
        this.value = value;
    }

    String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BinaryString that = (BinaryString) o;

        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
