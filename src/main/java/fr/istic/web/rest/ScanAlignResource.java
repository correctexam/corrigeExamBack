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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(
    name = "ScanAlign",
    description = "Management of scan alignment jobs: submit a job and follow its progress through a streaming connection."
)
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
    @Operation(summary = "Submit a scan alignment job", description = "Submits a scan alignment job to the processing queue.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "Job submitted"),
            @APIResponse(responseCode = "400", description = "Invalid job request")
        }
    )
    public void createScanJob(@Valid ScanRequestDto request) {
        System.out.println("Envoi du job RabbitMQ pour examID: " + request.examID);
        System.out.println("Envoi du job RabbitMQ pour pagesToManage: " + request.pagesToManage);
        System.out.println("Envoi du job RabbitMQ pour scan_id: " + request.scanID);
        System.out.println("Envoi du job RabbitMQ pour templateID: " + request.templateID);
        System.out.println("Envoi du job RabbitMQ pour algo: " + request.algo);
        System.out.println("Envoi du job RabbitMQ pour heightresolution: " + request.heightresolution);
        System.out.println("Envoi du job RabbitMQ pour corner_square_size: " + request.corner_square_size);
        System.out.println("Envoi du job RabbitMQ pour min_radius: " + request.min_radius);

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
    @Operation(summary = "Stream scan alignment progress", description = "Opens a Server-Sent Events connection that streams the real-time progress of the scan alignment job.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "Stream of scan status events")
        }
    )
    public Multi<ScanStatusDto> streamScanProgress(@Parameter(description = "ID of the scan", required = true) @PathParam("id") int exam_id) {
        System.out.println("Client connecté pour recevoir les événements SSE du scan_id: " + exam_id);
//        throw new UnsupportedOperationException("Not implemented yet");
        return streamService.streamStatus(exam_id);
    }
}
