package fr.istic.web.rest;

import static jakarta.ws.rs.core.UriBuilder.fromPath;

import fr.istic.domain.Authority;
import fr.istic.domain.Comments;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.Student;
import fr.istic.domain.User;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.CommentsService;
import fr.istic.web.rest.errors.AccountResourceException;
import fr.istic.web.rest.errors.BadRequestAlertException;
import fr.istic.web.util.HeaderUtil;
import fr.istic.web.util.ResponseUtil;
import fr.istic.service.dto.CommentsDTO;
import fr.istic.service.dto.ExamSheetDTO;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.istic.service.Paged;
import fr.istic.service.SecurityService;
import fr.istic.service.StudentService;
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
import java.util.StringTokenizer;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * REST controller for managing {@link fr.istic.domain.Comments}.
 */
@Path("/api/comments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class CommentsResource {

    private final Logger log = LoggerFactory.getLogger(CommentsResource.class);

    private static final String ENTITY_NAME = "comments";

    @ConfigProperty(name = "application.name")
    String applicationName;

    @Inject
    CommentsService commentsService;

    @Inject
    SecurityService securityService;

    /**
     * {@code POST  /comments} : Create a new comments.
     *
     * @param commentsDTO the commentsDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body
     *         the new commentsDTO, or with status {@code 400 (Bad Request)} if the
     *         comments has already an ID.
     */
    @POST
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response createComments(CommentsDTO commentsDTO, @Context UriInfo uriInfo) {
        log.debug("REST request to save Comments : {}", commentsDTO);
        if (commentsDTO.id != null) {
            throw new BadRequestAlertException("A new comments cannot already have an ID", ENTITY_NAME, "idexists");
        }
        var result = commentsService.persistOrUpdate(commentsDTO);
        var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build()).entity(result);
        HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString())
                .forEach(response::header);
        return response.build();
    }

    /**
     * {@code PUT  /comments} : Updates an existing comments.
     *
     * @param commentsDTO the commentsDTO to update.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the
     *         updated commentsDTO,
     *         or with status {@code 400 (Bad Request)} if the commentsDTO is not
     *         valid,
     *         or with status {@code 500 (Internal Server Error)} if the commentsDTO
     *         couldn't be updated.
     */
    @PUT
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response updateComments(CommentsDTO commentsDTO, @Context SecurityContext ctx) {
        // log.error("REST request to update Comments : {}", commentsDTO);
        if (commentsDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!securityService.canAccess(ctx, commentsDTO.id, Comments.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        var result = commentsService.persistOrUpdate(commentsDTO);
        var response = Response.ok().entity(result);
        HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, commentsDTO.id.toString())
                .forEach(response::header);
        return response.build();
    }

    /**
     * {@code DELETE  /comments/:id} : delete the "id" comments.
     *
     * @param id the id of the commentsDTO to delete.
     * @return the {@link Response} with status {@code 204 (NO_CONTENT)}.
     */
    @DELETE
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Path("/{id}")
    public Response deleteComments(@PathParam("id") Long id, @Context SecurityContext ctx) {
        log.debug("REST request to delete Comments : {}", id);
        if (!securityService.canAccess(ctx, id, Comments.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        ;

        commentsService.delete(id);
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())
                .forEach(response::header);
        return response.build();
    }

    /**
     * {@code GET  /comments} : get all the comments.
     *
     * @param pageRequest the pagination information.
     * @return the {@link Response} with status {@code 200 (OK)} and the list of
     *         comments in body.
     */
    @GET
    public Response getAllComments(@BeanParam PageRequestVM pageRequest, @BeanParam SortRequestVM sortRequest,
            @Context UriInfo uriInfo, @Context SecurityContext ctx) {
        log.debug("REST request to get a page of Comments");
        var page = pageRequest.toPage();
        var sort = sortRequest.toSort();
        Paged<CommentsDTO> result = new Paged<>(0, 0, 0, 0, new ArrayList<>());
        MultivaluedMap param = uriInfo.getQueryParameters();
        if (param.containsKey("zonegeneratedid")) {
            List<String> zonegeneratedid = (List<String>) param.get("zonegeneratedid");
            result = commentsService.findCommentsbyZonegeneratedid(page, zonegeneratedid.get(0));
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
                        && user.get().authorities.stream().anyMatch(e1 -> e1.equals(new Authority("ROLE_ADMIN")))) {
                    result = commentsService.findAll(page);

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
     * {@code GET  /comments/:id} : get the "id" comments.
     *
     * @param id the id of the commentsDTO to retrieve.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the
     *         commentsDTO, or with status {@code 404 (Not Found)}.
     */
    @GET
    @Path("/{id}")

    public Response getComments(@PathParam("id") Long id, @Context SecurityContext ctx) {
        log.debug("REST request to get Comments : {}", id);
        if (!securityService.canAccess(ctx, id, Comments.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        ;
        Optional<CommentsDTO> commentsDTO = commentsService.findOne(id);
        return ResponseUtil.wrapOrNotFound(commentsDTO);
    }

    @GET
    @Path("/migrate/{exId}")
    @Produces(MediaType.TEXT_PLAIN)
    public String migrateExamSheets4ExamId(@PathParam("exId") Long exId, @Context SecurityContext ctx) {
        log.debug("REST request to migrate Comment : {}", exId);
        List<Comments> toMigrate = Comments.findCommentByExamId("" + exId).list();
        List<ExamSheet> sheets = ExamSheet.getAll4ExamId(exId).list();
        StringBuffer result = new StringBuffer();
        List<String> newIds = new ArrayList<>();
        for (Comments comment : toMigrate) {

            StringTokenizer t = new StringTokenizer(comment.zonegeneratedid, "_");
            if (t.countTokens() > 1) {
                String examId = t.nextToken();
                String studentId = t.nextToken();
                String questiono = t.nextToken();
                String index = t.nextToken();

                List<ExamSheet> sheetsf = new ArrayList<>();
                for (ExamSheet sh : sheets){
                    if (sh.students != null && sh.students.size() > 0){
                        Student s = (Student) sh.students.toArray()[0];
                        if (Long.valueOf(studentId).equals(s.id)){
                            sheetsf.add(sh);
                        }
                    }
                }
                /*
                 * '' + this.exam!.id + '_' + this.selectionStudents![0].id + '_' +
                 * this.questionno + '_' + index
                 *
                 */
                if (sheetsf.size() > 0) {

                    var zonegeneratedid = "" + examId + "_" +
                            sheetsf.get(0).id + "_" + questiono + "_"
                            + index;
                    if (newIds.contains(comment.zonegeneratedid)){
                    result.append("--CLASH for " + comment.zonegeneratedid + "\n");

                    }
                    result.append("update comments set zonegeneratedid=\"" + zonegeneratedid
                            + "\" WHERE zonegeneratedid=\"" + comment.zonegeneratedid + "\";\n");
                    newIds.add(zonegeneratedid);

                } else {
                    result.append("--no match for " + comment.zonegeneratedid + "\n");
                }
            }

        }

        return result.toString();
    }

}
