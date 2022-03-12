package com.pivovarit.thread;

class ThreadSafeCounter {

    private volatile int counter = 0;
    private volatile boolean dirty = false;

    public synchronized int incrementAndGet() {
        if (dirty) {
            throw new IllegalStateException("this should never happen");
        }
        dirty = true;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        counter = counter + 1;
        dirty = false;
        return counter;
    }
}
