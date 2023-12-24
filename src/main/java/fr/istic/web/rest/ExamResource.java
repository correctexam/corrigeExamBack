package fr.istic.web.rest;

import static javax.ws.rs.core.UriBuilder.fromPath;

import fr.istic.domain.Authority;
import fr.istic.domain.Course;
import fr.istic.domain.Exam;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.Scan;
import fr.istic.domain.Student;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.User;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.ExamService;
import fr.istic.web.rest.errors.AccountResourceException;
import fr.istic.web.rest.errors.BadRequestAlertException;
import fr.istic.web.util.HeaderUtil;
import fr.istic.web.util.ResponseUtil;
import fr.istic.service.dto.ExamDTO;

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
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST controller for managing {@link fr.istic.domain.Exam}.
 */
@Path("/api/exams")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ExamResource {

    private final Logger log = LoggerFactory.getLogger(ExamResource.class);

    private static final String ENTITY_NAME = "exam";

    @ConfigProperty(name = "application.name")
    String applicationName;

    @Inject
    ExamService examService;

    @Inject
    SecurityService securityService;

    /**
     * {@code POST  /exams} : Create a new exam.
     *
     * @param examDTO the examDTO to create.
     * @return the {@link Response} with status {@code 201 (Created)} and with body
     *         the new examDTO, or with status {@code 400 (Bad Request)} if the exam
     *         has already an ID.
     */
    @POST
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response createExam(@Valid ExamDTO examDTO, @Context UriInfo uriInfo) {
        log.debug("REST request to save Exam : {}", examDTO);
        if (examDTO.id != null) {
            throw new BadRequestAlertException("A new exam cannot already have an ID", ENTITY_NAME, "idexists");
        }
        var result = examService.persistOrUpdate(examDTO);
        var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build()).entity(result);
        HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString())
                .forEach(response::header);
        return response.build();
    }

    /**
     * {@code PUT  /exams} : Updates an existing exam.
     *
     * @param examDTO the examDTO to update.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the
     *         updated examDTO,
     *         or with status {@code 400 (Bad Request)} if the examDTO is not valid,
     *         or with status {@code 500 (Internal Server Error)} if the examDTO
     *         couldn't be updated.
     */
    @PUT
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response updateExam(@Valid ExamDTO examDTO, @Context SecurityContext ctx) {
        log.debug("REST request to update Exam : {}", examDTO);
        if (examDTO.id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!securityService.canAccess(ctx, examDTO.id, Exam.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        var result = examService.persistOrUpdate(examDTO);
        var response = Response.ok().entity(result);
        HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, examDTO.id.toString())
                .forEach(response::header);
        return response.build();
    }

    /**
     * {@code DELETE  /exams/:id} : delete the "id" exam.
     *
     * @param id the id of the examDTO to delete.
     * @return the {@link Response} with status {@code 204 (NO_CONTENT)}.
     */
    @DELETE
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Path("/{id}")
    public Response deleteExam(@PathParam("id") Long id, @Context SecurityContext ctx) {
        log.debug("REST request to delete Exam : {}", id);
        if (!securityService.canAccess(ctx, id, Exam.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        examService.delete(id);
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())
                .forEach(response::header);
        return response.build();
    }

    @DELETE
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Path("/cleanAllStudentSheet/{id}")
    @Transactional
    public Response deleteStudentSheet(@PathParam("id") Long id, @Context SecurityContext ctx) {
        log.debug("REST request to delete Exam : {}", id);
        if (!securityService.canAccess(ctx, id, Exam.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        Optional<Exam> ex = Exam.findByIdOptional(id);
        if (ex.isPresent()){
            List<Student> st = Student.findStudentsbyCourseId(ex.get().course.id).list();
            Set<ExamSheet> toRemoveS = new HashSet<>();
            for (Student student : st){
                List<ExamSheet> toRemove = student.examSheets.stream().filter(es -> es.scan.id == ex.get().scanfile.id).collect(Collectors.toList());
                toRemoveS.addAll(toRemove);
                student.examSheets.removeIf(es -> es.scan.id == ex.get().scanfile.id);
                Student.update(student);
            }
            for (ExamSheet toRemove1: toRemoveS){
                if (StudentResponse.findStudentResponsesbysheetId(toRemove1.id).count() ==0){
                    toRemove1.delete();
                }
            }

        }
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, "examSheet", "-1")
                .forEach(response::header);
        return response.build();
    }

    /**
     * {@code GET  /exams} : get all the exams.
     *
     * @param pageRequest the pagination information.
     * @return the {@link Response} with status {@code 200 (OK)} and the list of
     *         exams in body.
     */
    @GET
    public Response getAllExams(@BeanParam PageRequestVM pageRequest, @BeanParam SortRequestVM sortRequest,
            @Context UriInfo uriInfo, @Context SecurityContext ctx) {
        log.debug("REST request to get a page of Exams");
        var page = pageRequest.toPage();
        var sort = sortRequest.toSort();
        Paged<ExamDTO> result = new Paged<>(0,0,0,0, new ArrayList<>());
        MultivaluedMap param = uriInfo.getQueryParameters();
        if (param.containsKey("courseId")) {
            List id = (List) param.get("courseId");
            if (!securityService.canAccess(ctx, Long.parseLong("" + id.get(0)), Course.class)) {
                return Response.status(403, "Current user cannot access to this ressource").build();
            }

            result = examService.findExambyCourseId(page, Long.parseLong("" + id.get(0)));
        } else if (param.containsKey("scanId")) {
            List scanId = (List) param.get("scanId");
            /*if (!securityService.canAccess(ctx, Long.parseLong("" + scanId.get(0)), Scan.class)) {
                return Response.status(403, "Current user cannot access to this ressource").build();
            }*/
            result = examService.findExambyScanId(page, Long.parseLong("" + scanId.get(0)));

        } else {
            if (ctx.getUserPrincipal().getName()!= null){


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
                result = examService.findAll(page);

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
     * {@code GET  /exams/:id} : get the "id" exam.
     *
     * @param id the id of the examDTO to retrieve.
     * @return the {@link Response} with status {@code 200 (OK)} and with body the
     *         examDTO, or with status {@code 404 (Not Found)}.
     */
    @GET
    @Path("/{id}")
    public Response getExam(@PathParam("id") Long id, @Context SecurityContext ctx) {
        log.debug("REST request to get Exam : {}", id);
        if (!securityService.canAccess(ctx, id, Exam.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        ;

        Optional<ExamDTO> examDTO = examService.findOne(id);
        return ResponseUtil.wrapOrNotFound(examDTO);
    }
}
