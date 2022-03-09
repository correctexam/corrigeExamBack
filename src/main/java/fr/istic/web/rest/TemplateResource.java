package fr.istic.web.rest;

import static javax.ws.rs.core.UriBuilder.fromPath;

import fr.istic.service.TemplateService;
import fr.istic.web.rest.errors.BadRequestAlertException;
import fr.istic.web.util.HeaderUtil;
import fr.istic.web.util.ResponseUtil;
import fr.istic.service.dto.TemplateDTO;

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
import java.util.stream.StreamSupport;

/**
 * REST controller for managing {@link fr.istic.domain.Template}.
 */
@Path("/api/templates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class TemplateResource {

    private final Logger log = LoggerFactory.getLogger(TemplateResource.class);

    private static final String ENTITY_NAME = "template";

    @ConfigProperty(name = "application.name")
    String applicationName;


    @Inject
    TemplateService templateService;
    /**
     * {@code POST  /templates} : Create a new template.
     *
     * @param templateDTO the templateDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body the new templateDTO, or with status {@code 400 (Bad Request)} if the template has already an ID.
     */
    @POST
    public Response createTemplate(@Valid TemplateDTO templateDTO, @Context UriInfo uriInfo) {
        log.debug("REST request to save Template : {}", templateDTO);
        if (templateDTO.id != null) {
            throw new BadRequestAlertException("A new template cannot already have an ID", ENTITY_NAME, "idexists");
        }
        var result = templateService.persistOrUpdate(templateDTO);
        var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build()).entity(result);
        HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code PUT  /templates} : Updates an existing template.
     *
     * @param templateDTO the templateDTO to update.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the updated templateDTO,
     * or with status {@code 400 (Bad Request)} if the templateDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the templateDTO couldn't be updated.
     */
    @PUT
    public Response updateTemplate(@Valid TemplateDTO templateDTO) {
        log.debug("REST request to update Template : {}", templateDTO);
        if (templateDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        var result = templateService.persistOrUpdate(templateDTO);
        var response = Response.ok().entity(result);
        HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, templateDTO.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code DELETE  /templates/:id} : delete the "id" template.
     *
     * @param id the id of the templateDTO to delete.
     * @return the {@link Response} with status {@code 204 (NO_CONTENT)}.
     */
    @DELETE
    @Path("/{id}")
    public Response deleteTemplate(@PathParam("id") Long id) {
        log.debug("REST request to delete Template : {}", id);
        templateService.delete(id);
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code GET  /templates} : get all the templates.
     *
     * @param pageRequest the pagination information.
     * @return the {@link Response} with status {@code 200 (OK)} and the list of templates in body.
     */
    @GET
    public Response getAllTemplates(@BeanParam PageRequestVM pageRequest, @BeanParam SortRequestVM sortRequest, @Context UriInfo uriInfo) {
        log.debug("REST request to get a page of Templates");
        var page = pageRequest.toPage();
        var sort = sortRequest.toSort();
        Paged<TemplateDTO> result = templateService.findAll(page);
        var response = Response.ok().entity(result.content);
        response = PaginationUtil.withPaginationInfo(response, uriInfo, result);
        return response.build();
    }


    /**
     * {@code GET  /templates/:id} : get the "id" template.
     *
     * @param id the id of the templateDTO to retrieve.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the templateDTO, or with status {@code 404 (Not Found)}.
     */
    @GET
    @Path("/{id}")

    public Response getTemplate(@PathParam("id") Long id) {
        log.debug("REST request to get Template : {}", id);
        Optional<TemplateDTO> templateDTO = templateService.findOne(id);
        return ResponseUtil.wrapOrNotFound(templateDTO);
    }
}
