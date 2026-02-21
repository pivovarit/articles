package com.pivovarit.idempotency;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Embedded HTTP payment server that exposes POST /payments.
 *
 * <p>In non-idempotent mode every request triggers a new charge regardless of
 * whether an identical request has been seen before.
 *
 * <p>In idempotent mode the server de-duplicates requests using the
 * {@code Idempotency-Key} header: the first call with a given key processes
 * the charge and caches the result; subsequent calls with the same key return
 * the cached result without charging again.
 */
public class PaymentServer {

    private final HttpServer server;
    private final AtomicInteger chargesProcessed = new AtomicInteger(0);

    public PaymentServer(boolean idempotent) throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);

        Map<String, String> processedRequests = new ConcurrentHashMap<>();

        server.createContext("/payments", exchange -> {
            try (exchange) {
                exchange.getRequestBody().readAllBytes();

                String responseBody = getResponseBody(idempotent, exchange, processedRequests);
                if (responseBody == null) {
                    return;
                }

                byte[] bytes = responseBody.getBytes();
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
            }
        });

        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();
    }

    private String getResponseBody(boolean idempotent, HttpExchange exchange, Map<String, String> processedRequests) throws IOException {
        if (idempotent) {
            String key = exchange.getRequestHeaders().getFirst("Idempotency-Key");
            if (key == null || key.isBlank()) {
                exchange.sendResponseHeaders(400, -1);
                return null;
            }
            return processedRequests.computeIfAbsent(key, _ -> {
                chargesProcessed.incrementAndGet();
                return buildResponse();
            });
        } else {
            chargesProcessed.incrementAndGet();
            return buildResponse();
        }
    }

    private static String buildResponse() {
        return "{\"transactionId\": \"%s\", \"status\": \"success\"}".formatted(UUID.randomUUID());
    }

    public int getPort() {
        return server.getAddress().getPort();
    }

    public int getChargesProcessed() {
        return chargesProcessed.get();
    }

    public void stop() {
        server.stop(0);
    }
}
