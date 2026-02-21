package com.pivovarit.idempotency;

import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.Testcontainers;
import org.testcontainers.toxiproxy.ToxiproxyContainer;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdempotencyTest {

    private static final int NON_IDEMPOTENT_PROXY_PORT = 8666;
    private static final int IDEMPOTENT_PROXY_PORT = 8667;

    private static ToxiproxyContainer toxiproxy;
    private static PaymentServer nonIdempotentServer;
    private static PaymentServer idempotentServer;
    private static Proxy nonIdempotentProxy;
    private static Proxy idempotentProxy;

    @BeforeAll
    static void setup() throws IOException {
        nonIdempotentServer = new PaymentServer(false);
        idempotentServer = new PaymentServer(true);

        Testcontainers.exposeHostPorts(nonIdempotentServer.getPort(), idempotentServer.getPort());

        toxiproxy = new ToxiproxyContainer("ghcr.io/shopify/toxiproxy:2.9.0");
        toxiproxy.start();

        var client = new ToxiproxyClient(toxiproxy.getHost(), toxiproxy.getControlPort());

        nonIdempotentProxy = client.createProxy(
          "non-idempotent-payment",
          "0.0.0.0:" + NON_IDEMPOTENT_PROXY_PORT,
          "host.testcontainers.internal:" + nonIdempotentServer.getPort()
        );

        idempotentProxy = client.createProxy(
          "idempotent-payment",
          "0.0.0.0:" + IDEMPOTENT_PROXY_PORT,
          "host.testcontainers.internal:" + idempotentServer.getPort()
        );
    }

    @AfterAll
    static void teardown() {
        toxiproxy.stop();
        nonIdempotentServer.stop();
        idempotentServer.stop();
    }

    @Test
    void withoutIdempotency_retryResultsInDoubleCharge() throws Exception {
        var client = new PaymentClient(toxiproxy.getHost(), toxiproxy.getMappedPort(NON_IDEMPOTENT_PROXY_PORT));
        var request = new PaymentRequest("customer-1", 100);

        var toxic = nonIdempotentProxy.toxics()
          .limitData("cut-response", ToxicDirection.DOWNSTREAM, 1L);

        assertThatThrownBy(() -> client.charge(request))
          .isInstanceOf(IOException.class);

        assertThat(nonIdempotentServer.getChargesProcessed())
          .as("charge was processed even though client got an error")
          .isEqualTo(1);

        toxic.remove();
        client.charge(request);

        assertThat(nonIdempotentServer.getChargesProcessed())
          .as("non-idempotent server processed the charge twice on retry")
          .isEqualTo(2);
    }

    @Test
    void withIdempotency_retryIsSafe() throws Exception {
        var client = new PaymentClient(toxiproxy.getHost(), toxiproxy.getMappedPort(IDEMPOTENT_PROXY_PORT));
        var request = new PaymentRequest("customer-1", 100);
        String idempotencyKey = UUID.randomUUID().toString();

        var toxic = idempotentProxy.toxics()
          .limitData("cut-response", ToxicDirection.DOWNSTREAM, 1L);

        assertThatThrownBy(() -> client.chargeIdempotently(request, idempotencyKey))
          .isInstanceOf(IOException.class);

        assertThat(idempotentServer.getChargesProcessed())
          .as("charge was processed even though client got an error")
          .isEqualTo(1);

        toxic.remove();
        client.chargeIdempotently(request, idempotencyKey);

        assertThat(idempotentServer.getChargesProcessed())
          .as("idempotent server processed the charge only once despite two attempts")
          .isEqualTo(1);
    }
}
