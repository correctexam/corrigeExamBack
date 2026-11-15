package fr.istic.web.rest;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.domain.Exam;
import fr.istic.domain.ExamSheet;
import fr.istic.service.StudentSheetViewService;
import fr.istic.service.customdto.studentview.StudentSheetViewDTO;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Student view of one sheet through its link {@code /copie/{uuid}} (ADR 0003, T10c).
 * <p>
 * The UUID of the sheet is the capability: everything is reached from it, and nothing else can be read (no list by
 * sequential identifier, no other sheet, no other student). These links are explicit grants of the instructor and show
 * the corrected sheet whatever the student access of the exam. The starred answers of the other students are shown
 * without anything that identifies them.
 */
@Tag(name = "Student sheet (public)", description = "Read-only view of one exam sheet through the UUID of its link.")
@Path("/api/public/copie/{uuid}")
@ApplicationScoped
@PermitAll
public class PublicSheetResource {

    private static final Logger log = LoggerFactory.getLogger(PublicSheetResource.class);

    @Inject
    StudentSheetViewService studentSheetViewService;

    @Operation(summary = "Get a sheet for its student",
        description = "Exam, questions, answers, comments, grade and contact e-mails of the sheet with this UUID.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The sheet",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = StudentSheetViewDTO.class))
            ),
            @APIResponse(responseCode = "404", description = "No sheet with this UUID")
        }
    )
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSheet(@Parameter(description = "UUID of the sheet", required = true) @PathParam("uuid") String uuid) {
        return studentSheetViewService.view(uuid, true)
                .map(dto -> Response.ok(dto).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @Operation(summary = "Get a page of a sheet",
        description = "Page n (from 1) of the sheet with this UUID, as stored in the cache of the exam (aligned, or not "
            + "aligned with noalign=true).")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "The cached page",
                content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = org.eclipse.microprofile.openapi.annotations.enums.SchemaType.STRING))),
            @APIResponse(responseCode = "404", description = "No sheet with this UUID, or no such page in the sheet"),
            @APIResponse(responseCode = "503", description = "The cache of the exam cannot be read")
        }
    )
    @GET
    @Path("/page/{page}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getPage(@Parameter(description = "UUID of the sheet", required = true) @PathParam("uuid") String uuid,
            @Parameter(description = "Page of the sheet, from 1", required = true) @PathParam("page") int page,
            @Parameter(description = "Page before alignment") @QueryParam("noalign") @DefaultValue("false") boolean noalign) {
        Optional<ExamSheet> sheet = studentSheetViewService.findSheet(uuid);
        Optional<Exam> exam = sheet.flatMap(studentSheetViewService::examOf);
        if (exam.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        try {
            return studentSheetViewService.page(sheet.get(), exam.get().id, page, noalign)
                    .map(p -> Response.ok(p, MediaType.TEXT_PLAIN).build())
                    .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
        } catch (IOException | RuntimeException e) {
            log.error("Could not read page {} of sheet {}: {}", page, uuid, e.toString());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
    }

    @Operation(summary = "Count the starred answers to a question",
        description = "Number of answers to question numero starred by the graders in the exam of the sheet. "
            + "They are read by index with /best/{numero}/{index}/page/{page}.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "{\"count\": n}"),
            @APIResponse(responseCode = "404", description = "No sheet with this UUID")
        }
    )
    @GET
    @Path("/best/{numero}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response countBestAnswers(@Parameter(description = "UUID of the sheet", required = true) @PathParam("uuid") String uuid,
            @Parameter(description = "Number of the question", required = true) @PathParam("numero") int numero) {
        Optional<Exam> exam = studentSheetViewService.findSheet(uuid).flatMap(studentSheetViewService::examOf);
        if (exam.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        int count = studentSheetViewService.bestAnswerSheets(exam.get().id, numero).size();
        return Response.ok(Map.of("count", count)).build();
    }

    @Operation(summary = "Get a page of a starred answer",
        description = "Page (from 1) of the index-th starred answer to question numero, without the identity of its "
            + "student. Only the pages that hold a zone of the question can be read.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "The cached page",
                content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = org.eclipse.microprofile.openapi.annotations.enums.SchemaType.STRING))),
            @APIResponse(responseCode = "404", description = "No sheet with this UUID, no such starred answer or page"),
            @APIResponse(responseCode = "503", description = "The cache of the exam cannot be read")
        }
    )
    @GET
    @Path("/best/{numero}/{index}/page/{page}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getBestAnswerPage(@Parameter(description = "UUID of the sheet", required = true) @PathParam("uuid") String uuid,
            @Parameter(description = "Number of the question", required = true) @PathParam("numero") int numero,
            @Parameter(description = "Index of the starred answer, from 0", required = true) @PathParam("index") int index,
            @Parameter(description = "Page of the sheet, from 1", required = true) @PathParam("page") int page,
            @Parameter(description = "Page before alignment") @QueryParam("noalign") @DefaultValue("false") boolean noalign) {
        Optional<Exam> exam = studentSheetViewService.findSheet(uuid).flatMap(studentSheetViewService::examOf);
        if (exam.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        try {
            return studentSheetViewService.bestAnswerPage(exam.get().id, numero, index, page, noalign)
                    .map(p -> Response.ok(p, MediaType.TEXT_PLAIN).build())
                    .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
        } catch (IOException | RuntimeException e) {
            log.error("Could not read a starred answer page of exam {}: {}", exam.get().id, e.toString());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
    }
}
