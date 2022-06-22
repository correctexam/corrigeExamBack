package fr.istic.web.rest;

import static javax.ws.rs.core.UriBuilder.fromPath;

import fr.istic.service.ZoneService;
import fr.istic.service.customdto.ResizeZoneDTO;
import fr.istic.web.rest.errors.BadRequestAlertException;
import fr.istic.web.util.HeaderUtil;
import fr.istic.web.util.ResponseUtil;
import fr.istic.service.dto.ZoneDTO;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.domain.Zone;
import fr.istic.security.AuthoritiesConstants;
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
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for managing {@link fr.istic.domain.Zone}.
 */
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
    @DELETE
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    @Path("/{id}")
    public Response deleteZone(@PathParam("id") Long id, @Context SecurityContext ctx) {
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
    @GET
    @Path("/{id}")
    public Response getZone(@PathParam("id") Long id, @Context SecurityContext ctx) {
        log.debug("REST request to get Zone : {}", id);
        // No scurity for voir copies
        Optional<ZoneDTO> zoneDTO = zoneService.findOne(id);
        return ResponseUtil.wrapOrNotFound(zoneDTO);
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
    @PATCH
    @Path(value = "/{id}")
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response partialUpdateZone(
        @PathParam(value = "id") final Long id,
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

    @PATCH
    @Path(value = "/scaling/{id}")
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response partialResizeZone(
        @PathParam(value = "id") final Long id,
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
