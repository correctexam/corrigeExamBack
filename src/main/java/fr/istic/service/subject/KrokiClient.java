package fr.istic.service.subject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Client of a self-hosted Kroki server (T25c, ADR 0004): {@code POST {url}/{kind}/svg} with the diagram source.
 *
 * <p>The API only relays text and stores the SVG it receives: no rendering happens in the JVM (ADR 0001). Kroki must
 * run without outbound network and in safe mode ({@code KROKI_SAFE_MODE=secure}), so that a source cannot include
 * files or URLs.</p>
 */
@ApplicationScoped
public class KrokiClient {

    private final Logger log = LoggerFactory.getLogger(KrokiClient.class);

    private final Optional<String> url;
    private final Duration timeout;
    private final int maxSourceBytes;
    private final int maxSvgBytes;
    private final HttpClient http;

    @Inject
    public KrokiClient(
            @ConfigProperty(name = "correctexam.kroki.url") Optional<String> url,
            @ConfigProperty(name = "correctexam.kroki.timeout", defaultValue = "PT15S") Duration timeout,
            @ConfigProperty(name = "correctexam.kroki.max-source-bytes", defaultValue = "65536") int maxSourceBytes,
            @ConfigProperty(name = "correctexam.kroki.max-svg-bytes", defaultValue = "2097152") int maxSvgBytes) {
        this.url = url.map(String::trim).filter(u -> !u.isEmpty()).map(u -> u.endsWith("/") ? u.substring(0, u.length() - 1) : u);
        this.timeout = timeout;
        this.maxSourceBytes = maxSourceBytes;
        this.maxSvgBytes = maxSvgBytes;
        this.http = HttpClient.newBuilder().connectTimeout(timeout).followRedirects(HttpClient.Redirect.NEVER).build();
    }

    public boolean isConfigured() {
        return url.isPresent();
    }

    public int getMaxSourceBytes() {
        return maxSourceBytes;
    }

    /** Renders a diagram; {@code kind} must come from {@link DiagramKinds#normalizeKind(String)}. */
    public byte[] renderSvg(String kind, String source) {
        String base = url.orElseThrow(() -> new KrokiException(KrokiException.Reason.NOT_CONFIGURED,
                "Diagram rendering is not configured (correctexam.kroki.url)"));
        byte[] body = source.getBytes(StandardCharsets.UTF_8);
        if (body.length > maxSourceBytes) {
            throw new KrokiException(KrokiException.Reason.TOO_LARGE, "Diagram source larger than " + maxSourceBytes + " bytes");
        }
        HttpRequest request = HttpRequest.newBuilder(URI.create(base + "/" + kind + "/svg"))
                .timeout(timeout)
                .header("Content-Type", "text/plain; charset=utf-8")
                .header("Accept", "image/svg+xml")
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        HttpResponse<InputStream> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (IOException e) {
            log.warn("Kroki unreachable at {}: {}", base, e.getMessage());
            throw new KrokiException(KrokiException.Reason.UNAVAILABLE, "Kroki unreachable", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new KrokiException(KrokiException.Reason.UNAVAILABLE, "Interrupted while waiting for Kroki", e);
        }
        try (InputStream in = response.body()) {
            byte[] content = in.readNBytes(maxSvgBytes + 1);
            if (response.statusCode() == 200) {
                if (content.length > maxSvgBytes) {
                    throw new KrokiException(KrokiException.Reason.TOO_LARGE, "Rendered SVG larger than " + maxSvgBytes + " bytes");
                }
                return content;
            }
            String message = new String(content, 0, Math.min(content.length, 2000), StandardCharsets.UTF_8).trim();
            if (response.statusCode() == 400) {
                throw new KrokiException(KrokiException.Reason.INVALID_SOURCE, message.isEmpty() ? "Invalid diagram source" : message);
            }
            log.warn("Kroki answered {} for a {} diagram: {}", response.statusCode(), kind, message);
            throw new KrokiException(KrokiException.Reason.UNAVAILABLE, "Kroki answered HTTP " + response.statusCode());
        } catch (IOException e) {
            throw new KrokiException(KrokiException.Reason.UNAVAILABLE, "Could not read the answer of Kroki", e);
        }
    }
}
