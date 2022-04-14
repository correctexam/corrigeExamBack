package fr.istic.web.rest;

import static javax.ws.rs.core.UriBuilder.fromPath;

import fr.istic.service.StudentResponseService;
import fr.istic.web.rest.errors.BadRequestAlertException;
import fr.istic.web.util.HeaderUtil;
import fr.istic.web.util.ResponseUtil;
import fr.istic.service.dto.StudentResponseDTO;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.istic.service.Paged;
import fr.istic.web.rest.vm.PageRequestVM;
import fr.istic.web.rest.vm.SortRequestVM;
import fr.istic.web.util.PaginationUtil;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link fr.istic.domain.StudentResponse}.
 */
@Path("/api/student-responses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class StudentResponseResource {

    private final Logger log = LoggerFactory.getLogger(StudentResponseResource.class);

    private static final String ENTITY_NAME = "studentResponse";

    @ConfigProperty(name = "application.name")
    String applicationName;


    @Inject
    StudentResponseService studentResponseService;
    /**
     * {@code POST  /student-responses} : Create a new studentResponse.
     *
     * @param studentResponseDTO the studentResponseDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body the new studentResponseDTO, or with status {@code 400 (Bad Request)} if the studentResponse has already an ID.
     */
    @POST
    public Response createStudentResponse(StudentResponseDTO studentResponseDTO, @Context UriInfo uriInfo) {
        log.debug("REST request to save StudentResponse : {}", studentResponseDTO);
        if (studentResponseDTO.id != null) {
            throw new BadRequestAlertException("A new studentResponse cannot already have an ID", ENTITY_NAME, "idexists");
        }
        var result = studentResponseService.persistOrUpdate(studentResponseDTO);
        var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build()).entity(result);
        HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code PUT  /student-responses} : Updates an existing studentResponse.
     *
     * @param studentResponseDTO the studentResponseDTO to update.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the updated studentResponseDTO,
     * or with status {@code 400 (Bad Request)} if the studentResponseDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the studentResponseDTO couldn't be updated.
     */
    @PUT
    public Response updateStudentResponse(StudentResponseDTO studentResponseDTO) {
        log.debug("REST request to update StudentResponse : {}", studentResponseDTO);
        if (studentResponseDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        var result = studentResponseService.persistOrUpdate(studentResponseDTO);
        var response = Response.ok().entity(result);
        HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, studentResponseDTO.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code DELETE  /student-responses/:id} : delete the "id" studentResponse.
     *
     * @param id the id of the studentResponseDTO to delete.
     * @return the {@link Response} with status {@code 204 (NO_CONTENT)}.
     */
    @DELETE
    @Path("/{id}")
    public Response deleteStudentResponse(@PathParam("id") Long id) {
        log.debug("REST request to delete StudentResponse : {}", id);
        studentResponseService.delete(id);
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code GET  /student-responses} : get all the studentResponses.
     *
     * @param pageRequest the pagination information.
     * @return the {@link Response} with status {@code 200 (OK)} and the list of studentResponses in body.
     */
    @GET
    public Response getAllStudentResponses(@BeanParam PageRequestVM pageRequest, @BeanParam SortRequestVM sortRequest, @Context UriInfo uriInfo) {
        log.debug("REST request to get a page of StudentResponses");
        var page = pageRequest.toPage();
        var sort = sortRequest.toSort();
        Paged<StudentResponseDTO> result = null;

        MultivaluedMap param = uriInfo.getQueryParameters();

        if (param.containsKey("sheetId") && param.containsKey("questionId")) {
            List sheetId = (List) param.get("sheetId");
            List questionId = (List) param.get("questionId");
            result = studentResponseService.findStudentResponsesbysheetIdAndquestionId(page, Long.parseLong("" + sheetId.get(0)),
            Long.parseLong("" + questionId.get(0))
            );
        } else{
            result =studentResponseService.findAll(page);
        }



        var response = Response.ok().entity(result.content);
        response = PaginationUtil.withPaginationInfo(response, uriInfo, result);
        return response.build();
    }


    /**
     * {@code GET  /student-responses/:id} : get the "id" studentResponse.
     *
     * @param id the id of the studentResponseDTO to retrieve.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the studentResponseDTO, or with status {@code 404 (Not Found)}.
     */
    @GET
    @Path("/{id}")

    public Response getStudentResponse(@PathParam("id") Long id) {
        log.debug("REST request to get StudentResponse : {}", id);
        Optional<StudentResponseDTO> studentResponseDTO = studentResponseService.findOne(id);
        return ResponseUtil.wrapOrNotFound(studentResponseDTO);
    }
}
