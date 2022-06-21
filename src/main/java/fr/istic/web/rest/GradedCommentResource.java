package fr.istic.web.rest;

import static javax.ws.rs.core.UriBuilder.fromPath;

import fr.istic.domain.Authority;
import fr.istic.domain.GradedComment;
import fr.istic.domain.Question;
import fr.istic.domain.User;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.GradedCommentService;
import fr.istic.web.rest.errors.AccountResourceException;
import fr.istic.web.rest.errors.BadRequestAlertException;
import fr.istic.web.util.HeaderUtil;
import fr.istic.web.util.ResponseUtil;
import fr.istic.service.dto.GradedCommentDTO;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.istic.service.Paged;
import fr.istic.service.SecurityService;
import fr.istic.web.rest.vm.PageRequestVM;
import fr.istic.web.rest.vm.SortRequestVM;
import fr.istic.web.util.PaginationUtil;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link fr.istic.domain.GradedComment}.
 */
@Path("/api/graded-comments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class GradedCommentResource {

    private final Logger log = LoggerFactory.getLogger(GradedCommentResource.class);

    private static final String ENTITY_NAME = "gradedComment";

    @ConfigProperty(name = "application.name")
    String applicationName;


    @Inject
    GradedCommentService gradedCommentService;

    @Inject
    SecurityService securityService;

    /**
     * {@code POST  /graded-comments} : Create a new gradedComment.
     *
     * @param gradedCommentDTO the gradedCommentDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body the new gradedCommentDTO, or with status {@code 400 (Bad Request)} if the gradedComment has already an ID.
     */
    @POST
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response createGradedComment(GradedCommentDTO gradedCommentDTO, @Context UriInfo uriInfo) {
        log.debug("REST request to save GradedComment : {}", gradedCommentDTO);
        if (gradedCommentDTO.id != null) {
            throw new BadRequestAlertException("A new gradedComment cannot already have an ID", ENTITY_NAME, "idexists");
        }
        var result = gradedCommentService.persistOrUpdate(gradedCommentDTO);
        var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build()).entity(result);
        HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code PUT  /graded-comments} : Updates an existing gradedComment.
     *
     * @param gradedCommentDTO the gradedCommentDTO to update.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the updated gradedCommentDTO,
     * or with status {@code 400 (Bad Request)} if the gradedCommentDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the gradedCommentDTO couldn't be updated.
     */
    @PUT
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response updateGradedComment(GradedCommentDTO gradedCommentDTO, @Context SecurityContext ctx) {
        log.debug("REST request to update GradedComment : {}", gradedCommentDTO);
        if (gradedCommentDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!securityService.canAccess(ctx, gradedCommentDTO.id, GradedComment.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        var result = gradedCommentService.persistOrUpdate(gradedCommentDTO);
        var response = Response.ok().entity(result);
        HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, gradedCommentDTO.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code DELETE  /graded-comments/:id} : delete the "id" gradedComment.
     *
     * @param id the id of the gradedCommentDTO to delete.
     * @return the {@link Response} with status {@code 204 (NO_CONTENT)}.
     */
    @DELETE
    @Path("/{id}")
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response deleteGradedComment(@PathParam("id") Long id, @Context SecurityContext ctx) {
        log.debug("REST request to delete GradedComment : {}", id);
        if (!securityService.canAccess(ctx, id, GradedComment.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        gradedCommentService.delete(id);
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code GET  /graded-comments} : get all the gradedComments.
     *
     * @param pageRequest the pagination information.
     * @return the {@link Response} with status {@code 200 (OK)} and the list of gradedComments in body.
     */
    @GET
    public Response getAllGradedComments(@BeanParam PageRequestVM pageRequest, @BeanParam SortRequestVM sortRequest, @Context UriInfo uriInfo, @Context SecurityContext ctx) {
        log.debug("REST request to get a page of GradedComments");
        var page = pageRequest.toPage();
        var sort = sortRequest.toSort();
        Paged<GradedCommentDTO> result = null;
        MultivaluedMap param = uriInfo.getQueryParameters();
        if (param.containsKey("questionId") ) {
            List questionId = (List) param.get("questionId");
            if (!securityService.canAccess(ctx, Long.parseLong("" + questionId.get(0)), Question.class  )){
                return Response.status(403, "Current user cannot access to this ressource").build();
            }
            result = gradedCommentService.findGradedCommentByQuestionId(page, Long.parseLong("" + questionId.get(0)));
        }
        else{

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
                result = gradedCommentService.findAll(page);

            } else {
                return Response.status(403, "Current user cannot access to this ressource").build();
            }


        }
        var response = Response.ok().entity(result.content);
        response = PaginationUtil.withPaginationInfo(response, uriInfo, result);
        return response.build();
    }


    /**
     * {@code GET  /graded-comments/:id} : get the "id" gradedComment.
     *
     * @param id the id of the gradedCommentDTO to retrieve.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the gradedCommentDTO, or with status {@code 404 (Not Found)}.
     */
    @GET
    @Path("/{id}")

    public Response getGradedComment(@PathParam("id") Long id, @Context SecurityContext ctx) {
        log.debug("REST request to get GradedComment : {}", id);
        if (!securityService.canAccess(ctx, id, GradedComment.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        Optional<GradedCommentDTO> gradedCommentDTO = gradedCommentService.findOne(id);
        return ResponseUtil.wrapOrNotFound(gradedCommentDTO);
    }
}
