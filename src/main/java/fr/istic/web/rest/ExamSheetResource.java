package fr.istic.web.rest;

import static javax.ws.rs.core.UriBuilder.fromPath;

import fr.istic.service.ExamSheetService;
import fr.istic.web.rest.errors.BadRequestAlertException;
import fr.istic.web.util.HeaderUtil;
import fr.istic.web.util.ResponseUtil;
import fr.istic.service.dto.ExamSheetDTO;

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
 * REST controller for managing {@link fr.istic.domain.ExamSheet}.
 */
@Path("/api/exam-sheets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ExamSheetResource {

    private final Logger log = LoggerFactory.getLogger(ExamSheetResource.class);

    private static final String ENTITY_NAME = "examSheet";

    @ConfigProperty(name = "application.name")
    String applicationName;


    @Inject
    ExamSheetService examSheetService;
    /**
     * {@code POST  /exam-sheets} : Create a new examSheet.
     *
     * @param examSheetDTO the examSheetDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body the new examSheetDTO, or with status {@code 400 (Bad Request)} if the examSheet has already an ID.
     */
    @POST
    public Response createExamSheet(@Valid ExamSheetDTO examSheetDTO, @Context UriInfo uriInfo) {
        log.debug("REST request to save ExamSheet : {}", examSheetDTO);
        if (examSheetDTO.id != null) {
            throw new BadRequestAlertException("A new examSheet cannot already have an ID", ENTITY_NAME, "idexists");
        }
        var result = examSheetService.persistOrUpdate(examSheetDTO);
        var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build()).entity(result);
        HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code PUT  /exam-sheets} : Updates an existing examSheet.
     *
     * @param examSheetDTO the examSheetDTO to update.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the updated examSheetDTO,
     * or with status {@code 400 (Bad Request)} if the examSheetDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the examSheetDTO couldn't be updated.
     */
    @PUT
    public Response updateExamSheet(@Valid ExamSheetDTO examSheetDTO) {
        log.debug("REST request to update ExamSheet : {}", examSheetDTO);
        if (examSheetDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        var result = examSheetService.persistOrUpdate(examSheetDTO);
        var response = Response.ok().entity(result);
        HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, examSheetDTO.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code DELETE  /exam-sheets/:id} : delete the "id" examSheet.
     *
     * @param id the id of the examSheetDTO to delete.
     * @return the {@link Response} with status {@code 204 (NO_CONTENT)}.
     */
    @DELETE
    @Path("/{id}")
    public Response deleteExamSheet(@PathParam("id") Long id) {
        log.debug("REST request to delete ExamSheet : {}", id);
        examSheetService.delete(id);
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code GET  /exam-sheets} : get all the examSheets.
     *
     * @param pageRequest the pagination information.
     * @return the {@link Response} with status {@code 200 (OK)} and the list of examSheets in body.
     */
    @GET
    public Response getAllExamSheets(@BeanParam PageRequestVM pageRequest, @BeanParam SortRequestVM sortRequest, @Context UriInfo uriInfo) {
        log.debug("REST request to get a page of ExamSheets");
        var page = pageRequest.toPage();
        var sort = sortRequest.toSort();
        Paged<ExamSheetDTO> result = examSheetService.findAll(page);
        var response = Response.ok().entity(result.content);
        response = PaginationUtil.withPaginationInfo(response, uriInfo, result);
        return response.build();
    }


    /**
     * {@code GET  /exam-sheets/:id} : get the "id" examSheet.
     *
     * @param id the id of the examSheetDTO to retrieve.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the examSheetDTO, or with status {@code 404 (Not Found)}.
     */
    @GET
    @Path("/{id}")

    public Response getExamSheet(@PathParam("id") Long id) {
        log.debug("REST request to get ExamSheet : {}", id);
        Optional<ExamSheetDTO> examSheetDTO = examSheetService.findOne(id);
        return ResponseUtil.wrapOrNotFound(examSheetDTO);
    }
}
