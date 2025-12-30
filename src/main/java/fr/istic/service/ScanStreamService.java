package fr.istic.service;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.istic.service.customdto.ScanStatusDto;

@ApplicationScoped
public class ScanStreamService {

    // Crée un flux "chaud" (Hot Stream) qui diffuse à tous les abonnés
    private final BroadcastProcessor<ScanStatusDto> statusProcessor = BroadcastProcessor.create();
    private final ObjectMapper objectMapper ;
    public  ScanStreamService() {
                this.objectMapper = new ObjectMapper();
    }

    /**
     * Écoute RabbitMQ (configuré dans application.properties)
     * et pousse chaque message reçu dans le processeur interne.
     */
    @Incoming("scan-status-in")
    public void consumeRabbitMQ(Object status) {
        byte[] b  =(byte[]) status;
                try {
                    ScanStatusDto dto = objectMapper.reader().readValue(b, ScanStatusDto.class);
                    System.err.println("receive " + dto.page + " "  + dto.progress+ " " +dto.status);
                    statusProcessor.onNext(dto);
                } catch (IOException e) {
                    e.printStackTrace();
                }

    }

    /**
     * Retourne un flux filtré pour un ID spécifique.
     * C'est ici que la magie du SSE opère.
     */
    public Multi<ScanStatusDto> streamStatus(int examId) {
        return statusProcessor
                // On ne garde que les messages qui concernent ce exam_id
                .select().where(s -> s.examId == examId);
    }
}
