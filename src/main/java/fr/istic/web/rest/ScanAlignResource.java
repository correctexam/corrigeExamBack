package fr.istic.web.rest;


import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import fr.istic.domain.Exam;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.ScanAlignJobRegistry;
import fr.istic.service.ScanStreamService;
import fr.istic.service.SecurityService;
import fr.istic.service.customdto.ScanAlignJobDto;
import fr.istic.service.customdto.ScanRequestDto;
import fr.istic.service.customdto.ScanStatusDto;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
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

    @Inject
    ScanAlignJobRegistry jobRegistry;

    @Inject
    SecurityService securityService;

    /**
     * POST /api/scansalign
     * Reçoit la demande et l'envoie dans la queue RabbitMQ, sauf si un job est déjà en cours
     * pour cet examen (les deux jobs écriraient le même cache {@code cache/{examId}.sqlite3}).
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Submit a scan alignment job", description = "Submits a scan alignment job to the processing queue. Rejected with 409 while a job of the same exam is still running: follow that job instead (GET /api/scansalign/{id}/status and the SSE stream).")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "202", description = "Job submitted"),
            @APIResponse(responseCode = "400", description = "Invalid job request"),
            @APIResponse(
                responseCode = "409",
                description = "A job is already running for this exam",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ScanAlignJobDto.class))
            ),
            @APIResponse(responseCode = "503", description = "The job could not be sent to the message broker")
        }
    )
    public Response createScanJob(@Valid ScanRequestDto request) {
        var running = jobRegistry.submit(request);
        if (running.isPresent()) {
            log.info("Alignment job for exam {} rejected: a job is already {}", request.examID, running.get().status);
            return Response.status(Response.Status.CONFLICT).entity(running.get()).build();
        }
        log.info("Submitting scan alignment job: exam {}, template {}, scan {}, pages '{}', algo {}",
                request.examID, request.templateID, request.scanID, request.pagesToManage, request.algo);
        try {
            requestEmitter.send(request).whenComplete((ok, error) -> {
                if (error != null) {
                    log.error("Alignment job for exam {} not delivered to the broker", request.examID, error);
                    jobRegistry.fail(request.examID, "Job not delivered to the message broker: " + error.getMessage());
                }
            });
        } catch (RuntimeException e) {
            log.error("Alignment job for exam {} could not be sent", request.examID, e);
            jobRegistry.fail(request.examID, "Job not sent to the message broker: " + e.getMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        return Response.accepted().build();
    }

    /**
     * GET /api/scansalign/{id}/status
     * État connu du job d'alignement de l'examen, pour qu'un client se raccroche à un job
     * qu'il n'a pas lancé (page rechargée, autre onglet, autre enseignant).
     */
    @GET
    @Path("/{id}/status")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Operation(summary = "Get the state of the alignment job of an exam", description = "Returns the last known state of the server-side alignment job of the exam: submitted or running while it is active, then finished or error for a while after its end.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "Last known state of the job",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ScanAlignJobDto.class))
            ),
            @APIResponse(responseCode = "204", description = "No known job for this exam"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this exam")
        }
    )
    public Response getScanJobStatus(
        @Parameter(description = "ID of the exam", required = true) @PathParam("id") long examId,
        @Context SecurityContext ctx
    ) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return jobRegistry.find(examId)
            .map(job -> Response.ok(job).build())
            .orElseGet(() -> Response.noContent().build());
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
