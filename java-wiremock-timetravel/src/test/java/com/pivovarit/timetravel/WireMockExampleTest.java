package com.pivovarit.timetravel;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.TestcontainersExtension;

@Testcontainers
@ExtendWith(TestcontainersExtension.class)
class WireMockExampleTest {

    @Container
    static GenericContainer<?> wiremock = new GenericContainer<>("wiremock/wiremock:3.13.1")
      .withExposedPorts(8080)
      .withCommand("--port 8080");

    @BeforeAll
    static void setup() {
        WireMock.configureFor(wiremock.getHost(), wiremock.getMappedPort(8080));
    }

    @Test
    void example() {
        WireMock.stubFor(WireMock.get("/timestamp")
          .willReturn(WireMock.aResponse()
            .withStatus(200)
            .withBody("""
              "message": "hello"
              """)));

        // ...
    }

    @Test
    void example2() {
        WireMock.stubFor(WireMock.get("/timestamp")
          .willReturn(WireMock.aResponse()
            .withStatus(200)
            .withBody("""
                "message": "hello",
                "timestamp": "%s"
              """.formatted(java.time.Instant.now())
            )));

        // ...
    }
}
