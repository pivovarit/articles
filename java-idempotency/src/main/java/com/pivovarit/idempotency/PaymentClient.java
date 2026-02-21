package com.pivovarit.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class PaymentClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String host;
    private final int port;
    private final HttpClient http;

    public PaymentClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.http = HttpClient.newBuilder()
          .version(HttpClient.Version.HTTP_1_1)
          .connectTimeout(Duration.ofSeconds(5))
          .build();
    }

    /**
     * Sends a payment request without any idempotency key.
     * Retrying on failure will cause the server to process the charge again.
     */
    public PaymentResponse charge(PaymentRequest request) throws IOException, InterruptedException {
        var httpRequest = HttpRequest.newBuilder()
          .uri(URI.create("http://" + host + ":" + port + "/payments"))
          .header("Content-Type", "application/json")
          .timeout(Duration.ofSeconds(5))
          .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(request)))
          .build();

        return MAPPER.readValue(http.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body(), PaymentResponse.class);
    }

    /**
     * Sends a payment request with an idempotency key.
     * Retrying on failure with the same key is safe — the server will
     * recognize the duplicate and return the cached result without
     * charging again.
     */
    public PaymentResponse chargeIdempotently(PaymentRequest request, String idempotencyKey)
      throws IOException, InterruptedException {

        var httpRequest = HttpRequest.newBuilder()
          .uri(URI.create("http://" + host + ":" + port + "/payments"))
          .header("Content-Type", "application/json")
          .header("Idempotency-Key", idempotencyKey)
          .timeout(Duration.ofSeconds(5))
          .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(request)))
          .build();

        return MAPPER.readValue(http.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body(), PaymentResponse.class);
    }
}
