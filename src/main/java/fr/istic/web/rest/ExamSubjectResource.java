package fr.istic.web.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.domain.Exam;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.SecurityService;
import fr.istic.service.subject.ExamSubjectService;
import fr.istic.service.subject.SubjectException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Typst subject of an exam (T25c, ADR 0004): files, edition lock. Every endpoint needs a teacher role and access to
 * the exam.
 */
@Path("/api/exams/{examId}/subject")
@Tag(name = "Exam subject", description = "Typst subject of an exam: files and edition lock (T25c)")
public class ExamSubjectResource {

    private final Logger log = LoggerFactory.getLogger(ExamSubjectResource.class);

    @Inject
    ExamSubjectService examSubjectService;

    @Inject
    SecurityService securityService;

    @GET
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get the subject of an exam", description = "Files (without their content) and edition lock.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The subject"),
        @APIResponse(responseCode = "403", description = "Current user cannot access this exam"),
        @APIResponse(responseCode = "404", description = "The exam has no subject")
    })
    public Response get(@Parameter(description = "Id of the exam") @PathParam("examId") long examId, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return forbidden();
        }
        return examSubjectService.find(examId, login(ctx)).map(s -> Response.ok(s).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create the subject of an exam", description = "Idempotent; the creator holds the edition lock.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The subject (new or existing)"),
        @APIResponse(responseCode = "403", description = "Current user cannot access this exam"),
        @APIResponse(responseCode = "404", description = "No such exam"),
        @APIResponse(responseCode = "503", description = "No object storage configured")
    })
    public Response create(@PathParam("examId") long examId, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return forbidden();
        }
        try {
            return Response.ok(examSubjectService.create(examId, login(ctx))).build();
        } catch (SubjectException e) {
            return error(e);
        }
    }

    @GET
    @Path("/files/{path: .+}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Operation(summary = "Read a file of the subject")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The content of the file"),
        @APIResponse(responseCode = "400", description = "Path not allowed"),
        @APIResponse(responseCode = "403", description = "Current user cannot access this exam"),
        @APIResponse(responseCode = "404", description = "No such file"),
        @APIResponse(responseCode = "503", description = "Object storage unavailable")
    })
    public Response readFile(@PathParam("examId") long examId, @PathParam("path") String path, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return forbidden();
        }
        try {
            ExamSubjectService.FileContent file = examSubjectService.readFile(examId, path);
            return Response.ok(file.stream(), file.contentType())
                    // files of the teachers: never interpreted by the browser as a page of the application
                    .header("X-Content-Type-Options", "nosniff")
                    .header("Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'; sandbox")
                    .header("Cache-Control", "private, no-cache")
                    .build();
        } catch (SubjectException e) {
            return error(e);
        }
    }

    @PUT
    @Path("/files/{path: .+}")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Operation(summary = "Write a file of the subject",
        description = "The caller must hold the edition lock. Optional If-Match header: the revision the editor knows.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The subject after the change"),
        @APIResponse(responseCode = "400", description = "Path not allowed"),
        @APIResponse(responseCode = "403", description = "Current user cannot access this exam"),
        @APIResponse(responseCode = "404", description = "The exam has no subject"),
        @APIResponse(responseCode = "409", description = "Lock not held by the caller, or revision changed"),
        @APIResponse(responseCode = "413", description = "File or subject too large"),
        @APIResponse(responseCode = "503", description = "Object storage unavailable")
    })
    public Response writeFile(@PathParam("examId") long examId, @PathParam("path") String path,
            @HeaderParam("If-Match") String ifMatch, byte[] content, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return forbidden();
        }
        try {
            return Response.ok(examSubjectService.writeFile(examId, path, content == null ? new byte[0] : content, login(ctx),
                    revision(ifMatch))).build();
        } catch (SubjectException e) {
            return error(e);
        }
    }

    @DELETE
    @Path("/files/{path: .+}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Operation(summary = "Delete a file of the subject", description = "The caller must hold the edition lock.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The subject after the change"),
        @APIResponse(responseCode = "403", description = "Current user cannot access this exam"),
        @APIResponse(responseCode = "404", description = "No such file"),
        @APIResponse(responseCode = "409", description = "Lock not held by the caller, or revision changed")
    })
    public Response deleteFile(@PathParam("examId") long examId, @PathParam("path") String path,
            @HeaderParam("If-Match") String ifMatch, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return forbidden();
        }
        try {
            return Response.ok(examSubjectService.deleteFile(examId, path, login(ctx), revision(ifMatch))).build();
        } catch (SubjectException e) {
            return error(e);
        }
    }

    @POST
    @Path("/lock")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Operation(summary = "Take or renew the edition lock",
        description = "force=true takes the lock over from another teacher (after a warning in the editor).")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The lock, held by the caller"),
        @APIResponse(responseCode = "403", description = "Current user cannot access this exam"),
        @APIResponse(responseCode = "404", description = "The exam has no subject"),
        @APIResponse(responseCode = "409", description = "Held by another teacher")
    })
    public Response lock(@PathParam("examId") long examId, @QueryParam("force") @DefaultValue("false") boolean force,
            @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return forbidden();
        }
        try {
            return Response.ok(examSubjectService.lock(examId, login(ctx), force)).build();
        } catch (SubjectException e) {
            return error(e);
        }
    }

    @DELETE
    @Path("/lock")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Operation(summary = "Release the edition lock", description = "Without effect when the caller does not hold it.")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Released"),
        @APIResponse(responseCode = "403", description = "Current user cannot access this exam"),
        @APIResponse(responseCode = "404", description = "The exam has no subject")
    })
    public Response unlock(@PathParam("examId") long examId, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return forbidden();
        }
        try {
            examSubjectService.unlock(examId, login(ctx));
            return Response.noContent().build();
        } catch (SubjectException e) {
            return error(e);
        }
    }

    /** Revision in an {@code If-Match} header ({@code 3} or {@code "3"}); null when absent or not a number. */
    static Integer revision(String ifMatch) {
        if (ifMatch == null) {
            return null;
        }
        String v = ifMatch.trim().replace("\"", "");
        return v.matches("\\d{1,9}") ? Integer.valueOf(v) : null;
    }

    static Response error(SubjectException e) {
        Response.Status status = switch (e.getReason()) {
            case NOT_FOUND -> Response.Status.NOT_FOUND;
            case BAD_PATH -> Response.Status.BAD_REQUEST;
            case LOCKED, REVISION_CONFLICT -> Response.Status.CONFLICT;
            case TOO_LARGE -> Response.Status.REQUEST_ENTITY_TOO_LARGE;
            case STORAGE_UNAVAILABLE -> Response.Status.SERVICE_UNAVAILABLE;
        };
        return Response.status(status).type(MediaType.APPLICATION_JSON).entity(new Error(e.getReason().name(), e.getMessage())).build();
    }

    private Response forbidden() {
        log.debug("Access to an exam subject refused");
        return Response.status(403, "Current user cannot access to this ressource").build();
    }

    static String login(SecurityContext ctx) {
        return ctx.getUserPrincipal().getName();
    }

    /** Error body: {@code error} is the reason, {@code message} details it. */
    public record Error(String error, String message) {
    }
}
