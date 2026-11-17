package fr.istic.service.subject;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

/** Unit tests of {@link KrokiClient} against a local fake Kroki (JDK HttpServer). */
class KrokiClientTest {

    private HttpServer server;
    private final AtomicReference<String> lastPath = new AtomicReference<>();
    private final AtomicReference<String> lastBody = new AtomicReference<>();
    private volatile int status = 200;
    private volatile byte[] reply = "<svg/>".getBytes(StandardCharsets.UTF_8);

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            lastPath.set(exchange.getRequestURI().getPath());
            lastBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(status, reply.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(reply);
            }
        });
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private KrokiClient client(int maxSource, int maxSvg) {
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
        return new KrokiClient(Optional.of(url), Duration.ofSeconds(5), maxSource, maxSvg);
    }

    @Test
    void postsSourceAndReturnsSvg() {
        byte[] svg = client(1000, 1000).renderSvg("plantuml", "@startuml\nA -> B\n@enduml");
        assertArrayEquals(reply, svg);
        assertEquals("/plantuml/svg", lastPath.get());
        assertEquals("@startuml\nA -> B\n@enduml", lastBody.get());
    }

    @Test
    void invalidSourceIsReported() {
        status = 400;
        reply = "Syntax Error? (line 2)".getBytes(StandardCharsets.UTF_8);
        KrokiException e = assertThrows(KrokiException.class, () -> client(1000, 1000).renderSvg("plantuml", "x"));
        assertEquals(KrokiException.Reason.INVALID_SOURCE, e.getReason());
    }

    @Test
    void serverErrorMeansUnavailable() {
        status = 500;
        KrokiException e = assertThrows(KrokiException.class, () -> client(1000, 1000).renderSvg("d2", "a -> b"));
        assertEquals(KrokiException.Reason.UNAVAILABLE, e.getReason());
    }

    @Test
    void limitsAreEnforced() {
        assertEquals(KrokiException.Reason.TOO_LARGE, assertThrows(KrokiException.class,
                () -> client(4, 1000).renderSvg("d2", "a -> b")).getReason());
        reply = "<svg>0123456789</svg>".getBytes(StandardCharsets.UTF_8);
        assertEquals(KrokiException.Reason.TOO_LARGE, assertThrows(KrokiException.class,
                () -> client(1000, 8).renderSvg("d2", "a -> b")).getReason());
    }

    @Test
    void notConfiguredAndUnreachable() {
        KrokiClient none = new KrokiClient(Optional.of("  "), Duration.ofSeconds(1), 1000, 1000);
        assertFalse(none.isConfigured());
        assertEquals(KrokiException.Reason.NOT_CONFIGURED,
                assertThrows(KrokiException.class, () -> none.renderSvg("d2", "a")).getReason());
        int port = server.getAddress().getPort();
        server.stop(0);
        KrokiClient down = new KrokiClient(Optional.of("http://127.0.0.1:" + port), Duration.ofSeconds(1), 1000, 1000);
        assertEquals(KrokiException.Reason.UNAVAILABLE,
                assertThrows(KrokiException.class, () -> down.renderSvg("d2", "a")).getReason());
    }
}
