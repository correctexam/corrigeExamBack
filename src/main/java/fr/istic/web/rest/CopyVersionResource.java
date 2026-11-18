package fr.istic.web.rest;

import java.util.List;

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
import fr.istic.service.dto.CopyVersionDTO;
import fr.istic.service.subject.CopyVersionService;
import fr.istic.service.subject.VersionException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
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
 * Version of each copy of the scan of an exam (T25e6b, ADR 0005): written by the front after the alignment, read by
 * the statistics per version (and returned with each result of {@code showResult}).
 */
@Path("/api/exams/{examId}/copy-versions")
@Tag(name = "Exam versions", description = "Versions of an exam written in Typst (T25e)")
public class CopyVersionResource {

    private final Logger log = LoggerFactory.getLogger(CopyVersionResource.class);

    @Inject
    CopyVersionService copyVersionService;

    @Inject
    SecurityService securityService;

    @GET
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Version of each copy of the scan", description = "Copies by their first page in the scan (from 1).")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The versions of the copies"),
        @APIResponse(responseCode = "403", description = "Current user cannot access this exam")
    })
    public Response list(@Parameter(description = "Id of the exam") @PathParam("examId") long examId, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return forbidden();
        }
        return Response.ok(copyVersionService.list(examId)).build();
    }

    @PUT
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Replace the versions of the copies", description = "Sent by the front after an alignment.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The recorded versions"),
        @APIResponse(responseCode = "400", description = "Page or label not valid, page given twice"),
        @APIResponse(responseCode = "403", description = "Current user cannot access this exam"),
        @APIResponse(responseCode = "404", description = "No such exam")
    })
    public Response replace(@PathParam("examId") long examId, List<CopyVersionDTO> copies, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return forbidden();
        }
        try {
            return Response.ok(copyVersionService.replace(examId, copies)).build();
        } catch (VersionException e) {
            return ExamVersionResource.error(e);
        }
    }

    private Response forbidden() {
        log.debug("Access to the versions of the copies of an exam refused");
        return Response.status(403, "Current user cannot access to this ressource").build();
    }
}
