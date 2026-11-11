package fr.istic.web.rest;


import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import fr.istic.service.ScanStreamService;
import fr.istic.service.customdto.ScanRequestDto;
import fr.istic.service.customdto.ScanStatusDto;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger log = LoggerFactory.getLogger(ScanAlignResource.class);


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
        log.info("Submitting scan alignment job: exam {}, template {}, scan {}, pages '{}', algo {}",
                request.examID, request.templateID, request.scanID, request.pagesToManage, request.algo);
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
    @Operation(summary = "Stream scan alignment progress", description = "Opens a Server-Sent Events connection that streams the real-time progress of the scan alignment jobs of an exam. The first event has the status 'connected': submit the job only after receiving it. The stream ends on the client side after a 'finished' or 'error' status.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "Stream of scan status events")
        }
    )
    public Multi<ScanStatusDto> streamScanProgress(@Parameter(description = "ID of the exam", required = true) @PathParam("id") int exam_id) {
        log.debug("SSE client subscribed to the alignment statuses of exam {}", exam_id);
        return streamService.streamStatus(exam_id);
    }
}
