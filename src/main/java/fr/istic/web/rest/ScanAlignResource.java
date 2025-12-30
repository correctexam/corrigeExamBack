package fr.istic.web.rest;


import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import fr.istic.service.ScanStreamService;
import fr.istic.service.customdto.ScanRequestDto;
import fr.istic.service.customdto.ScanStatusDto;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/api/scansalign")
@Consumes(MediaType.APPLICATION_JSON)
public class ScanAlignResource {

    // 1. Injecter l'émetteur pour envoyer vers RabbitMQ
    @Channel("scan-request-out")
    Emitter<ScanRequestDto> requestEmitter;

    // 2. Injecter notre service de streaming
    @Inject
    ScanStreamService streamService;

    /**
     * POST /api/scansalign
     * Reçoit la demande et l'envoie dans la queue RabbitMQ.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void createScanJob(@Valid ScanRequestDto request) {
        System.out.println("Envoi du job RabbitMQ pour scan_id: " + request.examID);
        System.out.println("Envoi du job RabbitMQ pour scan_id: " + request.pagesToManage);
        System.out.println("Envoi du job RabbitMQ pour scan_id: " + request.scanID);
        System.out.println("Envoi du job RabbitMQ pour scan_id: " + request.templateID);
        System.out.println("Envoi du job RabbitMQ pour scan_id: " + request.algo);
        System.out.println("Envoi du job RabbitMQ pour scan_id: " + request.heightresolution);
        System.out.println("Envoi du job RabbitMQ pour scan_id: " + request.corner_square_size);
        System.out.println("Envoi du job RabbitMQ pour scan_id: " + request.min_radius);

        requestEmitter.send(request);
    }

    /**
     * GET /api/scans/{id}/events
     * "Long Polling" via Server-Sent Events (SSE).
     * Le client garde la connexion ouverte et reçoit les mises à jour en temps réel.
     */
    @GET
    @Path("/{id}/events")
    @Produces(MediaType.SERVER_SENT_EVENTS) // Indique que c'est du streaming
    public Multi<ScanStatusDto> streamScanProgress(@PathParam("id") int exam_id) {
        System.out.println("Client connecté pour recevoir les événements SSE du scan_id: " + exam_id);
//        throw new UnsupportedOperationException("Not implemented yet");
        return streamService.streamStatus(exam_id);
    }
}
