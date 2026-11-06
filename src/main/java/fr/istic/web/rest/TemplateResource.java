package fr.istic.web.rest;

import static jakarta.ws.rs.core.UriBuilder.fromPath;

import fr.istic.service.TemplateService;
import fr.istic.service.customdto.StudentResponseNote;
import fr.istic.service.customdto.TemplateCaseDTO;
import fr.istic.web.rest.errors.BadRequestAlertException;
import fr.istic.web.util.HeaderUtil;
import fr.istic.web.util.ResponseUtil;
import fr.istic.service.dto.StudentResponseDTO;
import fr.istic.service.dto.TemplateDTO;
import fr.istic.service.dto.TemplateDTOContent;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.domain.StudentResponse;
import fr.istic.domain.Template;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.Paged;
import fr.istic.service.SecurityService;
import fr.istic.web.rest.vm.PageRequestVM;
import fr.istic.web.rest.vm.SortRequestVM;
import fr.istic.web.util.PaginationUtil;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.Optional;

/**
 * REST controller for managing {@link fr.istic.domain.Template}.
 */
@Tag(
    name = "Template",
    description = "Management of correction templates: create, update, delete, list, retrieve and partially update templates."
)
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

    @Inject
    SecurityService securityService;

    /**
     * {@code POST  /templates} : Create a new template.
     *
     * @param templateDTO the templateDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body the new templateDTO, or with status {@code 400 (Bad Request)} if the template has already an ID.
     */
    @Operation(summary = "Create a new template", description = "Creates a new correction template and returns the created template.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "201",
                description = "The created template",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TemplateDTO.class))
            ),
            @APIResponse(responseCode = "400", description = "A new template cannot already have an ID")
        }
    )
    @POST
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response createTemplate(@Valid TemplateDTOContent templateDTO, @Context UriInfo uriInfo) {
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
    @Operation(summary = "Update a template", description = "Updates an existing correction template and returns the updated template.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The updated template",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TemplateDTO.class))
            ),
            @APIResponse(responseCode = "400", description = "Invalid template id or validation error"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "500", description = "The template could not be updated")
        }
    )
    @PUT
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response updateTemplate(@Valid TemplateDTOContent templateDTO, @Context SecurityContext ctx) {
        log.debug("REST request to update Template : {}", templateDTO);
        if (templateDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!securityService.canAccess(ctx, templateDTO.id, Template.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
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
    @Operation(summary = "Delete a template", description = "Deletes the template with the given id.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "204", description = "The template has been deleted"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @DELETE
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    @Path("/{id}")
    public Response deleteTemplate(@Parameter(description = "Id of the template to delete", required = true) @PathParam("id") Long id, @Context SecurityContext ctx) {
        log.debug("REST request to delete Template : {}", id);
        if (!securityService.canAccess(ctx, id, Template.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
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
    @Operation(summary = "Get all templates", description = "Returns a page of correction templates.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The page of templates",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = TemplateDTO.class))
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @GET
    @RolesAllowed({AuthoritiesConstants.ADMIN})
    public Response getAllTemplates(@BeanParam PageRequestVM pageRequest, @BeanParam SortRequestVM sortRequest, @Context UriInfo uriInfo, @Context SecurityContext ctx) {
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
    @Operation(summary = "Get the template by id", description = "Gets the template with the given id.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The requested template",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TemplateDTO.class))
            ),
            @APIResponse(responseCode = "404", description = "No template found for this id")
        }
    )
    @GET
    @Path("/{id}")
    public Response getTemplate(@Parameter(description = "Id of the template to retrieve", required = true) @PathParam("id") Long id, @Context SecurityContext ctx) {
        log.debug("REST request to get Template : {}", id);
        Optional<TemplateDTO> templateDTO = templateService.findOne(id);
        return ResponseUtil.wrapOrNotFound(templateDTO);
    }

    @Operation(
        summary = "Partially update a template",
        description = "Partially updates the given fields of the template with the given id."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The updated template",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TemplateDTO.class))
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "404", description = "No template found for this id")
        }
    )
    @PATCH
    @Path(value = "/{id}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response partialUpdateSR(
            @Parameter(description = "Id of the template to update", required = true) @PathParam(value = "id") final Long id,
            TemplateCaseDTO tDTO, @Context SecurityContext ctx) {
        log.debug("REST request to partial update Template partially : {}, {}", id, tDTO);

        if (!securityService.canAccess(ctx, id, Template.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        Optional<TemplateDTO> result = templateService.partialUpdate(tDTO, id);
        return ResponseUtil.wrapOrNotFound(
                result,
                HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, id.toString()));
    }
}
