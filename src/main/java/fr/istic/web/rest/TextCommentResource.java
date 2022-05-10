package fr.istic.web.rest;

import static javax.ws.rs.core.UriBuilder.fromPath;

import fr.istic.service.TextCommentService;
import fr.istic.web.rest.errors.BadRequestAlertException;
import fr.istic.web.util.HeaderUtil;
import fr.istic.web.util.ResponseUtil;
import fr.istic.service.dto.TextCommentDTO;

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
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link fr.istic.domain.TextComment}.
 */
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
    TextCommentService textCommentService;
    /**
     * {@code POST  /text-comments} : Create a new textComment.
     *
     * @param textCommentDTO the textCommentDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body the new textCommentDTO, or with status {@code 400 (Bad Request)} if the textComment has already an ID.
     */
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
    @PUT
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response updateTextComment(TextCommentDTO textCommentDTO) {
        log.debug("REST request to update TextComment : {}", textCommentDTO);
        if (textCommentDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
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
    @DELETE
    @Path("/{id}")
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response deleteTextComment(@PathParam("id") Long id) {
        log.debug("REST request to delete TextComment : {}", id);
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
    @GET
    public Response getAllTextComments(@BeanParam PageRequestVM pageRequest, @BeanParam SortRequestVM sortRequest, @Context UriInfo uriInfo) {
        log.debug("REST request to get a page of TextComments");
        var page = pageRequest.toPage();
        var sort = sortRequest.toSort();
        MultivaluedMap param = uriInfo.getQueryParameters();
        Paged<TextCommentDTO> result = null;
        if (param.containsKey("questionId") ) {
            List questionId = (List) param.get("questionId");
            result = textCommentService.findTextCommentByQuestionId(page, Long.parseLong("" + questionId.get(0)));
        }



        else{
            result = textCommentService.findAll(page);

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
    @GET
    @Path("/{id}")

    public Response getTextComment(@PathParam("id") Long id) {
        log.debug("REST request to get TextComment : {}", id);
        Optional<TextCommentDTO> textCommentDTO = textCommentService.findOne(id);
        return ResponseUtil.wrapOrNotFound(textCommentDTO);
    }
}
