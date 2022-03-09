package fr.istic.web.rest;

import static javax.ws.rs.core.UriBuilder.fromPath;

import fr.istic.service.ScanService;
import fr.istic.web.rest.errors.BadRequestAlertException;
import fr.istic.web.util.HeaderUtil;
import fr.istic.web.util.ResponseUtil;
import fr.istic.service.dto.ScanDTO;

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
    /**
     * {@code POST  /scans} : Create a new scan.
     *
     * @param scanDTO the scanDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body the new scanDTO, or with status {@code 400 (Bad Request)} if the scan has already an ID.
     */
    @POST
    public Response createScan(@Valid ScanDTO scanDTO, @Context UriInfo uriInfo) {
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
    public Response updateScan(@Valid ScanDTO scanDTO) {
        log.debug("REST request to update Scan : {}", scanDTO);
        if (scanDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
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
    public Response deleteScan(@PathParam("id") Long id) {
        log.debug("REST request to delete Scan : {}", id);
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
    public Response getAllScans(@BeanParam PageRequestVM pageRequest, @BeanParam SortRequestVM sortRequest, @Context UriInfo uriInfo) {
        log.debug("REST request to get a page of Scans");
        var page = pageRequest.toPage();
        var sort = sortRequest.toSort();
        Paged<ScanDTO> result = scanService.findAll(page);
        var response = Response.ok().entity(result.content);
        response = PaginationUtil.withPaginationInfo(response, uriInfo, result);
        return response.build();
    }


    /**
     * {@code GET  /scans/:id} : get the "id" scan.
     *
     * @param id the id of the scanDTO to retrieve.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the scanDTO, or with status {@code 404 (Not Found)}.
     */
    @GET
    @Path("/{id}")

    public Response getScan(@PathParam("id") Long id) {
        log.debug("REST request to get Scan : {}", id);
        Optional<ScanDTO> scanDTO = scanService.findOne(id);
        return ResponseUtil.wrapOrNotFound(scanDTO);
    }
}
