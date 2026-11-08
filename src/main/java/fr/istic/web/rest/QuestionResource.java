package fr.istic.web.rest;

import static jakarta.ws.rs.core.UriBuilder.fromPath;

import fr.istic.service.QuestionService;
import fr.istic.service.SecurityService;
import fr.istic.web.rest.errors.AccountResourceException;
import fr.istic.web.rest.errors.BadRequestAlertException;
import fr.istic.web.util.HeaderUtil;
import fr.istic.web.util.ResponseUtil;
import fr.istic.service.dto.QuestionDTO;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.domain.Authority;
import fr.istic.domain.Question;
import fr.istic.domain.User;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.Paged;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link fr.istic.domain.Question}.
 */
@Tag(
    name = "Question",
    description = "Management of questions: create, update, delete, and search questions."
)
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
    @Inject
    SecurityService securityService;

    /**
     * {@code POST  /questions} : Create a new question.
     *
     * @param questionDTO the questionDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body
     *         the new questionDTO, or with status {@code 400 (Bad Request)} if the
     *         question has already an ID.
     */
    @Operation(
        summary = "Create a question",
        description = "Creates a new question and returns the created question."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "201",
                description = "The created question",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = QuestionDTO.class))
            ),
            @APIResponse(responseCode = "400", description = "The new question cannot already have an ID")
        }
    )
    @POST
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})

    public Response createQuestion(@Valid QuestionDTO questionDTO, @Context UriInfo uriInfo) {
        log.debug("REST request to save Question : {}", questionDTO);
        if (questionDTO.id != null) {
            throw new BadRequestAlertException("A new question cannot already have an ID", ENTITY_NAME, "idexists");
        }
        var result = questionService.persistOrUpdate(questionDTO);
        var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build()).entity(result);
        HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString())
                .forEach(response::header);
        return response.build();
    }


    /**
     * {@code PUT  /questions} : Updates an existing question.
     *
     * @param questionDTO the questionDTO to update.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the
     *         updated questionDTO,
     *         or with status {@code 400 (Bad Request)} if the questionDTO is not
     *         valid,
     *         or with status {@code 500 (Internal Server Error)} if the questionDTO
     *         couldn't be updated.
     */
    @Operation(
        summary = "Update a question",
        description = "Updates an existing question and returns the updated question."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The updated question",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = QuestionDTO.class))
            ),
            @APIResponse(responseCode = "400", description = "Question with an invalid (null) ID"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "500", description = "The question couldn't be updated")
        }
    )
    @PUT
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response updateQuestion(@Valid QuestionDTO questionDTO, @Context SecurityContext ctx) {
        log.debug("REST request to update Question : {}", questionDTO);
        if (questionDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!securityService.canAccess(ctx, questionDTO.id, Question.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        };
        var result = questionService.persistOrUpdate(questionDTO);

        var response = Response.ok().entity(result);
        HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, questionDTO.id.toString())
                .forEach(response::header);
        return response.build();
    }

    /**
     * {@code DELETE  /questions/:id} : delete the "id" question.
     *
     * @param id the id of the questionDTO to delete.
     * @return the {@link Response} with status {@code 204 (NO_CONTENT)}.
     */
    @Operation(
        summary = "Delete a question",
        description = "Deletes the question with the given id."
    )
    @APIResponses(
        value = {
            @APIResponse(responseCode = "204", description = "Question deleted"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @DELETE
    @Path("/{id}")
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response deleteQuestion(
        @Parameter(description = "Id of the question to delete", required = true) @PathParam("id") Long id,
        @Context SecurityContext ctx
    ) {
        log.debug("REST request to delete Question : {}", id);
        if (!securityService.canAccess(ctx, id, Question.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        };

        questionService.delete(id);
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())
                .forEach(response::header);
        return response.build();
    }

    /**
     * {@code GET  /questions} : get all the questions.
     *
     * @param pageRequest the pagination information.
     * @return the {@link Response} with status {@code 200 (OK)} and the list of
     *         questions in body.
     */
    @Operation(
        summary = "Get all questions",
        description = "Returns a paged list of questions. Optionally filter by examId, by examId with numero, or by zoneId."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The list of questions",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = QuestionDTO.class))
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "500", description = "Current user not found")
        }
    )
    @GET
    public Response getAllQuestions(@BeanParam PageRequestVM pageRequest, @BeanParam SortRequestVM sortRequest,
            @Context UriInfo uriInfo, @Context SecurityContext ctx) {
        log.debug("REST request to get a page of Questions");
        var page = pageRequest.toPage();
        var sort = sortRequest.toSort();
        Paged<QuestionDTO> result = new Paged<>(0,0,0,0,new ArrayList<>());
        MultivaluedMap param = uriInfo.getQueryParameters();
        if (param.containsKey("examId") && param.containsKey("numero")) {
            List id = (List) param.get("examId");
            List numero = (List) param.get("numero");
            result = questionService.findQuestionbyExamIdAndNumero(page, Long.parseLong("" + id.get(0)),
            Integer.parseInt("" + numero.get(0))
            );
        }
        else if (param.containsKey("examId")) {
            List id = (List) param.get("examId");
            result = questionService.findQuestionbyExamId(page, Long.parseLong("" + id.get(0)));
        } else if (param.containsKey("zoneId")) {
            List id = (List) param.get("zoneId");
            result = questionService.findQuestionbyZoneId(page, Long.parseLong("" + id.get(0)));
        }

        else {
            if (ctx.getUserPrincipal().getName()!= null){

            var userLogin = Optional
            .ofNullable(ctx.getUserPrincipal().getName());
        if (!userLogin.isPresent()){
            throw new AccountResourceException("Current user login not found");
        }
        var user = User.findOneByLogin(userLogin.get());
        if (!user.isPresent()) {
            throw new AccountResourceException("User could not be found");
        }
            else if (user.get().authorities.size() >= 1 && user.get().authorities.stream().anyMatch(e1-> e1.equals(new Authority("ROLE_ADMIN")))){
                result = questionService.findAll(page);

            } else {
                return Response.status(403, "Current user cannot access to this ressource").build();
            }
        }

        }
        var response = Response.ok().entity(result.content);
        response = PaginationUtil.withPaginationInfo(response, uriInfo, result);
        return response.build();
    }

    /**
     * {@code GET  /questions/:id} : get the "id" question.
     *
     * @param id the id of the questionDTO to retrieve.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the
     *         questionDTO, or with status {@code 404 (Not Found)}.
     */
    @Operation(
        summary = "Get a question",
        description = "Returns the question with the given id."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The question",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = QuestionDTO.class))
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "404", description = "Question not found")
        }
    )
    @GET
    @Path("/{id}")
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response getQuestion(
        @Parameter(description = "Id of the question to retrieve", required = true) @PathParam("id") Long id,
        @Context SecurityContext ctx
    ) {
        log.debug("REST request to get Question : {}", id);
        if (!securityService.canAccess(ctx, id, Question.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        };
        Optional<QuestionDTO> questionDTO = questionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(questionDTO);
    }
}
