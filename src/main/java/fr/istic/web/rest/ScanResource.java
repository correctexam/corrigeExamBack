package fr.istic.web.rest;

import static jakarta.ws.rs.core.UriBuilder.fromPath;

import fr.istic.service.ScanService;
import fr.istic.service.SecurityService;
import fr.istic.web.rest.errors.BadRequestAlertException;
import fr.istic.web.util.HeaderUtil;
import fr.istic.web.util.ResponseUtil;
import fr.istic.service.dto.ScanDTO;
import fr.istic.service.dto.ScanDTOContent;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.domain.Scan;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.Paged;
import fr.istic.web.rest.vm.PageRequestVM;
import fr.istic.web.rest.vm.SortRequestVM;
import fr.istic.web.util.PaginationUtil;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link fr.istic.domain.Scan}.
 */
@Path("/api/scans")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ScanResource {

    private final Logger log = LoggerFactory.getLogger(ScanResource.class);

    private static final String ENTITY_NAME = "scan";

    @ConfigProperty(name = "application.name")
    String applicationName;


    @Inject
    ScanService scanService;
    @Inject
    SecurityService securityService;

    /**
     * {@code POST  /scans} : Create a new scan.
     *
     * @param scanDTO the scanDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body the new scanDTO, or with status {@code 400 (Bad Request)} if the scan has already an ID.
     */
    @POST
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response createScan(@Valid ScanDTOContent scanDTO, @Context UriInfo uriInfo) {
        log.debug("REST request to save Scan : {}", scanDTO);
        if (scanDTO.id != null) {
            throw new BadRequestAlertException("A new scan cannot already have an ID", ENTITY_NAME, "idexists");
        }
        var result = scanService.persistOrUpdate(scanDTO);
        var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build()).entity(result);
        HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code PUT  /scans} : Updates an existing scan.
     *
     * @param scanDTO the scanDTO to update.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the updated scanDTO,
     * or with status {@code 400 (Bad Request)} if the scanDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the scanDTO couldn't be updated.
     */
    @PUT
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response updateScan(@Valid ScanDTOContent scanDTO, @Context SecurityContext ctx) {
        log.debug("REST request to update Scan : {}", scanDTO);
        if (scanDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!securityService.canAccess(ctx, scanDTO.id, Scan.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        var result = scanService.persistOrUpdate(scanDTO);
        var response = Response.ok().entity(result);
        HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, scanDTO.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code DELETE  /scans/:id} : delete the "id" scan.
     *
     * @param id the id of the scanDTO to delete.
     * @return the {@link Response} with status {@code 204 (NO_CONTENT)}.
     */
    @DELETE
    @Path("/{id}")
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response deleteScan(@PathParam("id") Long id, @Context SecurityContext ctx) {
        log.debug("REST request to delete Scan : {}", id);
        if (!securityService.canAccess(ctx, id, Scan.class  )){
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        scanService.delete(id);
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code GET  /scans} : get all the scans.
     *
     * @param pageRequest the pagination information.
     * @return the {@link Response} with status {@code 200 (OK)} and the list of scans in body.
     */
    @GET
//    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response getAllScans(@BeanParam PageRequestVM pageRequest, @BeanParam SortRequestVM sortRequest, @Context UriInfo uriInfo, @Context SecurityContext ctx) {
        log.debug("REST request to get a page of Scans");
        var page = pageRequest.toPage();
        var sort = sortRequest.toSort();

        MultivaluedMap param = uriInfo.getQueryParameters();
        if (param.containsKey("name")) {
            List name = (List) param.get("name");
            Paged<ScanDTO> result =  scanService.findbyName("" + name.get(0),page);
            var response = Response.ok().entity(result.content);
            response = PaginationUtil.withPaginationInfo(response, uriInfo, result);
            return response.build();

        }else {
            Paged<ScanDTO> result  = scanService.findAll(page);
            var response = Response.ok().entity(result.content);
            response = PaginationUtil.withPaginationInfo(response, uriInfo, result);
            return response.build();

        }




    }


    /**
     * {@code GET  /scans/:id} : get the "id" scan.
     *
     * @param id the id of the scanDTO to retrieve.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the scanDTO, or with status {@code 404 (Not Found)}.
     */
    @GET
    @Path("/{id}")
    public Response getScan(@PathParam("id") Long id, @Context SecurityContext ctx) {
        log.debug("REST request to get Scan : {}", id);
        Optional<ScanDTO> scanDTO = scanService.findOne(id);
        return ResponseUtil.wrapOrNotFound(scanDTO);
    }
}
