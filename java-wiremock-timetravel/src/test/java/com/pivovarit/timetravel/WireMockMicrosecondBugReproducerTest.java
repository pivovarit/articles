package com.pivovarit.timetravel;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ExtendWith(org.testcontainers.junit.jupiter.TestcontainersExtension.class)
class WireMockMicrosecondBugReproducerTest {

    @Container
    static GenericContainer<?> wiremock = new GenericContainer<>("wiremock/wiremock:3.13.1")
      .withExposedPorts(8080)
      .withCommand("--global-response-templating --verbose --port 8080");

    @BeforeAll
    static void setup() {
        WireMock.configureFor(wiremock.getHost(), wiremock.getMappedPort(8080));
    }

    @Test
    void e1_imprintedTimestamp() throws IOException, InterruptedException {
        var baseUrl = "http://%s:%d".formatted(wiremock.getHost(), wiremock.getMappedPort(8080));
        WireMock.stubFor(WireMock.get("/timestamp")
          .willReturn(WireMock.aResponse()
            .withStatus(200)
            .withBody("""
              "value": "%s"
              """.formatted(Instant.now()))));

        try (var client = HttpClient.newHttpClient()) {
            System.out.println(client.send(HttpRequest.newBuilder()
              .uri(URI.create(baseUrl + "/timestamp"))
              .header("Accept", "application/json")
              .build(), HttpResponse.BodyHandlers.ofString()).body());
        }
    }

    @Test
    void e2_imprintedTimestampAfterDelay() throws IOException, InterruptedException {
        WireMock.stubFor(WireMock.get("/timestamp")
          .willReturn(WireMock.aResponse()
            .withStatus(200)
            .withBody("""
              "value": "%s
              """
              .formatted(Instant.now()))));

        var baseUrl = "http://%s:%d".formatted(wiremock.getHost(), wiremock.getMappedPort(8080));

        try (var client = HttpClient.newHttpClient()) {
            System.out.println(client.send(HttpRequest.newBuilder()
              .uri(URI.create(baseUrl + "/timestamp"))
              .header("Accept", "application/json")
              .build(), HttpResponse.BodyHandlers.ofString()).body());

            Thread.sleep(1000);

            System.out.println(client.send(HttpRequest.newBuilder()
              .uri(URI.create(baseUrl + "/timestamp"))
              .header("Accept", "application/json")
              .build(), HttpResponse.BodyHandlers.ofString()).body());
        }

        // "value": "2025-07-08T17:30:13.415723Z
        // "value": "2025-07-08T17:30:13.415723Z
    }

    @Test
    void e3_wrongOrdering() throws IOException, InterruptedException {
        WireMock.stubFor(WireMock.get("/timestamp")
          .willReturn(WireMock.aResponse()
            .withStatus(200)
            .withBody("""
              "value":"%s"
              """
              .formatted(Instant.now()))));

        Set<Message> log = new HashSet<>();

        log.add(new Message(Instant.now(), "first"));

        var baseUrl = "http://%s:%d".formatted(wiremock.getHost(), wiremock.getMappedPort(8080));

        try (var client = HttpClient.newHttpClient()) {
            var json = client.send(HttpRequest.newBuilder()
              .uri(URI.create(baseUrl + "/timestamp"))
              .header("Accept", "application/json")
              .build(), HttpResponse.BodyHandlers.ofString()).body();

            log.add(new Message(parseKeyAsInstant(json.trim(), "value"), "second"));
        }

        log.stream()
          .sorted(Comparator.comparing(Message::timestamp))
          .forEach(System.out::println);
    }

    @Test
    void e4_wrongOrderingWhenWiremockTemplateUsed() throws IOException, InterruptedException {
        var baseUrl = "http://%s:%d".formatted(wiremock.getHost(), wiremock.getMappedPort(8080));
        WireMock.stubFor(WireMock.get("/timestamp")
          .willReturn(WireMock.aResponse()
            .withStatus(200)
            .withBody("""
              "value":"{{now}}"
              """)));

        Set<Message> log = new HashSet<>();

        log.add(new Message(Instant.now(), "first"));

        // Message[timestamp=2025-07-08T19:14:48Z, value=second]
        // Message[timestamp=2025-07-08T19:14:48.722149Z, value=first]

        try (var client = HttpClient.newHttpClient()) {
            var json = client.send(HttpRequest.newBuilder()
              .uri(URI.create(baseUrl + "/timestamp"))
              .header("Accept", "application/json")
              .build(), HttpResponse.BodyHandlers.ofString()).body();

            log.add(new Message(parseKeyAsInstant(json, "value"), "second"));
        }

        log.stream()
          .sorted(Comparator.comparing(Message::timestamp))
          .forEach(System.out::println);
    }

    @Test
    void e5_wrongOrderingWhenCustomWiremockTemplateUsed() throws IOException, InterruptedException {
        var baseUrl = "http://%s:%d".formatted(wiremock.getHost(), wiremock.getMappedPort(8080));
        WireMock.stubFor(WireMock.get("/timestamp")
          .willReturn(WireMock.aResponse()
            .withStatus(200)
            .withBody("""
              "value":"{{now format="yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"}}"
              """)));

        Set<Message> log = new HashSet<>();

        // Message[timestamp=2025-07-08T19:58:08.000519Z, value=second]
        // Message[timestamp=2025-07-08T19:58:08.433615Z, value=first]

        // Message[timestamp=2025-07-08T20:00:37.000086Z, value=second]
        // Message[timestamp=2025-07-08T20:00:37.003508Z, value=first]

        // Message[timestamp=2025-07-08T20:01:05.000155Z, value=second]
        // Message[timestamp=2025-07-08T20:01:05.062106Z, value=first]

        // Message[timestamp=2025-07-08T20:01:25.000413Z, value=second]
        // Message[timestamp=2025-07-08T20:01:25.326382Z, value=first]

        log.add(new Message(Instant.now(), "first"));

        try (var client = HttpClient.newHttpClient()) {
            var json = client.send(HttpRequest.newBuilder()
              .uri(URI.create(baseUrl + "/timestamp"))
              .header("Accept", "application/json")
              .build(), HttpResponse.BodyHandlers.ofString()).body();

            log.add(new Message(parseKeyAsInstant(json, "value"), "second"));
        }

        log.stream()
          .sorted(Comparator.comparing(Message::timestamp))
          .forEach(System.out::println);
    }

    @Test
    void e6_correctOrderingWhenCustomWiremockTemplateUsed() throws IOException, InterruptedException {
        var baseUrl = "http://%s:%d".formatted(wiremock.getHost(), wiremock.getMappedPort(8080));
        WireMock.stubFor(WireMock.get("/timestamp")
          .willReturn(WireMock.aResponse()
            .withStatus(200)
            .withBody("""
              "value":"{{now format="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"}}"
              """)));

        Set<Message> log = new HashSet<>();

        log.add(new Message(Instant.now(), "first"));

        try (var client = HttpClient.newHttpClient()) {
            var json = client.send(HttpRequest.newBuilder()
              .uri(URI.create(baseUrl + "/timestamp"))
              .header("Accept", "application/json")
              .build(), HttpResponse.BodyHandlers.ofString()).body();

            log.add(new Message(parseKeyAsInstant(json, "value"), "second"));
        }

        log.stream()
          .sorted(Comparator.comparing(Message::timestamp))
          .forEach(System.out::println);
    }

    @Test
    void e7_coreIssue() {
        var date = new Date(Instant.parse("2025-07-07T15:23:11.123000Z").toEpochMilli());

        var formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        assertThat(formatter.format(date)).isEqualTo("2025-07-07T15:23:11.000123Z");
    }

    @Test
    void e7_coreIssue_date_time_format() {
        var instant = Instant.parse("2025-07-07T15:23:11.123000Z");
        var date = new Date(instant.toEpochMilli());

        var simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        var simpleDateFormatter = simpleDateFormat.format(date);
        var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
          .format(instant.atZone(ZoneOffset.UTC));

        assertThat(simpleDateFormatter).isEqualTo("2025-07-07T15:23:11.123Z");
        assertThat(dateTimeFormatter).isEqualTo("2025-07-07T15:23:11.123000Z");
    }

    public record Message(Instant timestamp, String value) {
    }

    public Instant parseKeyAsInstant(String json, String key) {
        var start = json.indexOf("%s\":\"".formatted(key)) + ("%s\":\"".formatted(key)).length();
        return Instant.parse(json.substring(start, json.indexOf("\"", start)));
    }
}
