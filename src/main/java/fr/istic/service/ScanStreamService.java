package fr.istic.service;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.istic.service.customdto.ScanStatusDto;

/**
 * Relays the alignment statuses published by the Rust worker (RabbitMQ exchange
 * {@code scan_status_exchange}) to the SSE clients, filtered by exam.
 *
 * <p>The stream is hot and does not replay past events: a client must subscribe
 * <b>before</b> submitting the job. Every subscription therefore starts with a
 * {@code connected} event, which tells the client that it can submit the job safely.</p>
 */
@ApplicationScoped
public class ScanStreamService {

    private final Logger log = LoggerFactory.getLogger(ScanStreamService.class);

    // Flux "chaud" (hot stream) qui diffuse à tous les abonnés
    private final BroadcastProcessor<ScanStatusDto> statusProcessor = BroadcastProcessor.create();
    private final ObjectMapper objectMapper;

    public ScanStreamService() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Écoute RabbitMQ (configuré dans application.properties)
     * et pousse chaque message reçu dans le processeur interne.
     */
    @Incoming("scan-status-in")
    public void consumeRabbitMQ(Object status) {
        byte[] b = null;
        if (status instanceof io.vertx.core.buffer.Buffer buffer) {
            b = buffer.getBytes();
        } else if (status instanceof byte[] bytes) {
            b = bytes;
        }
        if (b == null) {
            log.warn("Ignoring scan status message of unknown type {}",
                    status == null ? "null" : status.getClass().getName());
            return;
        }
        try {
            ScanStatusDto dto = objectMapper.reader().readValue(b, ScanStatusDto.class);
            if (ScanStatusDto.STATUS_ERROR.equals(dto.status)) {
                log.warn("Scan alignment failed for exam {}: {}", dto.examId, dto.details);
            } else {
                log.debug("Scan status for exam {}: page {} {} {}%", dto.examId, dto.page, dto.status, dto.progress);
            }
            statusProcessor.onNext(dto);
        } catch (IOException e) {
            log.error("Invalid scan status message: {}", new String(b, java.nio.charset.StandardCharsets.UTF_8), e);
        }
    }

    /**
     * Returns the statuses of one exam, starting with a {@code connected} event emitted
     * as soon as the subscription is active.
     */
    public Multi<ScanStatusDto> streamStatus(int examId) {
        Multi<ScanStatusDto> live = statusProcessor
                // On ne garde que les messages qui concernent cet exam_id
                .select().where(s -> s.examId == examId);
        // merging (and not concatenating) subscribes to the live stream immediately,
        // so that no status can be lost between the two streams
        return Multi.createBy().merging().streams(Multi.createFrom().item(ScanStatusDto.connected(examId)), live);
    }
}
