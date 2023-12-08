package fr.istic.web.rest;

import static javax.ws.rs.core.UriBuilder.fromPath;

import fr.istic.domain.Authority;
import fr.istic.domain.GradedComment;
import fr.istic.domain.HybridGradedComment;
import fr.istic.domain.Question;
import fr.istic.domain.User;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.HybridGradedCommentService;
import fr.istic.service.Paged;
import fr.istic.service.SecurityService;
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

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for managing {@link fr.istic.domain.HybridGradedComment}.
 */
@Path("/api/hybrid-graded-comments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class HybridGradedCommentResource {

    private final Logger log = LoggerFactory.getLogger(HybridGradedCommentResource.class);

    private static final String ENTITY_NAME = "hybridGradedComment";

    @ConfigProperty(name = "application.name")
    String applicationName;

    @Inject
    HybridGradedCommentService hybridGradedCommentService;

    @Inject
    SecurityService securityService;

    /**
     * {@code POST  /hybrid-graded-comments} : Create a new hybridGradedComment.
     *
     * @param hybridGradedCommentDTO the hybridGradedCommentDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body the new hybridGradedCommentDTO, or with status {@code 400 (Bad Request)} if the hybridGradedComment has already an ID.
     */
    @POST
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})

    public Response createHybridGradedComment(HybridGradedCommentDTO hybridGradedCommentDTO, @Context UriInfo uriInfo) {
        log.debug("REST request to save HybridGradedComment : {}", hybridGradedCommentDTO);
        if (hybridGradedCommentDTO.id != null) {
            throw new BadRequestAlertException("A new hybridGradedComment cannot already have an ID", ENTITY_NAME, "idexists");
        }
        var result = hybridGradedCommentService.persistOrUpdate(hybridGradedCommentDTO);
        var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build()).entity(result);
        HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code PUT  /hybrid-graded-comments} : Updates an existing hybridGradedComment.
     *
     * @param hybridGradedCommentDTO the hybridGradedCommentDTO to update.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the updated hybridGradedCommentDTO,
     * or with status {@code 400 (Bad Request)} if the hybridGradedCommentDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the hybridGradedCommentDTO couldn't be updated.
     */
    @PUT
    @Path("/{id}")
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response updateHybridGradedComment(HybridGradedCommentDTO hybridGradedCommentDTO, @PathParam("id") Long id, @Context SecurityContext ctx) {
        log.debug("REST request to update HybridGradedComment : {}", hybridGradedCommentDTO);
        if (hybridGradedCommentDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!securityService.canAccess(ctx, hybridGradedCommentDTO.id, HybridGradedComment.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        var result = hybridGradedCommentService.persistOrUpdate(hybridGradedCommentDTO);
        var response = Response.ok().entity(result);
        HeaderUtil
            .createEntityUpdateAlert(applicationName, true, ENTITY_NAME, hybridGradedCommentDTO.id.toString())
            .forEach(response::header);
        return response.build();
    }

    /**
     * {@code DELETE  /hybrid-graded-comments/:id} : delete the "id" hybridGradedComment.
     *
     * @param id the id of the hybridGradedCommentDTO to delete.
     * @return the {@link Response} with status {@code 204 (NO_CONTENT)}.
     */
    @DELETE
    @Path("/{id}")
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response deleteHybridGradedComment(@PathParam("id") Long id, @Context SecurityContext ctx) {
        log.debug("REST request to delete HybridGradedComment : {}", id);
        if (!securityService.canAccess(ctx, id, HybridGradedComment.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        hybridGradedCommentService.delete(id);
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code GET  /hybrid-graded-comments} : get all the hybridGradedComments.
     *
     * @param pageRequest the pagination information.
     * @return the {@link Response} with status {@code 200 (OK)} and the list of hybridGradedComments in body.
     */
    @GET
    public Response getAllHybridGradedComments(
        @BeanParam PageRequestVM pageRequest,
        @BeanParam SortRequestVM sortRequest,
        @Context UriInfo uriInfo, @Context SecurityContext ctx

    ) {
        log.debug("REST request to get a page of HybridGradedComments");
        var page = pageRequest.toPage();
        var sort = sortRequest.toSort();
        Paged<HybridGradedCommentDTO> result = new Paged<>(0,0,0,0,new ArrayList<>());
        MultivaluedMap param = uriInfo.getQueryParameters();

         if (param.containsKey("questionId") ) {
            List questionId = (List) param.get("questionId");
            if (!securityService.canAccess(ctx, Long.parseLong("" + questionId.get(0)), Question.class  )){
                return Response.status(403, "Current user cannot access to this ressource").build();
            }
            result = hybridGradedCommentService.findHybridGradedCommentByQuestionId(page, Long.parseLong("" + questionId.get(0)));
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
                result = hybridGradedCommentService.findAll(page);

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
     * {@code GET  /hybrid-graded-comments/:id} : get the "id" hybridGradedComment.
     *
     * @param id the id of the hybridGradedCommentDTO to retrieve.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the hybridGradedCommentDTO, or with status {@code 404 (Not Found)}.
     */
    @GET
    @Path("/{id}")
    public Response getHybridGradedComment(@PathParam("id") Long id) {
        log.debug("REST request to get HybridGradedComment : {}", id);
        Optional<HybridGradedCommentDTO> hybridGradedCommentDTO = hybridGradedCommentService.findOne(id);
        return ResponseUtil.wrapOrNotFound(hybridGradedCommentDTO);
    }
}
