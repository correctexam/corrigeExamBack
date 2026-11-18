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
import fr.istic.service.dto.ExamVersionsRequestDTO;
import fr.istic.service.subject.ExamVersionService;
import fr.istic.service.subject.VersionException;
import fr.istic.service.subject.VersionRules;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Versions of an exam written in Typst (T25e, ADR 0005). The front compiles the versions, checks them and sends their
 * plan, layout and PDF; the server keeps them for the print runs and for the reconstruction of the copies.
 */
@Path("/api/exams/{examId}/versions")
@Tag(name = "Exam versions", description = "Versions of an exam written in Typst (T25e)")
public class ExamVersionResource {

    private final Logger log = LoggerFactory.getLogger(ExamVersionResource.class);

    @Inject
    ExamVersionService examVersionService;

    @Inject
    SecurityService securityService;

    @GET
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Versions of an exam", description = "Version A first; empty for an exam of a single version.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The versions"),
        @APIResponse(responseCode = "403", description = "Current user cannot access this exam")
    })
    public Response list(@Parameter(description = "Id of the exam") @PathParam("examId") long examId, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return forbidden();
        }
        return Response.ok(examVersionService.list(examId)).build();
    }

    @PUT
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Replace the versions of an exam", description = "The PDFs of the previous versions are deleted; send the PDF of each version afterwards.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The recorded versions"),
        @APIResponse(responseCode = "400", description = "Versions, plan or layout not valid"),
        @APIResponse(responseCode = "403", description = "Current user cannot access this exam"),
        @APIResponse(responseCode = "404", description = "No such exam"),
        @APIResponse(responseCode = "409", description = "A print run of several versions exists"),
        @APIResponse(responseCode = "503", description = "Object storage unavailable")
    })
    public Response replace(@PathParam("examId") long examId, ExamVersionsRequestDTO request, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return forbidden();
        }
        try {
            return Response.ok(examVersionService.replace(examId, request, ExamSubjectResource.login(ctx))).build();
        } catch (VersionException e) {
            return error(e);
        }
    }

    @DELETE
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Operation(summary = "Delete the versions of an exam")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Deleted"),
        @APIResponse(responseCode = "403", description = "Current user cannot access this exam"),
        @APIResponse(responseCode = "409", description = "A print run of several versions exists")
    })
    public Response clear(@PathParam("examId") long examId, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return forbidden();
        }
        try {
            examVersionService.clear(examId, ExamSubjectResource.login(ctx));
            return Response.noContent().build();
        } catch (VersionException e) {
            return error(e);
        }
    }

    @GET
    @Path("/{index}/pdf")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Produces("application/pdf")
    @Operation(summary = "PDF of a version")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The PDF"),
        @APIResponse(responseCode = "403", description = "Current user cannot access this exam"),
        @APIResponse(responseCode = "404", description = "No such version or no PDF"),
        @APIResponse(responseCode = "503", description = "Object storage unavailable")
    })
    public Response readPdf(@PathParam("examId") long examId, @PathParam("index") int index, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return forbidden();
        }
        try {
            ExamVersionService.PdfContent pdf = examVersionService.readPdf(examId, index);
            return Response.ok(pdf.stream(), "application/pdf")
                    .header("Content-Length", pdf.size())
                    .header("Content-Disposition", "inline; filename=\"exam-" + examId + "-" + VersionRules.label(index) + ".pdf\"")
                    .header("X-Content-Type-Options", "nosniff")
                    .header("Cache-Control", "private, no-cache")
                    .build();
        } catch (VersionException e) {
            return error(e);
        }
    }

    @PUT
    @Path("/{index}/pdf")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Consumes({ "application/pdf", MediaType.APPLICATION_OCTET_STREAM })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Store the PDF of a version")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The version, with its PDF"),
        @APIResponse(responseCode = "400", description = "Not a PDF"),
        @APIResponse(responseCode = "403", description = "Current user cannot access this exam"),
        @APIResponse(responseCode = "404", description = "No such version"),
        @APIResponse(responseCode = "409", description = "A print run of several versions exists"),
        @APIResponse(responseCode = "413", description = "PDF too large"),
        @APIResponse(responseCode = "503", description = "Object storage unavailable")
    })
    public Response writePdf(@PathParam("examId") long examId, @PathParam("index") int index, byte[] content,
            @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return forbidden();
        }
        try {
            return Response.ok(examVersionService.writePdf(examId, index, content, ExamSubjectResource.login(ctx))).build();
        } catch (VersionException e) {
            return error(e);
        }
    }

    static Response error(VersionException e) {
        Response.Status status = switch (e.getReason()) {
            case NOT_FOUND -> Response.Status.NOT_FOUND;
            case INVALID -> Response.Status.BAD_REQUEST;
            case FROZEN -> Response.Status.CONFLICT;
            case TOO_LARGE -> Response.Status.REQUEST_ENTITY_TOO_LARGE;
            case STORAGE_UNAVAILABLE -> Response.Status.SERVICE_UNAVAILABLE;
        };
        return Response.status(status).type(MediaType.APPLICATION_JSON)
                .entity(new ExamSubjectResource.Error(e.getReason().name(), e.getMessage())).build();
    }

    private Response forbidden() {
        log.debug("Access to the versions of an exam refused");
        return Response.status(403, "Current user cannot access to this ressource").build();
    }
}
