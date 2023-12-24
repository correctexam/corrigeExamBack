package fr.istic.web.rest;

import static javax.ws.rs.core.UriBuilder.fromPath;

import fr.istic.domain.Authority;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.User;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.ExamSheetService;
import fr.istic.web.rest.errors.AccountResourceException;
import fr.istic.web.rest.errors.BadRequestAlertException;
import fr.istic.web.util.HeaderUtil;
import fr.istic.web.util.ResponseUtil;
import fr.istic.service.dto.ExamSheetDTO;

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
import javax.json.JsonObject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link fr.istic.domain.ExamSheet}.
 */
@Path("/api/exam-sheets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ExamSheetResource {

    private final Logger log = LoggerFactory.getLogger(ExamSheetResource.class);

    private static final String ENTITY_NAME = "examSheet";

    @ConfigProperty(name = "application.name")
    String applicationName;

    @Inject
    ExamSheetService examSheetService;

    @Inject
    SecurityService securityService;

    /**
     * {@code POST  /exam-sheets} : Create a new examSheet.
     *
     * @param examSheetDTO the examSheetDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body
     *         the new examSheetDTO, or with status {@code 400 (Bad Request)} if the
     *         examSheet has already an ID.
     */
    @POST
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response createExamSheet(@Valid ExamSheetDTO examSheetDTO, @Context UriInfo uriInfo) {
        log.debug("REST request to save ExamSheet : {}", examSheetDTO);
        if (examSheetDTO.id != null) {
            throw new BadRequestAlertException("A new examSheet cannot already have an ID", ENTITY_NAME, "idexists");
        }
        var result = examSheetService.persistOrUpdate(examSheetDTO);
        var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build()).entity(result);
        HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString())
                .forEach(response::header);
        return response.build();
    }

    /**
     * {@code PUT  /exam-sheets} : Updates an existing examSheet.
     *
     * @param examSheetDTO the examSheetDTO to update.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the
     *         updated examSheetDTO,
     *         or with status {@code 400 (Bad Request)} if the examSheetDTO is not
     *         valid,
     *         or with status {@code 500 (Internal Server Error)} if the
     *         examSheetDTO couldn't be updated.
     */
    @PUT
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response updateExamSheet(@Valid ExamSheetDTO examSheetDTO, @Context SecurityContext ctx) {
        log.debug("REST request to update ExamSheet : {}", examSheetDTO);
        if (examSheetDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!securityService.canAccess(ctx, examSheetDTO.id, ExamSheet.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        var result = examSheetService.persistOrUpdate(examSheetDTO);
        var response = Response.ok().entity(result);
        HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, examSheetDTO.id.toString())
                .forEach(response::header);
        return response.build();
    }

    @PUT
    @Path("/boundstudents/{id}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response updateExamSheetboundstudents(@PathParam("id") Long id, List<Long> studentsId,
            @Context SecurityContext ctx) {

        if (id == null || id <= 0) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!securityService.canAccess(ctx, id, ExamSheet.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        ExamSheetDTO result = null;
        try {
            result = examSheetService.updateStudent(id, studentsId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result != null) {
            var response = Response.ok().entity(result);
            return response.build();
        } else {
            var response = Response.noContent();
            return response.build();

        }
    }

    /**
     * {@code DELETE  /exam-sheets/:id} : delete the "id" examSheet.
     *
     * @param id the id of the examSheetDTO to delete.
     * @return the {@link Response} with status {@code 204 (NO_CONTENT)}.
     */
    @DELETE
    @Path("/{id}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response deleteExamSheet(@PathParam("id") Long id, @Context SecurityContext ctx) {
        log.debug("REST request to delete ExamSheet : {}", id);
        if (!securityService.canAccess(ctx, id, ExamSheet.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        ;

        examSheetService.delete(id);
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())
                .forEach(response::header);
        return response.build();
    }

    /**
     * {@code GET  /exam-sheets} : get all the examSheets.
     *
     * @param pageRequest the pagination information.
     * @return the {@link Response} with status {@code 200 (OK)} and the list of
     *         examSheets in body.
     */
    @GET
    public Response getAllExamSheets(@BeanParam PageRequestVM pageRequest, @BeanParam SortRequestVM sortRequest,
            @Context UriInfo uriInfo, @Context SecurityContext ctx) {
        log.debug("REST request to get a page of ExamSheets");
        var page = pageRequest.toPage();
        var sort = sortRequest.toSort();

        Paged<ExamSheetDTO> result = new Paged(0, 0, 0, 0, new ArrayList<>());
        MultivaluedMap param = uriInfo.getQueryParameters();

        if (param.containsKey("name")) {
            List name = (List) param.get("name");
            result = examSheetService.findExamSheetByName(page, "" + name.get(0));
        } else {
            if (ctx.getUserPrincipal().getName() != null) {

                var userLogin = Optional
                        .ofNullable(ctx.getUserPrincipal().getName());
                if (!userLogin.isPresent()) {
                    throw new AccountResourceException("Current user login not found");
                }
                var user = User.findOneByLogin(userLogin.get());
                if (!user.isPresent()) {
                    throw new AccountResourceException("User could not be found");
                } else if (user.get().authorities.size() >= 1
                        && user.get().authorities.stream().anyMatch(e1 -> e1.equals(new Authority("ROLE_ADMIN"))
                                || e1.equals(new Authority("ROLE_USER")))) {
                    if (param.containsKey("scanId") && param.containsKey("nbreFeuilleParCopie")
                            && param.containsKey("numberPagesInScan")) {
                        List scanId = (List) param.get("scanId");
                        int nbreFeuilleParCopie = Integer
                                .valueOf("" + ((List) param.get("nbreFeuilleParCopie")).get(0));
                        int numberPagesInScan = Integer.valueOf("" + ((List) param.get("numberPagesInScan")).get(0));
                        if (nbreFeuilleParCopie > 0 && numberPagesInScan > 0
                                && numberPagesInScan % nbreFeuilleParCopie == 0) {
                            try {
                                result = examSheetService.findOrCreateExamSheetByName(page,
                                        Long.valueOf("" + scanId.get(0)), nbreFeuilleParCopie, numberPagesInScan);
                            } catch (NumberFormatException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        } else {
                            log.error("query sheets but with inconsistency in pages " + nbreFeuilleParCopie + " "
                                    + numberPagesInScan + " " + scanId.get(0));
                        }
                    } else if (param.containsKey("scanId") && param.containsKey("pagemin")
                            && param.containsKey("pagemax")) {
                        List scanId = (List) param.get("scanId");
                        int pagemin = Integer.valueOf("" + ((List) param.get("pagemin")).get(0));
                        int pagemax = Integer.valueOf("" + ((List) param.get("pagemax")).get(0));
                        if (pagemin >= 0 && pagemax >= 0 && pagemax >= pagemin) {
                            try {
                                result = examSheetService.findOrCreateExamSheetByPageMinAndPageMax(page,
                                        Long.valueOf("" + scanId.get(0)), pagemin, pagemax);
                            } catch (NumberFormatException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        } else {
                            log.error("query sheets but with inconsistency in pages min and max " + pagemin + " "
                                    + pagemax + " " + scanId.get(0));
                        }
                    }

                    else {
                        result = examSheetService.findAll(page);

                    }

                } else {
                    return Response.status(403, "Current user cannot access to this ressource").build();
                }
            }
        }

        var response = Response.ok().entity(result.content);
        response = PaginationUtil.withPaginationInfo(response, uriInfo, result);
        return response.build();
    }

    /**
     * {@code GET  /exam-sheets/:id} : get the "id" examSheet.
     *
     * @param id the id of the examSheetDTO to retrieve.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the
     *         examSheetDTO, or with status {@code 404 (Not Found)}.
     */
    @GET
    @Path("/{id}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getExamSheet(@PathParam("id") Long id, @Context SecurityContext ctx) {
        log.debug("REST request to get ExamSheet : {}", id);
        if (!securityService.canAccess(ctx, id, ExamSheet.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        ;

        Optional<ExamSheetDTO> examSheetDTO = examSheetService.findOne(id);
        return ResponseUtil.wrapOrNotFound(examSheetDTO);
    }
}
