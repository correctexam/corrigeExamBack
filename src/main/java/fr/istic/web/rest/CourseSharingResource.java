package fr.istic.web.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import fr.istic.domain.Course;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.CourseService;
import fr.istic.service.SecurityService;
import fr.istic.service.customdto.ListUserModelShare;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Teachers who share a course.
 * <p>
 * Split out of the former {@code ExtendedAPI} class (T3): the REST paths are unchanged.
 */
@Tag(name = "Course sharing", description = "Teachers who share a course.")
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class CourseSharingResource {

    @Inject
    CourseService courseService;

    @Inject
    SecurityService securityService;

    /**
     * {@code GET /users} : get all users.
     *
     * @param pagination the pagination information.
     * @return the {@link Response} with status {@code 200 (OK)} and with body all
     *         users.
     */
    @Operation(summary = "Get the users of a course", description = "Returns the teachers and shared users of the course for the current user.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "The users of the course"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @GET
    @Path("/getUsers/{courseId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })

    public Response getAllUsers(@Parameter(description = "ID of the course", required = true) @PathParam("courseId") long courseId, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, courseId, Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        var login = ctx.getUserPrincipal().getName();

        var res = this.courseService.getAllListUserModelShare(courseId, login);

        Response.ResponseBuilder response = Response.ok().entity(res);
        return response.build();
    }

    /**
     * {@code GET /users} : get all users.
     *
     * @param pagination the pagination information.
     * @return the {@link Response} with status {@code 200 (OK)} and with body all
     *         users.
     */
    @Operation(summary = "Get the teacher emails of a course", description = "Returns the email addresses of all teachers of the course.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "The teacher email addresses")
        }
    )
    @GET
    @Path("/getAllEmailProfs4course/{courseId}")
    public Response getAllEmailProfs4course(@Parameter(description = "ID of the course", required = true) @PathParam("courseId") long courseId) {
        var res = this.courseService.getAllProfMail(courseId);
        Response.ResponseBuilder response = Response.ok().entity(res);
        return response.build();
    }

    /**
     * {@code POST
     *
     * @param pagination the pagination information.
     *
     * @return the {@link Response} with status {@code 200 (OK)} and with body all
     *         users.
     */
    @Operation(summary = "Update the teachers of a course", description = "Adds and removes teachers shared on the course.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The updated list of users",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ListUserModelShare.class))
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @PUT
    @Path("/updateProfs/{courseId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response updateProfs(@Parameter(description = "ID of the course", required = true) @PathParam("courseId") long courseId, ListUserModelShare updateLists,
            @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, courseId, Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        if (updateLists.getAvailables() != null && updateLists.getAvailables().size() > 0) {
            this.courseService.addProfs(courseId, updateLists.getAvailables());
        }
        if (updateLists.getShared() != null && updateLists.getShared().size() > 0) {
            this.courseService.removeProfs(courseId, updateLists.getShared());

        }
        Response.ResponseBuilder response = Response.ok().entity(updateLists);
        return response.build();
    }
}
