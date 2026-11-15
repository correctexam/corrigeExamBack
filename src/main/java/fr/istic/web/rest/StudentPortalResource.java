package fr.istic.web.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import fr.istic.config.StudentPortalConfig;
import fr.istic.service.AltchaService;
import fr.istic.service.StudentPortalService;
import fr.istic.service.customdto.studentportal.AltchaChallengeDTO;
import fr.istic.service.customdto.studentportal.LinkRequestDTO;
import fr.istic.service.customdto.studentportal.SessionRequestDTO;
import fr.istic.service.customdto.studentportal.StudentSessionDTO;
import io.vertx.core.http.HttpServerRequest;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Entry of the student portal (ADR 0003): captcha, link sent by e-mail, opening of the student session. Public by
 * design; disabled (404) when correctexam.student-portal.enabled=false.
 */
@Tag(name = "Student portal (public)", description = "Link to the student portal sent by e-mail, and student session.")
@Path("/api/public/my")
@ApplicationScoped
@PermitAll
@Produces(MediaType.APPLICATION_JSON)
public class StudentPortalResource {

    @Inject
    StudentPortalConfig config;

    @Inject
    AltchaService altchaService;

    @Inject
    StudentPortalService studentPortalService;

    @Operation(summary = "Get an ALTCHA challenge", description = "Proof-of-work challenge to solve before asking for a link.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "The challenge",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = AltchaChallengeDTO.class))),
            @APIResponse(responseCode = "404", description = "The student portal is disabled")
        }
    )
    @GET
    @Path("/challenge")
    public Response challenge() {
        if (!config.enabled()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(altchaService.createChallenge()).header("Cache-Control", "no-store").build();
    }

    @Operation(summary = "Ask for a link to the student portal",
        description = "Sends a single-use link to the address if a student record carries it. The answer is the same "
            + "whether the address is known or not.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "202", description = "Accepted: an e-mail is sent if the address is known"),
            @APIResponse(responseCode = "400", description = "Malformed address, or captcha missing, wrong, expired or already used"),
            @APIResponse(responseCode = "404", description = "The student portal is disabled"),
            @APIResponse(responseCode = "429", description = "Too many requests for this address or from this network")
        }
    )
    @POST
    @Path("/link")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response requestLink(LinkRequestDTO request, @Context HttpServerRequest http) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        String ip = http != null && http.remoteAddress() != null ? http.remoteAddress().hostAddress() : null;
        return switch (studentPortalService.requestLink(request.email, request.altcha, ip, request.lang)) {
            case ACCEPTED -> Response.accepted().build();
            case INVALID_EMAIL -> Response.status(Response.Status.BAD_REQUEST).entity(new Error("invalidEmail")).build();
            case INVALID_CAPTCHA -> Response.status(Response.Status.BAD_REQUEST).entity(new Error("invalidCaptcha")).build();
            case TOO_MANY_REQUESTS -> Response.status(Response.Status.TOO_MANY_REQUESTS)
                    .header("Retry-After", config.requestWindow().toSeconds()).entity(new Error("tooManyRequests")).build();
            case DISABLED -> Response.status(Response.Status.NOT_FOUND).build();
        };
    }

    @Operation(summary = "Open a student session",
        description = "Exchanges the token of the link (single use) for a session with the ROLE_STUDENT role.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "The session",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = StudentSessionDTO.class))),
            @APIResponse(responseCode = "401", description = "Unknown, expired or already used link")
        }
    )
    @POST
    @Path("/session")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response openSession(SessionRequestDTO request) {
        return studentPortalService.openSession(request == null ? null : request.token)
                .map(session -> Response.ok(session).header("Cache-Control", "no-store").build())
                .orElseGet(() -> Response.status(Response.Status.UNAUTHORIZED).build());
    }

    /** Error code for the front-end translations. */
    public record Error(String error) {
    }
}
