package fr.istic.web.rest;

import static jakarta.ws.rs.core.UriBuilder.fromPath;

import fr.istic.service.TextCommentService;
import fr.istic.web.rest.errors.AccountResourceException;
import fr.istic.web.rest.errors.BadRequestAlertException;
import fr.istic.web.util.HeaderUtil;
import fr.istic.web.util.ResponseUtil;
import fr.istic.service.dto.TextCommentDTO;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.domain.Authority;
import fr.istic.domain.GradedComment;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.TextComment;
import fr.istic.domain.User;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.Paged;
import fr.istic.service.SecurityService;
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
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link fr.istic.domain.TextComment}.
 */
@Tag(
    name = "TextComment",
    description = "Management of text comments: create, update, delete, and search text comments."
)
@Path("/api/text-comments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class TextCommentResource {

    private final Logger log = LoggerFactory.getLogger(TextCommentResource.class);

    private static final String ENTITY_NAME = "textComment";

    @ConfigProperty(name = "application.name")
    String applicationName;


    @Inject
    SecurityService securityService;

    @Inject
    TextCommentService textCommentService;
    /**
     * {@code POST  /text-comments} : Create a new textComment.
     *
     * @param textCommentDTO the textCommentDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body the new textCommentDTO, or with status {@code 400 (Bad Request)} if the textComment has already an ID.
     */
    @Operation(
        summary = "Create a text comment",
        description = "Creates a new text comment and returns the created text comment."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "201",
                description = "The created text comment",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TextCommentDTO.class))
            ),
            @APIResponse(responseCode = "400", description = "The new text comment cannot already have an ID")
        }
    )
    @POST
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response createTextComment(TextCommentDTO textCommentDTO, @Context UriInfo uriInfo) {
        log.debug("REST request to save TextComment : {}", textCommentDTO);
        if (textCommentDTO.id != null) {
            throw new BadRequestAlertException("A new textComment cannot already have an ID", ENTITY_NAME, "idexists");
        }
        var result = textCommentService.persistOrUpdate(textCommentDTO);
        var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build()).entity(result);
        HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code PUT  /text-comments} : Updates an existing textComment.
     *
     * @param textCommentDTO the textCommentDTO to update.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the updated textCommentDTO,
     * or with status {@code 400 (Bad Request)} if the textCommentDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the textCommentDTO couldn't be updated.
     */
    @Operation(
        summary = "Update a text comment",
        description = "Updates an existing text comment and returns the updated text comment."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The updated text comment",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TextCommentDTO.class))
            ),
            @APIResponse(responseCode = "400", description = "Text comment with an invalid (null) ID"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "500", description = "The text comment couldn't be updated")
        }
    )
    @PUT
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response updateTextComment(TextCommentDTO textCommentDTO, @Context SecurityContext ctx) {
        log.debug("REST request to update TextComment : {}", textCommentDTO);
        if (textCommentDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!securityService.canAccess(ctx, textCommentDTO.id, TextComment.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        var result = textCommentService.persistOrUpdate(textCommentDTO);
        var response = Response.ok().entity(result);
        HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, textCommentDTO.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code DELETE  /text-comments/:id} : delete the "id" textComment.
     *
     * @param id the id of the textCommentDTO to delete.
     * @return the {@link Response} with status {@code 204 (NO_CONTENT)}.
     */
    @Operation(
        summary = "Delete a text comment",
        description = "Deletes the text comment with the given id."
    )
    @APIResponses(
        value = {
            @APIResponse(responseCode = "204", description = "Text comment deleted"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @DELETE
    @Path("/{id}")
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response deleteTextComment(
        @Parameter(description = "Id of the text comment to delete", required = true) @PathParam("id") Long id,
        @Context SecurityContext ctx
    ) {
        log.debug("REST request to delete TextComment : {}", id);
        if (!securityService.canAccess(ctx, id, TextComment.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        textCommentService.delete(id);
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code GET  /text-comments} : get all the textComments.
     *
     * @param pageRequest the pagination information.
     * @return the {@link Response} with status {@code 200 (OK)} and the list of textComments in body.
     */
    @Operation(
        summary = "Get all text comments",
        description = "Returns a paged list of text comments. Optionally filter by questionId."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The list of text comments",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = TextCommentDTO.class))
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "500", description = "Current user not found")
        }
    )
    @GET
    public Response getAllTextComments(@BeanParam PageRequestVM pageRequest, @BeanParam SortRequestVM sortRequest, @Context UriInfo uriInfo, @Context SecurityContext ctx) {
        log.debug("REST request to get a page of TextComments");
        var page = pageRequest.toPage();
        var sort = sortRequest.toSort();
        MultivaluedMap param = uriInfo.getQueryParameters();
        Paged<TextCommentDTO> result = new Paged(0,0,0,0, new ArrayList<>());
        if (param.containsKey("questionId") ) {
            List questionId = (List) param.get("questionId");
            result = textCommentService.findTextCommentByQuestionId(page, Long.parseLong("" + questionId.get(0)));
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
                result = textCommentService.findAll(page);

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
     * {@code GET  /text-comments/:id} : get the "id" textComment.
     *
     * @param id the id of the textCommentDTO to retrieve.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the textCommentDTO, or with status {@code 404 (Not Found)}.
     */
    @Operation(
        summary = "Get a text comment",
        description = "Returns the text comment with the given id."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The text comment",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TextCommentDTO.class))
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "404", description = "Text comment not found")
        }
    )
    @GET
    @Path("/{id}")
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response getTextComment(
        @Parameter(description = "Id of the text comment to retrieve", required = true) @PathParam("id") Long id,
        @Context SecurityContext ctx
    ) {
        log.debug("REST request to get TextComment : {}", id);
        if (!securityService.canAccess(ctx, id, TextComment.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        Optional<TextCommentDTO> textCommentDTO = textCommentService.findOne(id);
        return ResponseUtil.wrapOrNotFound(textCommentDTO);
    }


    @Operation(
        summary = "Count usages of a text comment",
        description = "Returns the number of student responses using the text comment with the given id."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The number of usages",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.INTEGER, format = "int64"))
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @GET
    @Path("/countHowManyUse/{id}")
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response getUsedOfGradedComment(
        @Parameter(description = "Id of the text comment to count usages of", required = true) @PathParam("id") Long id,
        @Context SecurityContext ctx
    ) {
        log.debug("REST request to get count textcommentUse : {}", id);
        if (!securityService.canAccess(ctx, id, TextComment.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        long l = StudentResponse.findAllByTextCommentsIds(id).count();
        return Response.ok(Long.valueOf(l)).build();
    }

}
