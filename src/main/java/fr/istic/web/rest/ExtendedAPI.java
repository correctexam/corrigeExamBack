package fr.istic.web.rest;


import fr.istic.domain.Course;
import fr.istic.domain.CourseGroup;
import fr.istic.domain.Exam;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.FinalResult;
import fr.istic.domain.Student;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.User;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.CourseGroupService;
import fr.istic.service.MailService;
import fr.istic.service.dto.CourseGroupDTO;
import fr.istic.service.dto.StudentDTO;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import fr.istic.service.StudentService;
import fr.istic.service.customdto.MailResultDTO;
import fr.istic.service.customdto.StudentMassDTO;
import fr.istic.service.customdto.StudentResultDTO;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for managing {@link fr.istic.domain.Comments}.
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ExtendedAPI {

    @Inject
    CourseGroupService courseGroupService;

    @Inject
    StudentService studentService;
    @Inject
    MailService mailService;

    private static class AccountResourceException extends RuntimeException {

        private AccountResourceException(String message) {
            super(message);
        }
    }

//    private final Logger log = LoggerFactory.getLogger(ExtendedAPI.class);

    @ConfigProperty(name = "application.name")
    String applicationName;


    private Exam computeFinalNote(long examId){
        Exam ex = Exam.findById(examId);
        List<ExamSheet> sheets = ExamSheet.findExamSheetByScan(ex.scanfile.id).list();
        sheets.forEach(sh -> {
            // Compute Note
            List<StudentResponse> resps = StudentResponse.findStudentResponsesbysheetId(sh.id).list();
            var finalnote = 0;
            for (StudentResponse resp : resps){
                finalnote = finalnote+ (resp.note * 100 /  resp.question.step  );
            }
            final var finalnote1 = finalnote;
            sh.students.forEach(student -> {
                var q = FinalResult.findFinalResultByStudentIdAndExamId(student.id, examId);
                long count = q.count();
                if (count >0){
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
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response computeFinalNote4Exam(@PathParam("examId") long examId, @Context SecurityContext ctx) {

        this.computeFinalNote(examId);
        return Response.ok().build();
    }

    @POST
    @Path("sendResult/{examId}")
    @Transactional
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response sendResultToStudent(MailResultDTO dto, @PathParam("examId") long examId, @Context SecurityContext ctx) {

        Exam ex = this.computeFinalNote(examId);

        List<Student> students = Student.findStudentsbyCourseId(ex.course.id).list();
        students.forEach(student -> {
            long count = FinalResult.findFinalResultByStudentIdAndExamId(student.id, ex.id).count();
            if (count>0){
                FinalResult r = FinalResult.findFinalResultByStudentIdAndExamId(student.id, ex.id).firstResult();
                ExamSheet sheet = ExamSheet.findExamSheetByScanAndStudentId(ex.scanfile.id,student.id).firstResult();
                String uuid = sheet.name;

                // TODO Send EMAIL
                // mailService.sendEmailFromTemplate(user, template, subject)


            }else {
               // TODO Send EMAIL

                // Pas de copie pour cet étudiant
            }
        });


        return Response.ok().build();
    }

    @GET
    @Path("showResult/{examId}")
    @Transactional
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response showResult(@PathParam("examId") long examId, @Context SecurityContext ctx) {
        Exam ex = this.computeFinalNote(examId);
        List<StudentResultDTO> results = new ArrayList<>();
        List<Student> students = Student.findStudentsbyCourseId(ex.course.id).list();
        students.forEach(student -> {
            long count = FinalResult.findFinalResultByStudentIdAndExamId(student.id, ex.id).count();
            if (count>0){
                FinalResult r = FinalResult.findFinalResultByStudentIdAndExamId(student.id, ex.id).firstResult();
                ExamSheet sheet = ExamSheet.findExamSheetByScanAndStudentId(ex.scanfile.id,student.id).firstResult();
                String uuid = sheet.name;
                var res = new StudentResultDTO();
                res.setNom(student.name);
                res.setPrenom(student.firstname);
                res.setIne(student.ine);
                res.setMail(student.mail);
                res.setNote(r.note);
                res.setUuid(uuid);
                res.setAbi(false);
                results.add(res);

            }else {
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
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    public Response createAllStudent(StudentMassDTO dto, @Context SecurityContext ctx) {
        var userLogin = Optional
                .ofNullable(ctx.getUserPrincipal().getName());
        if (!userLogin.isPresent()) {
            throw new AccountResourceException("Current user login not found");
        }
        var user = User.findOneByLogin(userLogin.get());
        if (!user.isPresent()) {
            throw new AccountResourceException("User could not be found");
        }
        Course c = Course.findById(dto.getCourse());
        List<String> groupes = dto.getStudents().stream().map(e -> e.getGroupe()).distinct()
                .collect(Collectors.toList());
        Map<String, CourseGroupDTO> groupesdto = new HashMap<>();
        groupes.forEach(g -> {
            CourseGroupDTO g1 = new CourseGroupDTO();
            g1.courseId = c.id;
            g1.groupName = g;
            groupesdto.put(g, g1);
        });
        dto.getStudents().forEach(s -> {
            StudentDTO sdto = new StudentDTO();
            sdto.ine = s.getIne();
            sdto.name = s.getNom();
            sdto.firstname = s.getPrenom();
            sdto.mail = s.getMail();
            StudentDTO sdto1 = this.studentService.persistOrUpdate(sdto);
            groupesdto.get(s.getGroupe()).students.add(sdto1);
        });

        groupesdto.values().forEach(gdto -> this.courseGroupService.persistOrUpdate(gdto));
        return Response.ok().build();

    }

    @GET
    @Path("getstudentcours/{courseid}")
    @Transactional
    public Response getAllStudent4Course(@PathParam("courseid") long courseid, @Context SecurityContext ctx) {
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
    @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    @Transactional
    public Response deleteAllStudent4Course(@PathParam("courseid") long courseid, @Context SecurityContext ctx) {
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

}
