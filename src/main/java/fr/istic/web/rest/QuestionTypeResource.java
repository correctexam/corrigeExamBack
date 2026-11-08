package fr.istic.web.rest;

import static jakarta.ws.rs.core.UriBuilder.fromPath;

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
 * REST controller for managing {@link fr.istic.domain.QuestionType}.
 */
@Tag(
    name = "QuestionType",
    description = "Management of question types: create, update, delete, and search question types."
)
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
    @Operation(
        summary = "Create a question type",
        description = "Creates a new question type and returns the created question type."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "201",
                description = "The created question type",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = QuestionTypeDTO.class))
            ),
            @APIResponse(responseCode = "400", description = "The new question type cannot already have an ID")
        }
    )
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
    @Operation(
        summary = "Update a question type",
        description = "Updates an existing question type and returns the updated question type."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The updated question type",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = QuestionTypeDTO.class))
            ),
            @APIResponse(responseCode = "400", description = "Question type with an invalid (null) ID"),
            @APIResponse(responseCode = "500", description = "The question type couldn't be updated")
        }
    )
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
    @Operation(
        summary = "Delete a question type",
        description = "Deletes the question type with the given id."
    )
    @APIResponses(
        value = {
            @APIResponse(responseCode = "204", description = "Question type deleted")
        }
    )
    @DELETE
    @Path("/{id}")
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response deleteQuestionType(
        @Parameter(description = "Id of the question type to delete", required = true) @PathParam("id") Long id
    ) {
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
    @Operation(
        summary = "Get all question types",
        description = "Returns a paged list of all question types."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The list of question types",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = QuestionTypeDTO.class))
            )
        }
    )
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
    @Operation(
        summary = "Get a question type",
        description = "Returns the question type with the given id."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The question type",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = QuestionTypeDTO.class))
            ),
            @APIResponse(responseCode = "404", description = "Question type not found")
        }
    )
    @GET
    @Path("/{id}")

    public Response getQuestionType(
        @Parameter(description = "Id of the question type to retrieve", required = true) @PathParam("id") Long id
    ) {
        log.debug("REST request to get QuestionType : {}", id);
        Optional<QuestionTypeDTO> questionTypeDTO = questionTypeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(questionTypeDTO);
    }
}
