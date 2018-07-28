package com.pivovarit.sealed;

public abstract class Option<T> {

    private Option() {
    }

    public final static class Some<T> extends Option<T> {

        private final T value;

        public Some(T value) {
            this.value = value;
        }
    }

    public final static class None<T> extends Option<T> {
    }
}



