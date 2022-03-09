package fr.istic.web.rest;

import static javax.ws.rs.core.UriBuilder.fromPath;

import fr.istic.service.StudentService;
import fr.istic.web.rest.errors.BadRequestAlertException;
import fr.istic.web.util.HeaderUtil;
import fr.istic.web.util.ResponseUtil;
import fr.istic.service.dto.StudentDTO;

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
 * REST controller for managing {@link fr.istic.domain.Student}.
 */
@Path("/api/students")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class StudentResource {

    private final Logger log = LoggerFactory.getLogger(StudentResource.class);

    private static final String ENTITY_NAME = "student";

    @ConfigProperty(name = "application.name")
    String applicationName;


    @Inject
    StudentService studentService;
    /**
     * {@code POST  /students} : Create a new student.
     *
     * @param studentDTO the studentDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body the new studentDTO, or with status {@code 400 (Bad Request)} if the student has already an ID.
     */
    @POST
    public Response createStudent(@Valid StudentDTO studentDTO, @Context UriInfo uriInfo) {
        log.debug("REST request to save Student : {}", studentDTO);
        if (studentDTO.id != null) {
            throw new BadRequestAlertException("A new student cannot already have an ID", ENTITY_NAME, "idexists");
        }
        var result = studentService.persistOrUpdate(studentDTO);
        var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build()).entity(result);
        HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code PUT  /students} : Updates an existing student.
     *
     * @param studentDTO the studentDTO to update.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the updated studentDTO,
     * or with status {@code 400 (Bad Request)} if the studentDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the studentDTO couldn't be updated.
     */
    @PUT
    public Response updateStudent(@Valid StudentDTO studentDTO) {
        log.debug("REST request to update Student : {}", studentDTO);
        if (studentDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        var result = studentService.persistOrUpdate(studentDTO);
        var response = Response.ok().entity(result);
        HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, studentDTO.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code DELETE  /students/:id} : delete the "id" student.
     *
     * @param id the id of the studentDTO to delete.
     * @return the {@link Response} with status {@code 204 (NO_CONTENT)}.
     */
    @DELETE
    @Path("/{id}")
    public Response deleteStudent(@PathParam("id") Long id) {
        log.debug("REST request to delete Student : {}", id);
        studentService.delete(id);
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code GET  /students} : get all the students.
     *
     * @param pageRequest the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link Response} with status {@code 200 (OK)} and the list of students in body.
     */
    @GET
    public Response getAllStudents(@BeanParam PageRequestVM pageRequest, @BeanParam SortRequestVM sortRequest, @Context UriInfo uriInfo, @QueryParam(value = "eagerload") boolean eagerload) {
        log.debug("REST request to get a page of Students");
        var page = pageRequest.toPage();
        var sort = sortRequest.toSort();
        Paged<StudentDTO> result;
        if (eagerload) {
            result = studentService.findAllWithEagerRelationships(page);
        } else {
            result = studentService.findAll(page);
        }
        var response = Response.ok().entity(result.content);
        response = PaginationUtil.withPaginationInfo(response, uriInfo, result);
        return response.build();
    }


    /**
     * {@code GET  /students/:id} : get the "id" student.
     *
     * @param id the id of the studentDTO to retrieve.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the studentDTO, or with status {@code 404 (Not Found)}.
     */
    @GET
    @Path("/{id}")

    public Response getStudent(@PathParam("id") Long id) {
        log.debug("REST request to get Student : {}", id);
        Optional<StudentDTO> studentDTO = studentService.findOne(id);
        return ResponseUtil.wrapOrNotFound(studentDTO);
    }
}
