package fr.istic.web.rest;

import static jakarta.ws.rs.core.UriBuilder.fromPath;

import fr.istic.domain.Authority;
import fr.istic.domain.Course;
import fr.istic.domain.User;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.CourseService;
import fr.istic.web.rest.errors.AccountResourceException;
import fr.istic.web.rest.errors.BadRequestAlertException;
import fr.istic.web.util.HeaderUtil;
import fr.istic.web.util.ResponseUtil;
import fr.istic.service.dto.CourseDTO;
import fr.istic.service.mapper.UserMapper;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.istic.service.Paged;
import fr.istic.service.SecurityService;
import fr.istic.web.rest.vm.PageRequestVM;
import fr.istic.web.rest.vm.SortRequestVM;
import fr.istic.web.util.PaginationUtil;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.Optional;

/**
 * REST controller for managing {@link fr.istic.domain.Course}.
 */
@Path("/api/courses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class CourseResource {



    private final Logger log = LoggerFactory.getLogger(CourseResource.class);

    private static final String ENTITY_NAME = "course";

    @ConfigProperty(name = "application.name")
    String applicationName;


    @Inject
    CourseService courseService;

    @Inject
    SecurityService securityService;

    @Inject
    UserMapper userMapper;


    /**
     * {@code POST  /courses} : Create a new course.
     *
     * @param courseDTO the courseDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body the new courseDTO, or with status {@code 400 (Bad Request)} if the course has already an ID.
     */
    @POST
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response createCourse(@Valid CourseDTO courseDTO, @Context UriInfo uriInfo, @Context SecurityContext ctx) {
        log.debug("REST request to save Course : {}", courseDTO);
        if (courseDTO.id != null) {
            throw new BadRequestAlertException("A new course cannot already have an ID", ENTITY_NAME, "idexists");
        }

        var userLogin = Optional
            .ofNullable(ctx.getUserPrincipal().getName());
        if (!userLogin.isPresent()){
            throw new AccountResourceException("Current user login not found");
        }
        var user = User.findOneByLogin(userLogin.get());
        if (!user.isPresent()) {
            throw new AccountResourceException("User could not be found");
        }
        if (!userLogin.equals("system")&& courseDTO.profs.stream().filter(c -> userLogin.equals(c.login)).count() == 0 ){
            courseDTO.profs.add(  userMapper.userToUserDTO(user.get()));
//            courseDTO.profId =user.get().id;
        }

        var result = courseService.persistOrUpdate(courseDTO);
        var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build()).entity(result);
        HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code PUT  /courses} : Updates an existing course.
     *
     * @param courseDTO the courseDTO to update.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the updated courseDTO,
     * or with status {@code 400 (Bad Request)} if the courseDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the courseDTO couldn't be updated.
     */
    @PUT
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response updateCourse(@Valid CourseDTO courseDTO, @Context SecurityContext ctx) {
        log.debug("REST request to update Course : {}", courseDTO);
        if (courseDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!securityService.canAccess(ctx, courseDTO.id, Course.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        var result = courseService.persistOrUpdate(courseDTO);
        var response = Response.ok().entity(result);
        HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, courseDTO.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code DELETE  /courses/:id} : delete the "id" course.
     *
     * @param id the id of the courseDTO to delete.
     * @return the {@link Response} with status {@code 204 (NO_CONTENT)}.
     */
    @DELETE
    @Path("/{id}")
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response deleteCourse(@PathParam("id") Long id, @Context SecurityContext ctx) {
        log.debug("REST request to delete Course : {}", id);
        if (!securityService.canAccess(ctx, id, Course.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        courseService.delete(id);

        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code GET  /courses} : get all the courses.
     *
     * @param pageRequest the pagination information.
     * @return the {@link Response} with status {@code 200 (OK)} and the list of courses in body.
     */
    @GET
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})

    public Response getAllCourses(@BeanParam PageRequestVM pageRequest, @BeanParam SortRequestVM sortRequest, @Context UriInfo uriInfo , @Context SecurityContext ctx) {
        log.debug("REST request to get a page of Courses");
        var page = pageRequest.toPage();
        var sort = sortRequest.toSort();

        var userLogin = Optional
        .ofNullable(ctx.getUserPrincipal().getName());
    if (!userLogin.isPresent()){
        throw new AccountResourceException("Current user login not found");
    }
    var user = User.findOneByLogin(userLogin.get());
    if (!user.isPresent()) {
        throw new AccountResourceException("User could not be found");
    }
    Paged<CourseDTO> result = null;
        if (user.get().authorities.size() == 1 && user.get().authorities.stream().anyMatch(e1-> e1.equals(new Authority("ROLE_USER")))){
            result = courseService.findAll4User(page,user.get());

        } else {

        result = courseService.findAll(page);
        }

        var response = Response.ok().entity(result.content);
        response = PaginationUtil.withPaginationInfo(response, uriInfo, result);
        return response.build();
    }


    /**
     * {@code GET  /courses/:id} : get the "id" course.
     *
     * @param id the id of the courseDTO to retrieve.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the courseDTO, or with status {@code 404 (Not Found)}.
     */
    @GET
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})

    @Path("/{id}")
    public Response getCourse(@PathParam("id") Long id, @Context SecurityContext ctx) {
        log.debug("REST request to get Course : {}", id);
        if (!securityService.canAccess(ctx, id, Course.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        Optional<CourseDTO> courseDTO = courseService.findOne(id);
        return ResponseUtil.wrapOrNotFound(courseDTO);
    }
}
