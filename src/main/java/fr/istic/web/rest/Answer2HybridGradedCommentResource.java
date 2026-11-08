package fr.istic.web.rest;

import static jakarta.ws.rs.core.UriBuilder.fromPath;

import fr.istic.domain.Answer2HybridGradedComment;
import fr.istic.domain.Authority;
import fr.istic.domain.HybridGradedComment;
import fr.istic.domain.Question;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.User;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.Answer2HybridGradedCommentService;
import fr.istic.service.Paged;
import fr.istic.service.SecurityService;
import fr.istic.service.dto.Answer2HybridGradedCommentDTO;
import fr.istic.service.dto.HybridGradedCommentDTO;
import fr.istic.web.rest.errors.AccountResourceException;
import fr.istic.web.rest.errors.BadRequestAlertException;
import fr.istic.web.rest.vm.PageRequestVM;
import fr.istic.web.rest.vm.SortRequestVM;
import fr.istic.web.util.HeaderUtil;
import fr.istic.web.util.PaginationUtil;
import fr.istic.web.util.ResponseUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for managing {@link fr.istic.domain.Answer2HybridGradedComment}.
 */
@Tag(
    name = "Answer2HybridGradedComment",
    description = "Management of answer to hybrid graded comment links: create, update, delete, and search them."
)
@Path("/api/answer-2-hybrid-graded-comments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class Answer2HybridGradedCommentResource {

    private final Logger log = LoggerFactory.getLogger(Answer2HybridGradedCommentResource.class);

    private static final String ENTITY_NAME = "answer2HybridGradedComment";

    @ConfigProperty(name = "application.name")
    String applicationName;

    @Inject
    Answer2HybridGradedCommentService answer2HybridGradedCommentService;

    @Inject
    SecurityService securityService;

    /**
     * {@code POST  /answer-2-hybrid-graded-comments} : Create a new answer2HybridGradedComment.
     *
     * @param answer2HybridGradedCommentDTO the answer2HybridGradedCommentDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body the new answer2HybridGradedCommentDTO, or with status {@code 400 (Bad Request)} if the answer2HybridGradedComment has already an ID.
     */
    @Operation(
        summary = "Create an answer to hybrid graded comment",
        description = "Creates a new answer to hybrid graded comment link and returns it."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "201",
                description = "The created answer to hybrid graded comment",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Answer2HybridGradedCommentDTO.class))
            ),
            @APIResponse(responseCode = "400", description = "The new answer to hybrid graded comment cannot already have an ID")
        }
    )
    @POST
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response createAnswer2HybridGradedComment(
        Answer2HybridGradedCommentDTO answer2HybridGradedCommentDTO,
        @Context UriInfo uriInfo
    ) {
        log.debug("REST request to save Answer2HybridGradedComment : {}", answer2HybridGradedCommentDTO);
        if (answer2HybridGradedCommentDTO.id != null) {
            throw new BadRequestAlertException("A new answer2HybridGradedComment cannot already have an ID", ENTITY_NAME, "idexists");
        }
        var result = answer2HybridGradedCommentService.persistOrUpdate(answer2HybridGradedCommentDTO);
        var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build()).entity(result);
        HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code PUT  /answer-2-hybrid-graded-comments} : Updates an existing answer2HybridGradedComment.
     *
     * @param answer2HybridGradedCommentDTO the answer2HybridGradedCommentDTO to update.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the updated answer2HybridGradedCommentDTO,
     * or with status {@code 400 (Bad Request)} if the answer2HybridGradedCommentDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the answer2HybridGradedCommentDTO couldn't be updated.
     */
    @Operation(
        summary = "Update an answer to hybrid graded comment",
        description = "Updates an existing answer to hybrid graded comment link and returns it."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The updated answer to hybrid graded comment",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Answer2HybridGradedCommentDTO.class))
            ),
            @APIResponse(responseCode = "400", description = "Answer to hybrid graded comment with an invalid (null) ID"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "500", description = "The answer to hybrid graded comment couldn't be updated")
        }
    )
    @PUT
    @Path("/{id}")
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response updateAnswer2HybridGradedComment(
        Answer2HybridGradedCommentDTO answer2HybridGradedCommentDTO,
        @Parameter(description = "Id of the answer to hybrid graded comment to update", required = true) @PathParam("id") Long id,
        @Context SecurityContext ctx
    ) {
        log.debug("REST request to update Answer2HybridGradedComment : {}", answer2HybridGradedCommentDTO);
        if (answer2HybridGradedCommentDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!securityService.canAccess(ctx, answer2HybridGradedCommentDTO.id, Answer2HybridGradedComment.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        var result = answer2HybridGradedCommentService.persistOrUpdate(answer2HybridGradedCommentDTO);
        var response = Response.ok().entity(result);
        HeaderUtil
            .createEntityUpdateAlert(applicationName, true, ENTITY_NAME, answer2HybridGradedCommentDTO.id.toString())
            .forEach(response::header);
        return response.build();
    }

    /**
     * {@code DELETE  /answer-2-hybrid-graded-comments/:id} : delete the "id" answer2HybridGradedComment.
     *
     * @param id the id of the answer2HybridGradedCommentDTO to delete.
     * @return the {@link Response} with status {@code 204 (NO_CONTENT)}.
     */
    @Operation(
        summary = "Delete an answer to hybrid graded comment",
        description = "Deletes the answer to hybrid graded comment link with the given id."
    )
    @APIResponses(
        value = {
            @APIResponse(responseCode = "204", description = "Answer to hybrid graded comment deleted"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @DELETE
    @Path("/{id}")
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})

    public Response deleteAnswer2HybridGradedComment(
        @Parameter(description = "Id of the answer to hybrid graded comment to delete", required = true) @PathParam("id") Long id,
        @Context SecurityContext ctx
    ) {
        log.debug("REST request to delete Answer2HybridGradedComment : {}", id);
        if (!securityService.canAccess(ctx, id, Answer2HybridGradedComment.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        answer2HybridGradedCommentService.delete(id);
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code GET  /answer-2-hybrid-graded-comments} : get all the answer2HybridGradedComments.
     *
     * @param pageRequest the pagination information.
     * @return the {@link Response} with status {@code 200 (OK)} and the list of answer2HybridGradedComments in body.
     */
    @Operation(
        summary = "Get all answers to hybrid graded comments",
        description = "Returns a paged list of answers to hybrid graded comments. Optionally filter by answerId."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The list of answers to hybrid graded comments",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = Answer2HybridGradedCommentDTO.class))
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "500", description = "Current user not found")
        }
    )
    @GET
    public Response getAllAnswer2HybridGradedComments(
        @BeanParam PageRequestVM pageRequest,
        @BeanParam SortRequestVM sortRequest,
        @Context UriInfo uriInfo
        ,@Context SecurityContext ctx
    ) {
        log.debug("REST request to get a page of Answer2HybridGradedComments");

        //answerId
        var page = pageRequest.toPage();
        var sort = sortRequest.toSort();

        Paged<Answer2HybridGradedCommentDTO> result = new Paged<>(0,0,0,0,new ArrayList<>());
        MultivaluedMap param = uriInfo.getQueryParameters();

         if (param.containsKey("answerId") ) {
            List answerId = (List) param.get("answerId");
/*            if (!securityService.canAccess(ctx, Long.parseLong("" + answerId.get(0)), StudentResponse.class  )){
                return Response.status(403, "Current user cannot access to this ressource").build();
            } */
            result = answer2HybridGradedCommentService.findAllAnswerHybridGradedCommentByAnswerId(page, Long.parseLong("" + answerId.get(0)));
        }
        else{
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
                result = answer2HybridGradedCommentService.findAll(page);

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
     * {@code GET  /answer-2-hybrid-graded-comments/:id} : get the "id" answer2HybridGradedComment.
     *
     * @param id the id of the answer2HybridGradedCommentDTO to retrieve.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the answer2HybridGradedCommentDTO, or with status {@code 404 (Not Found)}.
     */
    @Operation(
        summary = "Get an answer to hybrid graded comment",
        description = "Returns the answer to hybrid graded comment link with the given id."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The answer to hybrid graded comment",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Answer2HybridGradedCommentDTO.class))
            ),
            @APIResponse(responseCode = "404", description = "Answer to hybrid graded comment not found")
        }
    )
    @GET
    @Path("/{id}")
    public Response getAnswer2HybridGradedComment(
        @Parameter(description = "Id of the answer to hybrid graded comment to retrieve", required = true) @PathParam("iAnswer2HybridGradedCommentd") Long id
    ) {
        log.debug("REST request to get Answer2HybridGradedComment : {}", id);
        Optional<Answer2HybridGradedCommentDTO> answer2HybridGradedCommentDTO = answer2HybridGradedCommentService.findOne(id);
        return ResponseUtil.wrapOrNotFound(answer2HybridGradedCommentDTO);
    }
}
