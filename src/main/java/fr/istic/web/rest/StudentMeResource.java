package fr.istic.web.rest;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.StudentMeService;
import fr.istic.service.customdto.studentportal.StudentPersonalDataDTO;
import fr.istic.service.customdto.studentportal.StudentSheetSummaryDTO;
import fr.istic.service.customdto.studentview.StudentSheetViewDTO;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Sheets and personal data of the student of a portal session (ADR 0003). The student is the e-mail address of the
 * session; access to a sheet is checked by {@link StudentMeService} (the sheet belongs to a student record with that
 * address, and its exam is not hidden). SecurityService.canAccess does not apply: it checks instructors.
 */
@Tag(name = "Student portal", description = "Sheets and personal data of the student of the session.")
@Path("/api/student/me")
@ApplicationScoped
@RolesAllowed(AuthoritiesConstants.STUDENT)
public class StudentMeResource {

    private static final Logger log = LoggerFactory.getLogger(StudentMeResource.class);

    @Inject
    StudentMeService studentMeService;

    private static String email(SecurityContext ctx) {
        return ctx.getUserPrincipal().getName();
    }

    @Operation(summary = "List my sheets", description = "Sheets of the exams that are not hidden, most recent first.")
    @APIResponse(responseCode = "200", description = "The sheets",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = StudentSheetSummaryDTO.class)))
    @GET
    @Path("/sheets")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sheets(@Context SecurityContext ctx) {
        return Response.ok(studentMeService.sheets(email(ctx))).header("Cache-Control", "no-store").build();
    }

    @Operation(summary = "Get one of my sheets",
        description = "Corrections and grade only when the exam is published as GRADED.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "The sheet",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = StudentSheetViewDTO.class))),
            @APIResponse(responseCode = "404", description = "Not a sheet of the student, or exam hidden")
        }
    )
    @GET
    @Path("/sheets/{sheetId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sheet(@Parameter(description = "Identifier of the sheet", required = true) @PathParam("sheetId") long sheetId,
            @Context SecurityContext ctx) {
        return studentMeService.view(email(ctx), sheetId)
                .map(v -> Response.ok(v).header("Cache-Control", "no-store").build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @Operation(summary = "Get a page of one of my sheets", description = "Page n (from 1), as stored in the cache of the exam.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "The cached page",
                content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = SchemaType.STRING))),
            @APIResponse(responseCode = "404", description = "Not a sheet of the student, exam hidden, or no such page"),
            @APIResponse(responseCode = "503", description = "The cache of the exam cannot be read")
        }
    )
    @GET
    @Path("/sheets/{sheetId}/page/{page}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response page(@PathParam("sheetId") long sheetId, @PathParam("page") int page,
            @QueryParam("noalign") @DefaultValue("false") boolean noalign, @Context SecurityContext ctx) {
        try {
            return toResponse(studentMeService.page(email(ctx), sheetId, page, noalign));
        } catch (IOException | RuntimeException e) {
            log.error("Could not read page {} of sheet {}: {}", page, sheetId, e.toString());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
    }

    @Operation(summary = "Count the starred answers to a question", description = "Only for an exam published as GRADED.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "{\"count\": n}"),
            @APIResponse(responseCode = "404", description = "Not a sheet of the student, or exam not GRADED")
        }
    )
    @GET
    @Path("/sheets/{sheetId}/best/{numero}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response countBestAnswers(@PathParam("sheetId") long sheetId, @PathParam("numero") int numero, @Context SecurityContext ctx) {
        return studentMeService.countBestAnswers(email(ctx), sheetId, numero)
                .map(count -> Response.ok(Map.of("count", count)).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @Operation(summary = "Get a page of a starred answer",
        description = "Page of the index-th starred answer to the question, without the identity of its student.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "The cached page",
                content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = SchemaType.STRING))),
            @APIResponse(responseCode = "404", description = "Not a sheet of the student, exam not GRADED, or no such answer or page"),
            @APIResponse(responseCode = "503", description = "The cache of the exam cannot be read")
        }
    )
    @GET
    @Path("/sheets/{sheetId}/best/{numero}/{index}/page/{page}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response bestAnswerPage(@PathParam("sheetId") long sheetId, @PathParam("numero") int numero,
            @PathParam("index") int index, @PathParam("page") int page,
            @QueryParam("noalign") @DefaultValue("false") boolean noalign, @Context SecurityContext ctx) {
        try {
            return toResponse(studentMeService.bestAnswerPage(email(ctx), sheetId, numero, index, page, noalign));
        } catch (IOException | RuntimeException e) {
            log.error("Could not read a starred answer page for sheet {}: {}", sheetId, e.toString());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
    }

    @Operation(summary = "Get my personal data",
        description = "Every student record carrying the address of the session, and every sheet held (GDPR right of access).")
    @APIResponse(responseCode = "200", description = "The personal data",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = StudentPersonalDataDTO.class)))
    @GET
    @Path("/data")
    @Produces(MediaType.APPLICATION_JSON)
    public Response personalData(@Context SecurityContext ctx) {
        return Response.ok(studentMeService.personalData(email(ctx)))
                .header("Cache-Control", "no-store")
                .header("Content-Disposition", "inline; filename=\"correctexam-personal-data.json\"")
                .build();
    }

    private static Response toResponse(Optional<String> page) {
        return page.map(p -> Response.ok(p, MediaType.TEXT_PLAIN).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }
}
