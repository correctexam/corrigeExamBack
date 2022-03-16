package fr.istic.web.rest;

import static javax.ws.rs.core.UriBuilder.fromPath;

import fr.istic.service.QuestionService;
import fr.istic.web.rest.errors.BadRequestAlertException;
import fr.istic.web.util.HeaderUtil;
import fr.istic.web.util.ResponseUtil;
import fr.istic.service.dto.QuestionDTO;

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
 * REST controller for managing {@link fr.istic.domain.Question}.
 */
@Path("/api/questions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class QuestionResource {

    private final Logger log = LoggerFactory.getLogger(QuestionResource.class);

    private static final String ENTITY_NAME = "question";

    @ConfigProperty(name = "application.name")
    String applicationName;


    @Inject
    QuestionService questionService;
    /**
     * {@code POST  /questions} : Create a new question.
     *
     * @param questionDTO the questionDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body the new questionDTO, or with status {@code 400 (Bad Request)} if the question has already an ID.
     */
    @POST
    public Response createQuestion(@Valid QuestionDTO questionDTO, @Context UriInfo uriInfo) {
        log.debug("REST request to save Question : {}", questionDTO);
        if (questionDTO.id != null) {
            throw new BadRequestAlertException("A new question cannot already have an ID", ENTITY_NAME, "idexists");
        }
        var result = questionService.persistOrUpdate(questionDTO);
        var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build()).entity(result);
        HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code PUT  /questions} : Updates an existing question.
     *
     * @param questionDTO the questionDTO to update.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the updated questionDTO,
     * or with status {@code 400 (Bad Request)} if the questionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the questionDTO couldn't be updated.
     */
    @PUT
    public Response updateQuestion(@Valid QuestionDTO questionDTO) {
        log.debug("REST request to update Question : {}", questionDTO);
        if (questionDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        var result = questionService.persistOrUpdate(questionDTO);
        var response = Response.ok().entity(result);
        HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, questionDTO.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code DELETE  /questions/:id} : delete the "id" question.
     *
     * @param id the id of the questionDTO to delete.
     * @return the {@link Response} with status {@code 204 (NO_CONTENT)}.
     */
    @DELETE
    @Path("/{id}")
    public Response deleteQuestion(@PathParam("id") Long id) {
        log.debug("REST request to delete Question : {}", id);
        questionService.delete(id);
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code GET  /questions} : get all the questions.
     *
     * @param pageRequest the pagination information.
     * @return the {@link Response} with status {@code 200 (OK)} and the list of questions in body.
     */
    @GET
    public Response getAllQuestions(@BeanParam PageRequestVM pageRequest, @BeanParam SortRequestVM sortRequest, @Context UriInfo uriInfo) {
        log.debug("REST request to get a page of Questions");
            var page = pageRequest.toPage();
            var sort = sortRequest.toSort();
            Paged<QuestionDTO> result =null;
            MultivaluedMap param =  uriInfo.getQueryParameters();
            if (param.containsKey("examId")){
                List id = (List) param.get("examId");
                result =questionService.findQuestionbyExamId(page, Long.parseLong("" + id.get(0)));
            }
            else {
        result = questionService.findAll(page);

                    }
        var response = Response.ok().entity(result.content);
        response = PaginationUtil.withPaginationInfo(response, uriInfo, result);
        return response.build();
    }


    /**
     * {@code GET  /questions/:id} : get the "id" question.
     *
     * @param id the id of the questionDTO to retrieve.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the questionDTO, or with status {@code 404 (Not Found)}.
     */
    @GET
    @Path("/{id}")

    public Response getQuestion(@PathParam("id") Long id) {
        log.debug("REST request to get Question : {}", id);
        Optional<QuestionDTO> questionDTO = questionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(questionDTO);
    }
}
