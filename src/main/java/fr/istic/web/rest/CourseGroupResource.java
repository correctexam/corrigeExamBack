package fr.istic.web.rest;

import static javax.ws.rs.core.UriBuilder.fromPath;

import fr.istic.service.CourseGroupService;
import fr.istic.web.rest.errors.BadRequestAlertException;
import fr.istic.web.util.HeaderUtil;
import fr.istic.web.util.ResponseUtil;
import fr.istic.service.dto.CourseGroupDTO;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.istic.service.Paged;
import fr.istic.web.rest.vm.PageRequestVM;
import fr.istic.web.rest.vm.SortRequestVM;
import fr.istic.web.util.PaginationUtil;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link fr.istic.domain.CourseGroup}.
 */
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
    CourseGroupService courseGroupService;
    /**
     * {@code POST  /course-groups} : Create a new courseGroup.
     *
     * @param courseGroupDTO the courseGroupDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body the new courseGroupDTO, or with status {@code 400 (Bad Request)} if the courseGroup has already an ID.
     */
    @POST
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
    @PUT
    public Response updateCourseGroup(@Valid CourseGroupDTO courseGroupDTO) {
        log.debug("REST request to update CourseGroup : {}", courseGroupDTO);
        if (courseGroupDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
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
    @DELETE
    @Path("/{id}")
    public Response deleteCourseGroup(@PathParam("id") Long id) {
        log.debug("REST request to delete CourseGroup : {}", id);
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
    @GET
    public Response getAllCourseGroups(@BeanParam PageRequestVM pageRequest, @BeanParam SortRequestVM sortRequest, @Context UriInfo uriInfo, @QueryParam(value = "eagerload") boolean eagerload) {
        log.debug("REST request to get a page of CourseGroups");
        var page = pageRequest.toPage();
        var sort = sortRequest.toSort();
        Paged<CourseGroupDTO> result;
        if (eagerload) {
            result = courseGroupService.findAllWithEagerRelationships(page);
        } else {
            result = courseGroupService.findAll(page);
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
    @GET
    @Path("/{id}")

    public Response getCourseGroup(@PathParam("id") Long id) {
        log.debug("REST request to get CourseGroup : {}", id);
        Optional<CourseGroupDTO> courseGroupDTO = courseGroupService.findOne(id);
        return ResponseUtil.wrapOrNotFound(courseGroupDTO);
    }
}
