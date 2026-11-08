package fr.istic.web.rest;

import static jakarta.ws.rs.core.UriBuilder.fromPath;

import fr.istic.domain.CourseGroup;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.CourseGroupService;
import fr.istic.web.rest.errors.BadRequestAlertException;
import fr.istic.web.util.HeaderUtil;
import fr.istic.web.util.ResponseUtil;
import fr.istic.service.dto.CourseGroupDTO;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.istic.service.Paged;
import fr.istic.service.SecurityService;
import fr.istic.web.rest.vm.PageRequestVM;
import fr.istic.web.rest.vm.SortRequestVM;
import fr.istic.web.util.PaginationUtil;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link fr.istic.domain.CourseGroup}.
 */
@Tag(
    name = "CourseGroup",
    description = "Management of course groups: create, update, delete, and search course groups."
)
@Path("/api/course-groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class CourseGroupResource {

    private final Logger log = LoggerFactory.getLogger(CourseGroupResource.class);

    private static final String ENTITY_NAME = "courseGroup";

    @ConfigProperty(name = "application.name")
    String applicationName;
    @Inject
    SecurityService securityService;


    @Inject
    CourseGroupService courseGroupService;
    /**
     * {@code POST  /course-groups} : Create a new courseGroup.
     *
     * @param courseGroupDTO the courseGroupDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body the new courseGroupDTO, or with status {@code 400 (Bad Request)} if the courseGroup has already an ID.
     */
    @Operation(
        summary = "Create a course group",
        description = "Creates a new course group and returns the created course group."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "201",
                description = "The created course group",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CourseGroupDTO.class))
            ),
            @APIResponse(responseCode = "400", description = "The new course group cannot already have an ID")
        }
    )
    @POST
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response createCourseGroup(@Valid CourseGroupDTO courseGroupDTO, @Context UriInfo uriInfo) {
        log.debug("REST request to save CourseGroup : {}", courseGroupDTO);
        if (courseGroupDTO.id != null) {
            throw new BadRequestAlertException("A new courseGroup cannot already have an ID", ENTITY_NAME, "idexists");
        }
        var result = courseGroupService.persistOrUpdate(courseGroupDTO);
        var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build()).entity(result);
        HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code PUT  /course-groups} : Updates an existing courseGroup.
     *
     * @param courseGroupDTO the courseGroupDTO to update.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the updated courseGroupDTO,
     * or with status {@code 400 (Bad Request)} if the courseGroupDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the courseGroupDTO couldn't be updated.
     */
    @Operation(
        summary = "Update a course group",
        description = "Updates an existing course group and returns the updated course group."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The updated course group",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CourseGroupDTO.class))
            ),
            @APIResponse(responseCode = "400", description = "Course group with an invalid (null) ID"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "500", description = "The course group couldn't be updated")
        }
    )
    @PUT
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response updateCourseGroup(@Valid CourseGroupDTO courseGroupDTO, @Context SecurityContext ctx) {
        log.debug("REST request to update CourseGroup : {}", courseGroupDTO);
        if (courseGroupDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!securityService.canAccess(ctx, courseGroupDTO.id, CourseGroup.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        };

        var result = courseGroupService.persistOrUpdate(courseGroupDTO);
        var response = Response.ok().entity(result);
        HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, courseGroupDTO.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code DELETE  /course-groups/:id} : delete the "id" courseGroup.
     *
     * @param id the id of the courseGroupDTO to delete.
     * @return the {@link Response} with status {@code 204 (NO_CONTENT)}.
     */
    @Operation(
        summary = "Delete a course group",
        description = "Deletes the course group with the given id."
    )
    @APIResponses(
        value = {
            @APIResponse(responseCode = "204", description = "Course group deleted"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @DELETE
    @Path("/{id}")
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response deleteCourseGroup(
        @Parameter(description = "Id of the course group to delete", required = true) @PathParam("id") Long id,
        @Context SecurityContext ctx
    ) {
        log.debug("REST request to delete CourseGroup : {}", id);
        if (!securityService.canAccess(ctx, id, CourseGroup.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        };

        courseGroupService.delete(id);
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code GET  /course-groups} : get all the courseGroups.
     *
     * @param pageRequest the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link Response} with status {@code 200 (OK)} and the list of courseGroups in body.
     */
    @Operation(
        summary = "Get all course groups",
        description = "Returns a paged list of all course groups, optionally loading related entities."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The list of course groups",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = CourseGroupDTO.class))
            )
        }
    )
    @GET
    @RolesAllowed({AuthoritiesConstants.ADMIN})
    public Response getAllCourseGroups(@BeanParam PageRequestVM pageRequest, @BeanParam SortRequestVM sortRequest, @Context UriInfo uriInfo,
        @Parameter(description = "Flag to eagerly load related entities (many-to-many relationships)", required = false) @QueryParam(value = "eagerload") boolean eagerload,
        @Context SecurityContext ctx) {
        log.debug("REST request to get a page of CourseGroups");
        var page = pageRequest.toPage();
        var sort = sortRequest.toSort();
        Paged<CourseGroupDTO> result;
        if (eagerload) {
            result = courseGroupService.findAllWithEagerRelationships(page);
        } else {
            result = courseGroupService.findAllWithEagerRelationships(page);
//            result = courseGroupService.findAll(page);
        }
        var response = Response.ok().entity(result.content);
        response = PaginationUtil.withPaginationInfo(response, uriInfo, result);
        return response.build();
    }


    /**
     * {@code GET  /course-groups/:id} : get the "id" courseGroup.
     *
     * @param id the id of the courseGroupDTO to retrieve.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the courseGroupDTO, or with status {@code 404 (Not Found)}.
     */
    @Operation(
        summary = "Get a course group",
        description = "Returns the course group with the given id."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The course group",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CourseGroupDTO.class))
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "404", description = "Course group not found")
        }
    )
    @GET
    @Path("/{id}")
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response getCourseGroup(
        @Parameter(description = "Id of the course group to retrieve", required = true) @PathParam("id") Long id,
        @Context SecurityContext ctx
    ) {
        log.debug("REST request to get CourseGroup : {}", id);
        if (!securityService.canAccess(ctx, id, CourseGroup.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        };
        Optional<CourseGroupDTO> courseGroupDTO = courseGroupService.findOne(id);
        return ResponseUtil.wrapOrNotFound(courseGroupDTO);
    }
}
