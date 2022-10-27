package fr.istic.web.rest;

import fr.istic.config.JHipsterProperties;
import fr.istic.domain.Course;
import fr.istic.domain.CourseGroup;
import fr.istic.domain.Exam;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.FinalResult;
import fr.istic.domain.GradedComment;
import fr.istic.domain.Question;
import fr.istic.domain.Student;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.TextComment;
import fr.istic.domain.User;
import fr.istic.domain.enumeration.GradeType;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.CacheUploadService;
import fr.istic.service.CourseGroupService;
import fr.istic.service.CourseService;
import fr.istic.service.MailService;
import fr.istic.service.QuestionService;
import fr.istic.service.SecurityService;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import fr.istic.service.StudentService;
import fr.istic.service.UserService;
import fr.istic.service.customdto.ListUserModelShare;
import fr.istic.service.customdto.MailResultDTO;
import fr.istic.service.customdto.StudentMassDTO;
import fr.istic.service.customdto.StudentResultDTO;
import fr.istic.service.customdto.WorstAndBestSolution;
import fr.istic.service.customdto.correctexamstate.CorrectionExamState;
import fr.istic.service.customdto.correctexamstate.QuestionState;
import fr.istic.service.customdto.correctexamstate.StudentState;
import fr.istic.service.dto.QuestionDTO;
import fr.istic.web.util.HeaderUtil;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static javax.ws.rs.core.UriBuilder.fromPath;

/**
 * REST controller for managing {@link fr.istic.domain.Comments}.
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ExtendedAPI {

    private final Logger log = LoggerFactory.getLogger(ExtendedAPI.class);

    @Inject
    CourseGroupService courseGroupService;

    @Inject
    CourseService courseService;

    @Inject
    StudentService studentService;
    @Inject
    MailService mailService;

    @Inject
    SecurityService securityService;

    @Inject
    JHipsterProperties jHipsterProperties;

    @Inject
    CacheUploadService cacheUploadService;

    @Inject
    UserService userService;
    @Inject
    QuestionService questionService;

    private final class ComparatorImplementation implements Comparator<StudentResponse> {

        @Override
        public int compare(StudentResponse arg0, StudentResponse arg1) {
            return arg0.sheet.pagemin - arg1.sheet.pagemin;
        }
    }
    private final class ComparatorImplementation2 implements Comparator<StudentResponse> {

        @Override
        public int compare(StudentResponse arg0, StudentResponse arg1) {
            return arg0.question.numero - arg1.question.numero;
        }
    }


    private static class AccountResourceException extends RuntimeException {

        private AccountResourceException(String message) {
            super(message);
        }
    }

    // private final Logger log = LoggerFactory.getLogger(ExtendedAPI.class);

    @ConfigProperty(name = "application.name")
    String applicationName;

    private Exam computeFinalNote(long examId) {
        Exam ex = Exam.findById(examId);
        List<ExamSheet> sheets = ExamSheet.findExamSheetByScan(ex.scanfile.id).list();
        sheets.forEach(sh -> {
            // Compute Note
            List<StudentResponse> resps = StudentResponse.findStudentResponsesbysheetId(sh.id).list();
            var finalnote = 0;
            for (StudentResponse resp : resps) {
                if (resp.question.gradeType == GradeType.DIRECT && !"QCM".equals(resp.question.type.algoName)) {
                    if (resp.question.step > 0) {
                        finalnote = finalnote + ((resp.quarternote /4) * 100 / resp.question.step);
                    }
                } else if (resp.question.gradeType == GradeType.POSITIVE
                        && !"QCM".equals(resp.question.type.algoName)) {
                    int currentNote = 0;
                    for (var g : resp.gradedcomments) {
                        if (g.gradequarter != null) {
                            currentNote = currentNote + g.gradequarter;
                        }
                    }
                    ;
                    if (currentNote > (resp.question.quarterpoint) * resp.question.step) {
                        currentNote = (resp.question.quarterpoint ) * resp.question.step;
                    }
                    if (currentNote != resp.quarternote) {
                        resp.quarternote = currentNote;
                        StudentResponse.update(resp);
                    }
                    if (resp.question.step > 0) {
                        finalnote = finalnote + (currentNote * 100 /4 / resp.question.step);
                    }

                } else if (resp.question.gradeType == GradeType.NEGATIVE
                        && !"QCM".equals(resp.question.type.algoName)) {
                    int currentNote = resp.question.quarterpoint * resp.question.step;
                    for (var g : resp.gradedcomments) {
                        if (g.gradequarter != null) {
                            currentNote = currentNote - g.gradequarter;
                        }
                    }
                    ;
                    if (currentNote < 0) {
                        currentNote = 0;
                    }
                    if (currentNote != resp.quarternote) {
                        resp.quarternote = currentNote;
                        StudentResponse.update(resp);
                    }
                    if (resp.question.step > 0) {
                        finalnote = finalnote + (currentNote * 100 /4 / resp.question.step);
                    }

                } else if ("QCM".equals(resp.question.type.algoName) && resp.question.step > 0) {
                    int currentNote = 0;
                    for (var g : resp.gradedcomments) {
                        if (g.description.startsWith("correct")) {
                            currentNote = currentNote + resp.question.quarterpoint * resp.question.step;
                        } else if (g.description.startsWith("incorrect")) {
                            currentNote = currentNote - resp.question.quarterpoint;
                        }
                        if (currentNote != resp.quarternote) {
                            resp.quarternote = currentNote;
                            StudentResponse.update(resp);
                        }
                        finalnote = finalnote + (currentNote * 100 /4 / resp.question.step);

                    }
                } else if ("QCM".equals(resp.question.type.algoName) && resp.question.step <= 0) {
                    int currentNote = 0;
                    for (var g : resp.gradedcomments) {
                        if (g.description.startsWith("correct")) {
                            currentNote = currentNote + resp.question.quarterpoint;
                        }
                    }
                    if (currentNote != resp.quarternote) {
                        resp.quarternote = currentNote;
                        StudentResponse.update(resp);
                    }
                    finalnote = finalnote + (currentNote /4 * 100);
                }
            }
            final var finalnote1 = finalnote;
            sh.students.forEach(student -> {
                var q = FinalResult.findFinalResultByStudentIdAndExamId(student.id, examId);
                long count = q.count();
                if (count > 0) {
                    FinalResult r = q.firstResult();
                    r.note = finalnote1;
                    FinalResult.update(r);
                } else {
                    FinalResult r = new FinalResult();
                    r.student = student;
                    r.exam = ex;
                    r.note = finalnote1;
                    FinalResult.persistOrUpdate(r);
                }
            });
        });
        return ex;
    }

    @POST
    @Path("computeNode/{examId}")
    @Transactional
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response computeFinalNote4Exam(@PathParam("examId") long examId, @Context SecurityContext ctx) {

        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        this.computeFinalNote(examId);
        return Response.ok().build();
    }

    @POST
    @Path("sendResult/{examId}")
    @Transactional
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response sendResultToStudent(MailResultDTO dto, @PathParam("examId") long examId,
            @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        Exam ex = this.computeFinalNote(examId);

        List<Student> students = Student.findStudentsbyCourseId(ex.course.id).list();
        students.forEach(student -> {
            long count = FinalResult.findFinalResultByStudentIdAndExamId(student.id, ex.id).count();
            if (count > 0) {
                FinalResult r = FinalResult.findFinalResultByStudentIdAndExamId(student.id, ex.id).firstResult();
                ExamSheet sheet = ExamSheet.findExamSheetByScanAndStudentId(ex.scanfile.id, student.id).firstResult();
                String uuid = sheet.name;
                String body = dto.getBody();
                body = body.replace("${url}", this.jHipsterProperties.mail.baseUrl + "/copie/" + uuid + "/1");
                body = body.replace("${firstname}", student.firstname);
                body = body.replace("${lastname}", student.name);
                final DecimalFormat df = new DecimalFormat("0.00");
                body = body.replace("${note}", df.format(r.note / 100));
                mailService.sendEmail(student.mail, body,dto.getSubject());
                // TODO Send EMAIL
                // mailService.sendEmailFromTemplate(user, template, subject)

            } else {
                // TODO Send EMAIL

                // Pas de copie pour cet étudiant
            }
        });

        return Response.ok().build();
    }

    @GET
    @Path("showResult/{examId}")
    @Transactional
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response showResult(@PathParam("examId") long examId, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        Exam ex = this.computeFinalNote(examId);
        List<StudentResultDTO> results = new ArrayList<>();
        List<Student> students = Student.findStudentsbyCourseId(ex.course.id).list();
        students.forEach(student -> {
            long count = FinalResult.findFinalResultByStudentIdAndExamId(student.id, ex.id).count();
            if (count > 0) {
                FinalResult r = FinalResult.findFinalResultByStudentIdAndExamId(student.id, ex.id).firstResult();
                ExamSheet sheet = ExamSheet.findExamSheetByScanAndStudentId(ex.scanfile.id, student.id).firstResult();

                String uuid = sheet.name;
                int studentnumber = (sheet.pagemin / (sheet.pagemax - sheet.pagemin + 1)) + 1;
                var res = new StudentResultDTO();
                res.setNom(student.name);
                res.setPrenom(student.firstname);
                res.setIne(student.ine);
                res.setMail(student.mail);
                final DecimalFormat df = new DecimalFormat("0.00");
                res.setNote(df.format(r.note.doubleValue() / 100.0));
                res.setUuid(uuid);
                res.setStudentNumber("" + studentnumber);
                res.setAbi(false);
                res.setNotequestions(new HashMap<>());
                List<StudentResponse> resp = StudentResponse.findStudentResponsesbysheetId(sheet.id).list();
                resp.forEach(resp1 -> {
                    if ("QCM".equals(resp1.question.type.algoName) && resp1.question.step < 0) {
                        res.getNotequestions().put(resp1.question.numero,
                                df.format(
                                        resp1.quarternote.doubleValue() /4));

                    } else {
                        res.getNotequestions().put(resp1.question.numero,
                                df.format(
                                        ((resp1.quarternote.doubleValue() /4) * 100.0 / resp1.question.step) / 100.0));

                    }

                });
                results.add(res);

            } else {
                var res = new StudentResultDTO();
                res.setNom(student.name);
                res.setPrenom(student.firstname);
                res.setIne(student.ine);
                res.setMail(student.mail);
                res.setAbi(true);
                results.add(res);
            }
        });
        return Response.ok().entity(results).build();
    }

    @POST
    @Path("createstudentmasse")
    @Transactional
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response createAllStudent(StudentMassDTO dto, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, dto.getCourse(), Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        Course c = Course.findById(dto.getCourse());
        List<String> groupes = dto.getStudents().stream().map(e -> e.getGroupe()).distinct()
                .collect(Collectors.toList());
        Map<String, CourseGroup> groupesentities = new HashMap<>();
        groupes.forEach(g -> {
            long count = CourseGroup.findByNameandCourse(c.id, g).count();
            if (count > 0) {
                CourseGroup cgdest = CourseGroup.findByNameandCourse(c.id, g).firstResult();
                groupesentities.put(g, cgdest);
            } else {
                CourseGroup g1 = new CourseGroup();
                g1.course = c;
                g1.groupName = g;
                groupesentities.put(g, g1);

            }

        });
        dto.getStudents().forEach(s -> {
            Student st = new Student();
            st.ine = s.getIne();
            st.name = s.getNom();
            st.firstname = s.getPrenom();
            st.mail = s.getMail();
            Student st1 = Student.persistOrUpdate(st);
            groupesentities.get(s.getGroupe()).students.add(st1);
        });

        groupesentities.values().forEach(gc -> CourseGroup.persistOrUpdate(gc));
        return Response.ok().build();

    }

    @PUT
    @Path("updatestudent/{courseid}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Transactional
    public Response updatestudent4Course(fr.istic.service.customdto.StudentDTO student,
            @PathParam("courseid") long courseid, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, courseid, Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        var st = Student.findStudentsbyCourseIdAndINE(courseid, student.getIne()).firstResult();
        st.firstname = student.getPrenom();
        st.name = student.getNom();
        st.mail = student.getMail();
        Student.persistOrUpdate(st);
        return Response.ok().entity(st).build();
    }

    @PUT
    @Path("updatestudentgroup/{courseid}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Transactional
    public Response updatestudentgroup(fr.istic.service.customdto.StudentDTO student,
            @PathParam("courseid") long courseid, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, courseid, Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        var st = Student.findStudentsbyCourseIdAndINE(courseid, student.getIne()).firstResult();
        CourseGroup cgorigin = CourseGroup.findByStudentINEandCourse(courseid, student.getIne()).firstResult();
        cgorigin.students.remove(st);
        CourseGroup.persistOrUpdate(cgorigin);
        long count = CourseGroup.findByNameandCourse(courseid, student.getGroupe()).count();
        if (count > 0) {
            CourseGroup cgdest = CourseGroup.findByNameandCourse(courseid, student.getGroupe()).firstResult();
            cgdest.students.add(st);
            CourseGroup.persistOrUpdate(cgdest);
            return Response.ok().build();

        } else {
            CourseGroup cgdest = new CourseGroup();
            cgdest.groupName = student.getGroupe();
            cgdest.course = Course.findById(courseid);
            cgdest.students.add(st);
            CourseGroup.persistOrUpdate(cgdest);
            return Response.ok().build();
        }
    }

    @PUT
    @Path("deletestudentgroup/{courseid}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Transactional
    public Response deletestudentgroup(fr.istic.service.customdto.StudentDTO student,
            @PathParam("courseid") long courseid, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, courseid, Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        var st = Student.findStudentsbyCourseIdAndINE(courseid, student.getIne()).firstResult();
        CourseGroup cgorigin = CourseGroup.findByStudentINEandCourse(courseid, student.getIne()).firstResult();
        cgorigin.students.remove(st);
        CourseGroup.persistOrUpdate(cgorigin);
        Student.deleteById(st.id);
        return Response.ok().entity(st).build();
    }

    @PUT
    @Path("updatestudentine/{courseid}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Transactional
    public Response updatestudentine(fr.istic.service.customdto.StudentDTO student,
            @PathParam("courseid") long courseid, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, courseid, Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        var st = Student.findStudentsbyCourseIdAndFirsNameAndLastName(courseid, student.getPrenom(), student.getNom())
                .firstResult();
        st.ine = student.getIne();
        Student.persistOrUpdate(st);
        return Response.ok().entity(st).build();
    }

    @GET
    @Path("getstudentcours/{courseid}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Transactional
    public Response getAllStudent4Course(@PathParam("courseid") long courseid, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, courseid, Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        Course c = Course.findById(courseid);
        List<fr.istic.service.customdto.StudentDTO> students = new ArrayList<>();
        if (c != null) {
            c.groups.forEach(g -> {
                CourseGroup.findOneWithEagerRelationships(g.id).get().students.forEach(s -> {
                    fr.istic.service.customdto.StudentDTO sdto = new fr.istic.service.customdto.StudentDTO();
                    sdto.setIne(s.ine);
                    sdto.setNom(s.name);
                    sdto.setPrenom(s.firstname);
                    sdto.setMail(s.mail);
                    sdto.setGroupe(g.groupName);
                    students.add(sdto);
                });
            });

        }
        return Response.ok().entity(students).build();
    }

    @DELETE
    @Path("deletegroupstudents/{courseid}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Transactional
    public Response deleteAllStudent4Course(@PathParam("courseid") long courseid, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, courseid, Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        var userLogin = Optional
                .ofNullable(ctx.getUserPrincipal().getName());
        if (!userLogin.isPresent()) {
            throw new AccountResourceException("Current user login not found");
        }
        var user = User.findOneByLogin(userLogin.get());
        if (!user.isPresent()) {
            throw new AccountResourceException("User could not be found");
        }
        Course c = Course.findById(courseid);
        c.groups.forEach(g -> {
            g.students.forEach(st -> {
                st.groups.remove(g);
                Student.update(st);
            });
            CourseGroup.deleteById(g.id);
        });
        c.groups.clear();
        Course.update(c);
        return Response.ok().build();
    }

    @GET
    @Path("getBestAnswer/{examId}/{nume}")
    @Transactional
    public Response getBestAnswer(@PathParam("examId") long examId, @PathParam("nume") int nume) {
        List<String> res = StudentResponse.getBestAnswerforQuestionNoAndExamId(examId, nume).list().stream()
                .map(e -> e.name).collect(Collectors.toList());

        return Response.ok().entity(res).build();
    }

    @GET
    // @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    @Path("getAllStarAnswer/{examId}")
    @Transactional
    public Response getAllStarAnswer(@PathParam("examId") long examId) {
        Base64.Encoder encoder = Base64.getEncoder();

        Map<Integer, WorstAndBestSolution> res = new HashMap<Integer, WorstAndBestSolution>();
        List<StudentResponse> resbest = StudentResponse.getAllBestAnswerforExamId(examId).list();
        List<StudentResponse> resworst = StudentResponse.getAllWorstAnswerforExamId(examId).list();

        resbest.forEach(st -> {
            if (!res.containsKey(st.question.numero)) {
                res.put(st.question.numero, new WorstAndBestSolution(st.question.numero));
            }
            WorstAndBestSolution tmp = res.get(st.question.numero);
            // /reponse/' + btoa('/' + s1 + '/' + (this.questionno + 1) + '/')
            byte[] encodedContent = encoder.encode(("/" + st.sheet.name + "/" + (st.question.numero) + "/").getBytes());
            var s = new String(encodedContent);
            tmp.getBestSolutions().add("/reponse/" + s);
        });

        resworst.forEach(st -> {
            if (!res.containsKey(st.question.numero)) {
                res.put(st.question.numero, new WorstAndBestSolution(st.question.numero));
            }
            WorstAndBestSolution tmp = res.get(st.question.numero);
            // /reponse/' + btoa('/' + s1 + '/' + (this.questionno + 1) + '/')
            byte[] encodedContent = encoder.encode(("/" + st.sheet.name + "/" + (st.question.numero) + "/").getBytes());
            var s = new String(encodedContent);
            tmp.getWorstSolutions().add("/reponse/" + s);
        });

        return Response.ok().entity(res.values()).build();
    }

    @POST
    @Path("/uploadCache")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response fileUpload(@MultipartForm MultipartFormDataInput input) {
        try {
            cacheUploadService.uploadFile(input);
        } catch (Exception e) {
            return Response.serverError().build();

        }
        return Response.ok().build();
    }

    @GET
    @Path("/getCacheAlignPage/{examId}/{pageId}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getCachePageAlign(@PathParam("examId") long examId, @PathParam("pageId") int pageId) {
        try {
            return Response
                    .status(Response.Status.OK)
                    .entity(cacheUploadService.getAlignPage(examId, pageId, false))
                    .type(MediaType.TEXT_PLAIN)
                    .build();

        } catch (Exception e) {
            return Response.serverError().build();

        }
    }


    @GET
    @Path("/getCacheNonAlignPage/{examId}/{pageId}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getCachePageNoAlign(@PathParam("examId") long examId, @PathParam("pageId") int pageId) {
        try {
            return Response
                    .status(Response.Status.OK)
                    .entity(cacheUploadService.getAlignPage(examId, pageId, true))
                    .type(MediaType.TEXT_PLAIN)
                    .build();

        } catch (Exception e) {
            return Response.serverError().build();

        }
    }

    @GET
    @Path("/getCache/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile(@PathParam("fileName") String fileName) {

        try {
            File nf = cacheUploadService.getFile(fileName);
            ResponseBuilder response = Response.ok((Object) nf, MediaType.APPLICATION_OCTET_STREAM);
            response.header("Content-Disposition", "attachment;filename=" + nf);
            return response.build();
        } catch (Exception e) {
            return Response.noContent().build();
        }
    }

    /**
     * {@code GET /users} : get all users.
     *
     * @param pagination the pagination information.
     * @return the {@link Response} with status {@code 200 (OK)} and with body all
     *         users.
     */
    @GET
    @Path("/getUsers/{courseId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })

    public Response getAllUsers(@PathParam("courseId") long courseId, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, courseId, Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        var login = ctx.getUserPrincipal().getName();

        var res = this.courseService.getAllListUserModelShare(courseId, login);

        Response.ResponseBuilder response = Response.ok().entity(res);
        return response.build();
    }

    /**
     * {@code POST
     *
     * @param pagination the pagination information.
     *
     * @return the {@link Response} with status {@code 200 (OK)} and with body all
     *         users.
     */
    @PUT
    @Path("/updateProfs/{courseId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response updateProfs(@PathParam("courseId") long courseId, ListUserModelShare updateLists,
            @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, courseId, Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        if (updateLists.getAvailables() != null && updateLists.getAvailables().size() > 0) {
            this.courseService.addProfs(courseId, updateLists.getAvailables());
        }
        if (updateLists.getShared() != null && updateLists.getShared().size() > 0) {
            this.courseService.removeProfs(courseId, updateLists.getShared());

        }
        Response.ResponseBuilder response = Response.ok().entity(updateLists);
        return response.build();
    }

    @POST()
    @Path("/cleanResponse")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response cleanAllCorrectionAndComment(@Valid QuestionDTO questionDTO, @Context UriInfo uriInfo) {
        log.debug("REST request to clean Question : {}", questionDTO);
        var result = questionService.cleanAllCorrectionAndComment(questionDTO);
        var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build()).entity(result);
        HeaderUtil.createEntityCreationAlert(applicationName, true, "question", result.id.toString())
                .forEach(response::header);
        return response.build();
    }


    @DELETE
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Path("/deleteAllAnswerAndComment/{examId}")
    @Transactional
    public Response deleteAllAnswerAndComment(@PathParam("examId") long examId, @Context UriInfo uriInfo, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        Optional<Exam> ex = Exam.findByIdOptional(examId);
        if (ex.isPresent()){
            List<Question> qs  =Question.findQuestionbyExamId(examId).list();

            for (Question question : qs) {

            List<GradedComment> gradeComment = new ArrayList<GradedComment>();
            List<TextComment> textComments = new ArrayList<TextComment>();
            questionService.updateCorrectionAndAnswer(question, gradeComment, textComments);
            List<Long> gradeCommentids = gradeComment.stream().map(gc -> gc.id).collect(Collectors.toList());
            List<Long> textCommentsids = textComments.stream().map(gc -> gc.id).collect(Collectors.toList());
            questionService.deleteComments(gradeCommentids, textCommentsids);
            List<StudentResponse> srs =  StudentResponse.findAllByQuestionId(question.id).list();
            for (StudentResponse studentResponse : srs){
                studentResponse.delete();
            }
            }

        }
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, "studentResponse", "-1")
                .forEach(response::header);
        return response.build();
    }


    @GET
    @Path("/getExamStatus/{examId}")
   // @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getExamStatus(@PathParam("examId") long examId, @Context UriInfo uriInfo, @Context SecurityContext ctx) {

        /* if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }*/

        CorrectionExamState result = new CorrectionExamState();
        List<StudentResponse> res =StudentResponse.getAll4ExamId(examId).list();


        Map<Long, List<StudentResponse>> byQestion =  res.stream()
                   .collect(Collectors.groupingBy(StudentResponse::getQuestionId));

        for( long qid :byQestion.keySet() ) {
            List<StudentResponse> l =  byQestion.get(qid);
            QuestionState qs = new QuestionState();
            qs.setID(qid);
            qs.setAnsweredSheets(Long.valueOf(l.size()));
            l.sort(new ComparatorImplementation());
            qs.setFirstUnmarkedSheet(Long.valueOf(0));
            if (l.size()>0 && l.get(0).sheet.pagemin > 0) {
                if (l.size() == 1){
                    qs.setFirstUnmarkedSheet(Long.valueOf(l.get(0).sheet.pagemax +1));
                }
                for ( int i = 0;i< l.size()-1; i++) {
                    StudentResponse sl1 = l.get(i);
                    StudentResponse sl2 = l.get(i+1);
                    qs.setFirstUnmarkedSheet(Long.valueOf(sl1.sheet.pagemax +1));
    //                log.error("debug " + sl1.sheet.pagemax + " "+ sl2.sheet.pagemin);
                    if (sl1.sheet.pagemax + 1 < sl2.sheet.pagemin){
                        break;
                    } else if (i == l.size()-2 ){
                        qs.setFirstUnmarkedSheet(Long.valueOf(sl2.sheet.pagemax +1));
                    }
                }
            }

            result.getQuestions().add(qs);
        }

        Map<List<Long>, List<StudentResponse>> byStudent =  res.stream()
                   .collect(Collectors.groupingBy(StudentResponse::getStudentId));
        var students = new HashMap<Long, List<StudentResponse>>();
        byStudent.entrySet().stream().forEach(e-> {
            for (long id : e.getKey()){
                List<StudentResponse> sts = students.getOrDefault(id, new ArrayList<StudentResponse>());
                sts.addAll(e.getValue());
                if (!students.containsKey(id)){
                    students.put(id,sts);
                }
            }
        });

        for( long sid :students.keySet() ) {
            List<StudentResponse> l =  students.get(sid);
            l.sort(new ComparatorImplementation2());
            StudentState ss = new StudentState();
            ss.setID(sid);;
            ss.setAnsweredSheets(Long.valueOf(l.size()));
            if (l.size() ==0){
                ss.setFirstUnmarkedQuestion(Long.valueOf(1));

            } else if (l.size() ==1 && l.get(0).question.numero ==1) {
                ss.setFirstUnmarkedQuestion(Long.valueOf(2));

            } else if (l.size() >0 && l.get(0).question.numero !=1) {
                ss.setFirstUnmarkedQuestion(Long.valueOf(1));
            }
            else {
                for ( int i = 0;i< l.size()-1; i++) {
                    StudentResponse sl1 = l.get(i);
                    StudentResponse sl2 = l.get(i+1);
                    if (sl1.question.numero + 1 < sl2.question.numero ){
                        ss.setFirstUnmarkedQuestion(Long.valueOf(sl1.question.numero +1));
                        break;
                    } else if (i == l.size()-2 ){
                        ss.setFirstUnmarkedQuestion(Long.valueOf(sl1.question.numero +2));
                    }
                }
            }

            result.getStudents().add(ss);
        }


        Response.ResponseBuilder response = Response.ok().entity(result);
        return response.build();
    }

}
