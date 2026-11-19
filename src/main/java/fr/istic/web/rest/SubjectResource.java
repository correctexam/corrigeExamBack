package fr.istic.web.rest;

import java.io.InputStream;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.dto.DiagramRequestDTO;
import fr.istic.service.subject.DiagramService;
import fr.istic.service.subject.ExamSubjectService;
import fr.istic.service.subject.KrokiException;
import fr.istic.service.subject.SubjectException;
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
 * Typst subjects outside of one exam (T25c, ADR 0004): subjects to reuse, and the text diagrams rendered by Kroki
 * (shared content-addressed cache, usable before the exam exists).
 */
@Path("/api")
@Tag(name = "Subjects", description = "Reusable Typst subjects and text diagrams (T25c)")
public class SubjectResource {

    @Inject
    ExamSubjectService examSubjectService;

    @Inject
    DiagramService diagramService;

    @GET
    @Path("/subjects")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Subjects the user can reuse",
        description = "Subjects of the exams of the courses the user teaches (all for an administrator), latest first.")
    @APIResponse(responseCode = "200", description = "The subjects")
    public Response listReusable(@Context SecurityContext ctx) {
        return Response.ok(examSubjectService.listReusable(ctx.getUserPrincipal().getName(),
                ctx.isUserInRole(AuthoritiesConstants.ADMIN))).build();
    }

    @POST
    @Path("/subject-diagrams")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Render a text diagram",
        description = "Renders the source with the self-hosted Kroki, or finds it in the cache. Kinds: plantuml, c4plantuml, "
            + "mermaid, d2, nomnoml.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Kind and hash of the rendered diagram"),
        @APIResponse(responseCode = "400", description = "Unknown kind"),
        @APIResponse(responseCode = "413", description = "Source or SVG too large"),
        @APIResponse(responseCode = "422", description = "Kroki refused the source: the message explains why"),
        @APIResponse(responseCode = "503", description = "Kroki or the object storage is not configured or unreachable")
    })
    public Response render(DiagramRequestDTO request) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        try {
            return Response.ok(diagramService.render(request.kind(), request.source())).build();
        } catch (SubjectException e) {
            return ExamSubjectResource.error(e);
        } catch (KrokiException e) {
            Response.Status status = switch (e.getReason()) {
                case INVALID_SOURCE -> null;
                case TOO_LARGE -> Response.Status.REQUEST_ENTITY_TOO_LARGE;
                case NOT_CONFIGURED, UNAVAILABLE -> Response.Status.SERVICE_UNAVAILABLE;
            };
            return Response.status(status == null ? 422 : status.getStatusCode()).type(MediaType.APPLICATION_JSON)
                    .entity(new ExamSubjectResource.Error(e.getReason().name(), e.getMessage())).build();
        }
    }

    @GET
    @Path("/subject-diagrams/{kind}/{sha256}.svg")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Operation(summary = "Read a rendered diagram")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The SVG"),
        @APIResponse(responseCode = "404", description = "Not rendered")
    })
    public Response read(@PathParam("kind") String kind, @PathParam("sha256") String sha256) {
        try {
            InputStream svg = diagramService.read(kind, sha256);
            return Response.ok(svg, "image/svg+xml")
                    .header("X-Content-Type-Options", "nosniff")
                    .header("Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'; sandbox")
                    // content-addressed: never changes
                    .header("Cache-Control", "private, max-age=31536000, immutable")
                    .build();
        } catch (SubjectException e) {
            return ExamSubjectResource.error(e);
        }
    }
}
