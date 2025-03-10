package com.pivovarit.reactor;

class E1_ThreadLocalExample {

    public static void main(String[] args) throws InterruptedException {
        // think Map<Waiter, OrderDetails>
        ThreadLocal<OrderDetails> preferences = new ThreadLocal<>();

        Thread waiter1 = Thread.ofPlatform().start(() -> {
            preferences.set(new OrderDetails("lactose-free"));

            System.out.printf("preferences: %s%n", preferences.get());
        });

        waiter1.join();

        Thread waiter2 = Thread.ofPlatform().start(() -> {
            System.out.printf("preferences: %s%n", preferences.get());
        });

        waiter2.join();
    }

    public record OrderDetails(String preferences) {
    }
}
