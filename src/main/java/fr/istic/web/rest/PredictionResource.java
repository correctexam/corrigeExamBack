package fr.istic.web.rest;

import static jakarta.ws.rs.core.UriBuilder.fromPath;

import fr.istic.service.PredictionService;
import fr.istic.web.rest.errors.AccountResourceException;
import fr.istic.web.rest.errors.BadRequestAlertException;
import fr.istic.web.util.HeaderUtil;
import fr.istic.web.util.ResponseUtil;
import fr.istic.service.dto.PredictionDTO;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.domain.Authority;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.Prediction;
import fr.istic.domain.User;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link fr.istic.domain.Prediction}.
 */
@Path("/api/predictions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class PredictionResource {

    private final Logger log = LoggerFactory.getLogger(PredictionResource.class);

    private static final String ENTITY_NAME = "prediction";

    @ConfigProperty(name = "application.name")
    String applicationName;

    @Inject
    SecurityService securityService;

    @Inject
    PredictionService predictionService;

    /**
     * {@code POST  /predictions} : Create a new prediction.
     *
     * @param predictionDTO the predictionDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body the new predictionDTO, or with status {@code 400 (Bad Request)} if the prediction already has an ID.
     */
    @POST
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response createPrediction(PredictionDTO predictionDTO, @Context UriInfo uriInfo) {
        log.debug("REST request to save Prediction : {}", predictionDTO);
        if (predictionDTO.id != null) {
            throw new BadRequestAlertException("A new prediction cannot already have an ID", ENTITY_NAME, "idexists");
        }
        var result = predictionService.persistOrUpdate(predictionDTO);
        var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build()).entity(result);
        HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code PUT  /predictions} : Updates an existing prediction.
     *
     * @param predictionDTO the predictionDTO to update.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the updated predictionDTO,
     * or with status {@code 400 (Bad Request)} if the predictionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the predictionDTO couldn't be updated.
     */
    @PUT
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response updatePrediction(PredictionDTO predictionDTO, @Context SecurityContext ctx) {
        log.debug("REST request to update Prediction : {}", predictionDTO);
        if (predictionDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!securityService.canAccess(ctx, predictionDTO.id, Prediction.class)) {
            return Response.status(403, "Current user cannot access to this resource").build();
        }
        var result = predictionService.persistOrUpdate(predictionDTO);
        var response = Response.ok().entity(result);
        HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, predictionDTO.id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code DELETE  /predictions/:id} : delete the "id" prediction.
     *
     * @param id the id of the predictionDTO to delete.
     * @return the {@link Response} with status {@code 204 (NO_CONTENT)}.
     */
    @DELETE
    @Path("/{id}")
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response deletePrediction(@PathParam("id") Long id, @Context SecurityContext ctx) {
        log.debug("REST request to delete Prediction : {}", id);
        if (!securityService.canAccess(ctx, id, Prediction.class)) {
            return Response.status(403, "Current user cannot access to this resource").build();
        }
        predictionService.delete(id);
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()).forEach(response::header);
        return response.build();
    }

    /**
     * {@code GET  /predictions} : get all the predictions.
     *
     * @param pageRequest the pagination information.
     * @return the {@link Response} with status {@code 200 (OK)} and the list of predictions in body.
     */
    @GET
    public Response getAllPredictions(@BeanParam PageRequestVM pageRequest, @BeanParam SortRequestVM sortRequest, @Context UriInfo uriInfo, @Context SecurityContext ctx) {
        log.debug("REST request to get a page of Predictions");
        var page = pageRequest.toPage();
        var sort = sortRequest.toSort();
        MultivaluedMap<String, String> param = uriInfo.getQueryParameters();
        Paged<PredictionDTO> result = new Paged<>(0, 0, 0, 0, new ArrayList<>());
        if (param.containsKey("questionId")) {
            List<String> questionId = param.get("questionId");
            result = predictionService.findPredictionByQuestionId(page, Long.parseLong(questionId.get(0)));
        } else {
            if (ctx.getUserPrincipal().getName() != null) {
                var userLogin = Optional.ofNullable(ctx.getUserPrincipal().getName());
                if (!userLogin.isPresent()) {
                    throw new AccountResourceException("Current user login not found");
                }
                var user = User.findOneByLogin(userLogin.get());
                if (!user.isPresent()) {
                    throw new AccountResourceException("User could not be found");
                } else if (user.get().authorities.size() >= 1 && user.get().authorities.stream().anyMatch(e1 -> e1.equals(new Authority("ROLE_ADMIN")))) {
                    result = predictionService.findAll(page);
                } else {
                    return Response.status(403, "Current user cannot access to this resource").build();
                }
            }
        }
        var response = Response.ok().entity(result.content);
        response = PaginationUtil.withPaginationInfo(response, uriInfo, result);
        return response.build();
    }

    /**
     * {@code GET  /predictions/:id} : get the "id" prediction.
     *
     * @param id the id of the predictionDTO to retrieve.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the predictionDTO, or with status {@code 404 (Not Found)}.
     */
    @GET
    @Path("/{id}")
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response getPrediction(@PathParam("id") Long id, @Context SecurityContext ctx) {
        log.debug("REST request to get Prediction : {}", id);
        if (!securityService.canAccess(ctx, id, Prediction.class)) {
            return Response.status(403, "Current user cannot access to this resource").build();
        }
        Optional<PredictionDTO> predictionDTO = predictionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(predictionDTO);
    }
}
