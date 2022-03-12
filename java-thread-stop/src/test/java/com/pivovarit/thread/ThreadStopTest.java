package com.pivovarit.thread;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThreadStopTest {

    @Test
    void should_corrupt_threadsafe_data_structure() {
        assertThatThrownBy(() -> {
            var threadSafeCounter = new ThreadSafeCounter();

            var t1 = new Thread(() -> {
                while (true) {
                    threadSafeCounter.incrementAndGet();
                }
            });

            t1.start();
            Thread.sleep(500);
            t1.stop();

            threadSafeCounter.incrementAndGet();
        })
          .isExactlyInstanceOf(IllegalStateException.class)
          .hasMessage("this should never happen");
    }

    @Test
    void should_not_corrupt_threadsafe_data_structure() throws InterruptedException {
        var threadSafeCounter = new ThreadSafeCounter();

        var t1 = new Thread(() -> {
            while (!Thread.interrupted()) {
                threadSafeCounter.incrementAndGet();
            }
        });

        t1.start();
        Thread.sleep(500);
        t1.interrupt();

        assertThat(threadSafeCounter.incrementAndGet()).isGreaterThan(0);
    }

    @Test
    void should_not_corrupt_threadsafe_data_structure_interrupted() throws InterruptedException {
        var threadSafeCounter = new ThreadSafeCounter();

        var t1 = new Thread(() -> {
            while (!Thread.interrupted()) {
                threadSafeCounter.incrementAndGet();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        t1.start();
        Thread.sleep(500);
        t1.interrupt();

        assertThat(threadSafeCounter.incrementAndGet()).isGreaterThan(0);
    }
}
