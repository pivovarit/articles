package com.pivovarit.exception;

class StaticStacklessExceptionExample {

    private static final NullPointerException NULL_POINTER_EXCEPTION = new NullPointerException();

    static {
        NULL_POINTER_EXCEPTION.setStackTrace(new StackTraceElement[0]);
    }

    public static void main(String[] args) {
        throw NULL_POINTER_EXCEPTION;
    }
}
