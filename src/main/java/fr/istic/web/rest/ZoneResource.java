package fr.istic.web.rest;

import static jakarta.ws.rs.core.UriBuilder.fromPath;

import fr.istic.service.ZoneService;
import fr.istic.service.customdto.ResizeZoneDTO;
import fr.istic.web.rest.errors.BadRequestAlertException;
import fr.istic.web.util.HeaderUtil;
import fr.istic.web.util.ResponseUtil;
import fr.istic.service.dto.ZoneDTO;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.domain.Exam;
import fr.istic.domain.Zone;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.Paged;
import fr.istic.service.SecurityService;
import fr.istic.web.rest.vm.PageRequestVM;
import fr.istic.web.rest.vm.SortRequestVM;
import fr.istic.web.util.PaginationUtil;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for managing {@link fr.istic.domain.Zone}.
 */
@Tag(
    name = "Zone",
    description = "Management of answer zones on scans: create, update, delete, list, retrieve and resize zones."
)
@Path("/api/zones")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ZoneResource {

    private final Logger log = LoggerFactory.getLogger(ZoneResource.class);

    private static final String ENTITY_NAME = "zone";

    @ConfigProperty(name = "application.name")
    String applicationName;

    @Inject
    SecurityService securityService;


    @Inject
    ZoneService zoneService;
    /**
     * {@code POST  /zones} : Create a new zone.
     *
     * @param zoneDTO the zoneDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body the new zoneDTO, or with status {@code 400 (Bad Request)} if the zone has already an ID.
     */
    @Operation(summary = "Create a new zone", description = "Creates a new answer zone and returns the created zone.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "201",
                description = "The created zone",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ZoneDTO.class))
            ),
            @APIResponse(responseCode = "400", description = "A new zone cannot already have an ID")
        }
    )
    @POST
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response createZone(ZoneDTO zoneDTO, @Context UriInfo uriInfo) {
        log.debug("REST request to save Zone : {}", zoneDTO);
        if (zoneDTO.id != null) {
            throw new BadRequestAlertException("A new zone cannot already have an ID", ENTITY_NAME, "idexists");
        }
        var result = zoneService.persistOrUpdate(zoneDTO);
        var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build()).entity(result);
        HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code PUT  /zones} : Updates an existing zone.
     *
     * @param zoneDTO the zoneDTO to update.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the updated zoneDTO,
     * or with status {@code 400 (Bad Request)} if the zoneDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the zoneDTO couldn't be updated.
     */
    @Operation(summary = "Update a zone", description = "Updates an existing answer zone and returns the updated zone.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The updated zone",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ZoneDTO.class))
            ),
            @APIResponse(responseCode = "400", description = "Invalid zone id or validation error"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "500", description = "The zone could not be updated")
        }
    )
    @PUT
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response updateZone(ZoneDTO zoneDTO, @Context SecurityContext ctx) {
        log.debug("REST request to update Zone : {}", zoneDTO);
        if (zoneDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!securityService.canAccess(ctx, zoneDTO.id, Zone.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        var result = zoneService.persistOrUpdate(zoneDTO);
        var response = Response.ok().entity(result);
        HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, zoneDTO.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code DELETE  /zones/:id} : delete the "id" zone.
     *
     * @param id the id of the zoneDTO to delete.
     * @return the {@link Response} with status {@code 204 (NO_CONTENT)}.
     */
    @Operation(summary = "Delete a zone", description = "Deletes the zone with the given id.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "204", description = "The zone has been deleted"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @DELETE
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    @Path("/{id}")
    public Response deleteZone(@Parameter(description = "Id of the zone to delete", required = true) @PathParam("id") Long id, @Context SecurityContext ctx) {
        log.debug("REST request to delete Zone : {}", id);
        if (!securityService.canAccess(ctx, id, Zone.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        zoneService.delete(id);
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code GET  /zones} : get all the zones.
     *
     * @param pageRequest the pagination information.
     * @return the {@link Response} with status {@code 200 (OK)} and the list of zones in body.
     */
    @Operation(summary = "Get all zones", description = "Returns a page of answer zones.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The page of zones",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = ZoneDTO.class))
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @GET
    @RolesAllowed({AuthoritiesConstants.ADMIN})

    public Response getAllZones(@BeanParam PageRequestVM pageRequest, @BeanParam SortRequestVM sortRequest, @Context UriInfo uriInfo, @Context SecurityContext ctx) {
        log.debug("REST request to get a page of Zones");
        var page = pageRequest.toPage();
        var sort = sortRequest.toSort();
        Paged<ZoneDTO> result = zoneService.findAll(page);
        var response = Response.ok().entity(result.content);
        response = PaginationUtil.withPaginationInfo(response, uriInfo, result);
        return response.build();
    }


    /**
     * {@code GET  /zones/:id} : get the "id" zone.
     *
     * @param id the id of the zoneDTO to retrieve.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the zoneDTO, or with status {@code 404 (Not Found)}.
     */
    @Operation(summary = "Get the zone by id", description = "Gets the zone with the given id.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The requested zone",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ZoneDTO.class))
            ),
            @APIResponse(responseCode = "404", description = "No zone found for this id")
        }
    )
    @GET
    @Path("/{id}")
    public Response getZone(@Parameter(description = "Id of the zone to retrieve", required = true) @PathParam("id") Long id, @Context SecurityContext ctx) {
        log.debug("REST request to get Zone : {}", id);
        // No scurity for voir copies
        Optional<ZoneDTO> zoneDTO = zoneService.findOne(id);
        return ResponseUtil.wrapOrNotFound(zoneDTO);
    }

        /**
     * {@code GET  /zones/:id} : get the "id" zone.
     *
     * @param id the id of the zoneDTO to retrieve.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the zoneDTO, or with status {@code 404 (Not Found)}.
     */
    @Operation(summary = "Get zones of an exam", description = "Gets all the answer zones of the exam with the given id.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The zones of the exam",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = ZoneDTO.class))
            )
        }
    )
    @GET
    @Path("/byExam/{examid}")
    public Response getZoneByExam(@Parameter(description = "Id of the exam", required = true) @PathParam("examid") Long examid, @Context SecurityContext ctx) {
        log.debug("REST request to get Zone : {}", examid);
        // No scurity for voir copies
        ZoneDTO[] zoneDTOs = zoneService.getZone4Exam(examid);
        var response = Response.ok().entity(zoneDTOs);
        return response.build();
    }

    @Operation(
        summary = "Count student responses of a zone",
        description = "Returns the number of student responses associated to the zone with the given id."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The number of student responses of the zone",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.INTEGER))
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @GET
    @Path("/studentAnswer/{id}")
    public Response getNumberOfStudentResponse4Zone(@Parameter(description = "Id of the zone", required = true) @PathParam("id") Long zoneid, @Context SecurityContext ctx) {
        log.debug("REST request to get studentAnswer for Zone : {}", zoneid);
        if (!securityService.canAccess(ctx, zoneid, Zone.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        long l = zoneService.getNumberOfStudentResponse4Zone(zoneid);
        var response = Response.ok().entity(l);
        return response.build();
    }

    @Operation(
        summary = "Count student responses of an exam",
        description = "Returns the number of student responses associated to the exam with the given id."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The number of student responses of the exam",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.INTEGER))
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @GET
    @Path("/studentAnswer4exam/{examid}")
    public Response getNumberOfStudentResponse4Exam(@Parameter(description = "Id of the exam", required = true) @PathParam("examid") Long examid, @Context SecurityContext ctx) {
        log.debug("REST request to get studentAnswer for Zone : {}", examid);
        if (!securityService.canAccess(ctx, examid, Exam.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        long l = zoneService.getNumberOfStudentResponse4Exam(examid);
        var response = Response.ok().entity(l);
        return response.build();
    }



    /**
     * {@code PATCH  /zones/:id} : Partial updates given fields of an existing zone, field will ignore if it is null
     *
     * @param id the id of the zoneDTO to save.
     * @param zoneDTO the zoneDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated zoneDTO,
     * or with status {@code 400 (Bad Request)} if the zoneDTO is not valid,
     * or with status {@code 404 (Not Found)} if the zoneDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the zoneDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @Operation(
        summary = "Partially update a zone",
        description = "Partially updates the given fields of the zone with the given id; null fields are ignored."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The updated zone",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ZoneDTO.class))
            ),
            @APIResponse(responseCode = "400", description = "Invalid zone id or mismatched ids"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "404", description = "No zone found for this id")
        }
    )
    @PATCH
    @Path(value = "/{id}")
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response partialUpdateZone(
        @Parameter(description = "Id of the zone to update", required = true) @PathParam(value = "id") final Long id,
        ZoneDTO zoneDTO, @Context SecurityContext ctx
    ) {
        log.debug("REST request to partial update Zone partially : {}, {}", id, zoneDTO);

        if (zoneDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, zoneDTO.id)) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!securityService.canAccess(ctx, id, Zone.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        Optional<ZoneDTO> result = zoneService.partialUpdate(zoneDTO);
        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, zoneDTO.id.toString())
        );
    }

    @Operation(
        summary = "Resize a zone",
        description = "Partially updates the scaling of the zone with the given id from a resize request."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The resized zone",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ZoneDTO.class))
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "404", description = "No zone found for this id")
        }
    )
    @PATCH
    @Path(value = "/scaling/{id}")
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response partialResizeZone(
        @Parameter(description = "Id of the zone to resize", required = true) @PathParam(value = "id") final Long id,
        ResizeZoneDTO rzoneDTO, @Context SecurityContext ctx
    ) {
        log.debug("REST request to partial update Zone partially : {}, {}", id, rzoneDTO);

        if (!securityService.canAccess(ctx, id, Zone.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        }


        Optional<ZoneDTO> result = zoneService.partialResizeUpdate(rzoneDTO, id);
        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, id.toString())
        );
    }
}
