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
import fr.istic.service.dto.EditionRequestDTO;
import fr.istic.service.subject.EditionRules;
import fr.istic.service.subject.ExamEditionService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Print runs of an exam written in Typst (T25d, ADR 0004). The front makes the PDF from the template and stamps the
 * page codes; the server only gives the print run its identifier, used in the codes and checked at scan time.
 */
@Path("/api/exams/{examId}/editions")
@Tag(name = "Exam editions", description = "Print runs of an exam written in Typst (T25d)")
public class ExamEditionResource {

    private final Logger log = LoggerFactory.getLogger(ExamEditionResource.class);

    @Inject
    ExamEditionService examEditionService;

    @Inject
    SecurityService securityService;

    @GET
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Print runs of an exam", description = "Oldest first.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The print runs"),
        @APIResponse(responseCode = "403", description = "Current user cannot access this exam")
    })
    public Response list(@Parameter(description = "Id of the exam") @PathParam("examId") long examId, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return forbidden();
        }
        return Response.ok(examEditionService.list(examId)).build();
    }

    @POST
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Record a print run", description = "Returns its identifier, printed in the page codes CE1|edition|copy|page.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The new print run"),
        @APIResponse(responseCode = "400", description = "Number of copies or of pages out of range"),
        @APIResponse(responseCode = "403", description = "Current user cannot access this exam"),
        @APIResponse(responseCode = "404", description = "No such exam")
    })
    public Response create(@PathParam("examId") long examId, EditionRequestDTO request, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return forbidden();
        }
        var refused = EditionRules.check(request == null ? null : request.copies(), request == null ? null : request.pagesPerCopy());
        if (refused.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON)
                    .entity(new ExamSubjectResource.Error("BAD_REQUEST", refused.get())).build();
        }
        return examEditionService.create(examId, request.copies(), request.pagesPerCopy(), ExamSubjectResource.login(ctx))
                .map(e -> Response.ok(e).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    private Response forbidden() {
        log.debug("Access to the print runs of an exam refused");
        return Response.status(403, "Current user cannot access to this ressource").build();
    }
}
