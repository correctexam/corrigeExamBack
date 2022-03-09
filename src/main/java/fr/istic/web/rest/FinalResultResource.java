package fr.istic.web.rest;

import static javax.ws.rs.core.UriBuilder.fromPath;

import fr.istic.service.FinalResultService;
import fr.istic.web.rest.errors.BadRequestAlertException;
import fr.istic.web.util.HeaderUtil;
import fr.istic.web.util.ResponseUtil;
import fr.istic.service.dto.FinalResultDTO;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.istic.service.Paged;
import fr.istic.web.rest.vm.PageRequestVM;
import fr.istic.web.rest.vm.SortRequestVM;
import fr.istic.web.util.PaginationUtil;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link fr.istic.domain.FinalResult}.
 */
@Path("/api/final-results")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class FinalResultResource {

    private final Logger log = LoggerFactory.getLogger(FinalResultResource.class);

    private static final String ENTITY_NAME = "finalResult";

    @ConfigProperty(name = "application.name")
    String applicationName;


    @Inject
    FinalResultService finalResultService;
    /**
     * {@code POST  /final-results} : Create a new finalResult.
     *
     * @param finalResultDTO the finalResultDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body the new finalResultDTO, or with status {@code 400 (Bad Request)} if the finalResult has already an ID.
     */
    @POST
    public Response createFinalResult(FinalResultDTO finalResultDTO, @Context UriInfo uriInfo) {
        log.debug("REST request to save FinalResult : {}", finalResultDTO);
        if (finalResultDTO.id != null) {
            throw new BadRequestAlertException("A new finalResult cannot already have an ID", ENTITY_NAME, "idexists");
        }
        var result = finalResultService.persistOrUpdate(finalResultDTO);
        var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build()).entity(result);
        HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code PUT  /final-results} : Updates an existing finalResult.
     *
     * @param finalResultDTO the finalResultDTO to update.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the updated finalResultDTO,
     * or with status {@code 400 (Bad Request)} if the finalResultDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the finalResultDTO couldn't be updated.
     */
    @PUT
    public Response updateFinalResult(FinalResultDTO finalResultDTO) {
        log.debug("REST request to update FinalResult : {}", finalResultDTO);
        if (finalResultDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        var result = finalResultService.persistOrUpdate(finalResultDTO);
        var response = Response.ok().entity(result);
        HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, finalResultDTO.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code DELETE  /final-results/:id} : delete the "id" finalResult.
     *
     * @param id the id of the finalResultDTO to delete.
     * @return the {@link Response} with status {@code 204 (NO_CONTENT)}.
     */
    @DELETE
    @Path("/{id}")
    public Response deleteFinalResult(@PathParam("id") Long id) {
        log.debug("REST request to delete FinalResult : {}", id);
        finalResultService.delete(id);
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code GET  /final-results} : get all the finalResults.
     *
     * @param pageRequest the pagination information.
     * @return the {@link Response} with status {@code 200 (OK)} and the list of finalResults in body.
     */
    @GET
    public Response getAllFinalResults(@BeanParam PageRequestVM pageRequest, @BeanParam SortRequestVM sortRequest, @Context UriInfo uriInfo) {
        log.debug("REST request to get a page of FinalResults");
        var page = pageRequest.toPage();
        var sort = sortRequest.toSort();
        Paged<FinalResultDTO> result = finalResultService.findAll(page);
        var response = Response.ok().entity(result.content);
        response = PaginationUtil.withPaginationInfo(response, uriInfo, result);
        return response.build();
    }


    /**
     * {@code GET  /final-results/:id} : get the "id" finalResult.
     *
     * @param id the id of the finalResultDTO to retrieve.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the finalResultDTO, or with status {@code 404 (Not Found)}.
     */
    @GET
    @Path("/{id}")

    public Response getFinalResult(@PathParam("id") Long id) {
        log.debug("REST request to get FinalResult : {}", id);
        Optional<FinalResultDTO> finalResultDTO = finalResultService.findOne(id);
        return ResponseUtil.wrapOrNotFound(finalResultDTO);
    }
}
