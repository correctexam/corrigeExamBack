package fr.istic.web.rest;

import static javax.ws.rs.core.UriBuilder.fromPath;

import fr.istic.service.QuestionTypeService;
import fr.istic.web.rest.errors.BadRequestAlertException;
import fr.istic.web.util.HeaderUtil;
import fr.istic.web.util.ResponseUtil;
import fr.istic.service.dto.QuestionTypeDTO;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.Paged;
import fr.istic.web.rest.vm.PageRequestVM;
import fr.istic.web.rest.vm.SortRequestVM;
import fr.istic.web.util.PaginationUtil;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link fr.istic.domain.QuestionType}.
 */
@Path("/api/question-types")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class QuestionTypeResource {

    private final Logger log = LoggerFactory.getLogger(QuestionTypeResource.class);

    private static final String ENTITY_NAME = "questionType";

    @ConfigProperty(name = "application.name")
    String applicationName;


    @Inject
    QuestionTypeService questionTypeService;
    /**
     * {@code POST  /question-types} : Create a new questionType.
     *
     * @param questionTypeDTO the questionTypeDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body the new questionTypeDTO, or with status {@code 400 (Bad Request)} if the questionType has already an ID.
     */
    @POST
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response createQuestionType(@Valid QuestionTypeDTO questionTypeDTO, @Context UriInfo uriInfo) {
        log.debug("REST request to save QuestionType : {}", questionTypeDTO);
        if (questionTypeDTO.id != null) {
            throw new BadRequestAlertException("A new questionType cannot already have an ID", ENTITY_NAME, "idexists");
        }
        var result = questionTypeService.persistOrUpdate(questionTypeDTO);
        var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build()).entity(result);
        HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code PUT  /question-types} : Updates an existing questionType.
     *
     * @param questionTypeDTO the questionTypeDTO to update.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the updated questionTypeDTO,
     * or with status {@code 400 (Bad Request)} if the questionTypeDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the questionTypeDTO couldn't be updated.
     */
    @PUT
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response updateQuestionType(@Valid QuestionTypeDTO questionTypeDTO) {
        log.debug("REST request to update QuestionType : {}", questionTypeDTO);
        if (questionTypeDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        var result = questionTypeService.persistOrUpdate(questionTypeDTO);
        var response = Response.ok().entity(result);
        HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, questionTypeDTO.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code DELETE  /question-types/:id} : delete the "id" questionType.
     *
     * @param id the id of the questionTypeDTO to delete.
     * @return the {@link Response} with status {@code 204 (NO_CONTENT)}.
     */
    @DELETE
    @Path("/{id}")
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response deleteQuestionType(@PathParam("id") Long id) {
        log.debug("REST request to delete QuestionType : {}", id);
        questionTypeService.delete(id);
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code GET  /question-types} : get all the questionTypes.
     *
     * @param pageRequest the pagination information.
     * @return the {@link Response} with status {@code 200 (OK)} and the list of questionTypes in body.
     */
    @GET
    public Response getAllQuestionTypes(@BeanParam PageRequestVM pageRequest, @BeanParam SortRequestVM sortRequest, @Context UriInfo uriInfo) {
        log.debug("REST request to get a page of QuestionTypes");
        var page = pageRequest.toPage();
        var sort = sortRequest.toSort();
        Paged<QuestionTypeDTO> result = questionTypeService.findAll(page);
        var response = Response.ok().entity(result.content);
        response = PaginationUtil.withPaginationInfo(response, uriInfo, result);
        return response.build();
    }


    /**
     * {@code GET  /question-types/:id} : get the "id" questionType.
     *
     * @param id the id of the questionTypeDTO to retrieve.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the questionTypeDTO, or with status {@code 404 (Not Found)}.
     */
    @GET
    @Path("/{id}")

    public Response getQuestionType(@PathParam("id") Long id) {
        log.debug("REST request to get QuestionType : {}", id);
        Optional<QuestionTypeDTO> questionTypeDTO = questionTypeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(questionTypeDTO);
    }
}
