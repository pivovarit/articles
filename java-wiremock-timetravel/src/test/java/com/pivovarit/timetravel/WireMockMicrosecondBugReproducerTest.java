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
import java.net.http.HttpResponse;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
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
            System.out.println(client.send(java.net.http.HttpRequest.newBuilder()
              .uri(URI.create(baseUrl + "/timestamp"))
              .header("Accept", "application/json")
              .build(), HttpResponse.BodyHandlers.ofString()).body());
        }
    }

    @Test
    void e2_imprintedTimestampAfterDelay() throws IOException, InterruptedException {
        var baseUrl = "http://%s:%d".formatted(wiremock.getHost(), wiremock.getMappedPort(8080));
        WireMock.stubFor(WireMock.get("/timestamp")
          .willReturn(WireMock.aResponse()
            .withStatus(200)
            .withBody("""
              "value": "%s"""
              .formatted(Instant.now()))));

        try (var client = HttpClient.newHttpClient()) {
            System.out.println(client.send(java.net.http.HttpRequest.newBuilder()
              .uri(URI.create(baseUrl + "/timestamp"))
              .header("Accept", "application/json")
              .build(), HttpResponse.BodyHandlers.ofString()).body());

            Thread.sleep(1000);

            System.out.println(client.send(java.net.http.HttpRequest.newBuilder()
              .uri(URI.create(baseUrl + "/timestamp"))
              .header("Accept", "application/json")
              .build(), HttpResponse.BodyHandlers.ofString()).body());
        }
    }

    @Test
    void e3_wrongOrdering() throws IOException, InterruptedException {
        var baseUrl = "http://%s:%d".formatted(wiremock.getHost(), wiremock.getMappedPort(8080));
        WireMock.stubFor(WireMock.get("/timestamp")
          .willReturn(WireMock.aResponse()
            .withStatus(200)
            .withBody("""
              "value": "%s"
              """
              .formatted(Instant.now()))));

        Set<Operation> log = new HashSet<>();

        log.add(new Operation(Instant.now(), "first"));

        try (var client = HttpClient.newHttpClient()) {
            var json = client.send(java.net.http.HttpRequest.newBuilder()
              .uri(URI.create(baseUrl + "/timestamp"))
              .header("Accept", "application/json")
              .build(), HttpResponse.BodyHandlers.ofString()).body();

            log.add(new Operation(parseKeyAsInstant(json, "value"), "second"));
        }

        log.stream()
          .sorted(Comparator.comparing(Operation::timestamp))
          .forEach(System.out::println);
    }

    @Test
    void e4_wrongOrderingWhenWiremockTemplateUsed() throws IOException, InterruptedException {
        var baseUrl = "http://%s:%d".formatted(wiremock.getHost(), wiremock.getMappedPort(8080));
        WireMock.stubFor(WireMock.get("/timestamp")
          .willReturn(WireMock.aResponse()
            .withStatus(200)
            .withBody("""
              "value": "{{now}}"
              """)));

        Set<Operation> log = new HashSet<>();

        log.add(new Operation(Instant.now(), "first"));

        try (var client = HttpClient.newHttpClient()) {
            var json = client.send(java.net.http.HttpRequest.newBuilder()
              .uri(URI.create(baseUrl + "/timestamp"))
              .header("Accept", "application/json")
              .build(), HttpResponse.BodyHandlers.ofString()).body();

            log.add(new Operation(parseKeyAsInstant(json, "value"), "second"));
        }

        log.stream()
          .sorted(Comparator.comparing(Operation::timestamp))
          .forEach(System.out::println);
    }

    @Test
    void e5_wrongOrderingWhenCustomWiremockTemplateUsed() throws IOException, InterruptedException {
        var baseUrl = "http://%s:%d".formatted(wiremock.getHost(), wiremock.getMappedPort(8080));
        WireMock.stubFor(WireMock.get("/timestamp")
          .willReturn(WireMock.aResponse()
            .withStatus(200)
            .withBody("""
              "value": "{{now format="yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"}}"
              """)));

        Set<Operation> log = new HashSet<>();

        log.add(new Operation(Instant.now(), "first"));

        try (var client = HttpClient.newHttpClient()) {
            var json = client.send(java.net.http.HttpRequest.newBuilder()
              .uri(URI.create(baseUrl + "/timestamp"))
              .header("Accept", "application/json")
              .build(), HttpResponse.BodyHandlers.ofString()).body();

            log.add(new Operation(parseKeyAsInstant(json, "value"), "second"));
        }

        log.stream()
          .sorted(Comparator.comparing(Operation::timestamp))
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

        Set<Operation> log = new HashSet<>();

        log.add(new Operation(Instant.now(), "first"));

        try (var client = HttpClient.newHttpClient()) {
            var json = client.send(java.net.http.HttpRequest.newBuilder()
              .uri(URI.create(baseUrl + "/timestamp"))
              .header("Accept", "application/json")
              .build(), HttpResponse.BodyHandlers.ofString()).body();

            log.add(new Operation(parseKeyAsInstant(json, "value"), "second"));
        }

        log.stream()
          .sorted(Comparator.comparing(Operation::timestamp))
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

    public record Operation(Instant timestamp, String value) {

    }

    public Instant parseKeyAsInstant(String json, String key) {
        var start = json.indexOf("%s\":\"".formatted(key)) + ("%s\":\"".formatted(key)).length();
        return Instant.parse(json.substring(start, json.indexOf("\"", start)));
    }
}
