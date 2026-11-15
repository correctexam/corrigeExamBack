package fr.istic.web.rest;

import fr.istic.domain.enumeration.StudentAccess;
import static fr.istic.service.FinalNoteService.VALUEFORABJ;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.config.JHipsterProperties;
import fr.istic.domain.Comments;
import fr.istic.domain.Exam;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.FinalResult;
import fr.istic.domain.Question;
import fr.istic.domain.Student;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.User;
import fr.istic.domain.enumeration.GradeType;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.CacheStudentPdfFService;
import fr.istic.service.ExamService;
import fr.istic.service.FinalNoteService;
import fr.istic.service.MailService;
import fr.istic.service.SecurityService;
import fr.istic.service.customdto.MailResultDTO;
import fr.istic.service.customdto.StudentResultDTO;
import fr.istic.web.rest.errors.AccountResourceException;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Final notes, absences (ABJ), results e-mails and results table of an exam.
 * <p>
 * Split out of the former {@code ExtendedAPI} class (T3): the REST paths are unchanged.
 */
@Tag(name = "Exam results", description = "Final notes, absences (ABJ), results e-mails and results table of an exam.")
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ExamResultResource {

    private final Logger log = LoggerFactory.getLogger(ExamResultResource.class);

    @Inject
    MailService mailService;

    @Inject
    SecurityService securityService;

    @Inject
    JHipsterProperties jHipsterProperties;

    @Inject
    CacheStudentPdfFService cacheStudentPdfFService;

    @Inject
    FinalNoteService finalNoteService;

    @Inject
    ExamService examService;

    @Operation(summary = "Compute final notes", description = "Recomputes and persists the final notes of every student for the exam.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "Final notes computed"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @POST
    @Path("computeNode/{examId}")
    @Transactional
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response computeFinalNote4Exam(@Parameter(description = "ID of the exam", required = true) @PathParam("examId") long examId, @Context SecurityContext ctx) {

        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        finalNoteService.computeFinalNote(examId, new HashMap<>(), new HashMap<>(), new HashMap<>());
        return Response.ok().build();
    }

    @Operation(summary = "Toggle the absent flag of a student", description = "Sets or unsets the absent (ABI) flag on the final result of a student for an exam.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "Absent flag updated"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @PUT
    @Path("toggleAsAbJ/{studentId}/{examId}/{abi}")
    @Transactional
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response toggleAsAbJ(@Parameter(description = "ID of the student", required = true) @PathParam("studentId") long studentId, @Parameter(description = "ID of the exam", required = true) @PathParam("examId") long examId, @Parameter(description = "Flag to set (2) or unset the absent (ABI) status", required = true) @PathParam("abi") long abi, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        var frs = FinalResult.findFinalResultByStudentIdAndExamId(studentId, examId).list();

        if (frs.size() >0) {
            var fr = frs.get(0);
            if (abi ==2 && fr.note != VALUEFORABJ) {
                fr.note = VALUEFORABJ;
                fr = FinalResult.update(fr);
            } else {
                fr.delete();
            }
        }
        else {
            if (abi ==2){

                FinalResult r = new FinalResult();
                r.student = Student.findById(Long.valueOf(studentId));
                r.exam = Exam.findById(Long.valueOf(examId));
                r.note = VALUEFORABJ;
                r = FinalResult.persistOrUpdate(r);
            }

        }
        return Response.ok().build();

    }

    @Operation(summary = "Send results to students", description = "Computes the final notes and sends an email to each student with the configured message and optional PDF attachment.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "Result emails sent"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "500", description = "Current user could not be resolved")
        }
    )
    @POST
    @Path("sendResult/{examId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response sendResultToStudent(MailResultDTO dto, @Parameter(description = "ID of the exam", required = true) @PathParam("examId") long examId,
            @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
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
        String _replyTo = user.get().email;
        if (_replyTo == null || "".equals(_replyTo)) {
            _replyTo = "no-reply.correctexam@univ-rennes.fr";
        }
        final String replyTo = _replyTo;
        Map<Long, FinalResult> finalfinalResultsByStudentId = new HashMap<>();
        Map<ExamSheet, Integer> finalNotes = new HashMap<>();
        Map<ExamSheet, List<StudentResponse>> mapstudentResp = new HashMap<>();

        Exam ex = finalNoteService.computeFinalNote(examId, finalfinalResultsByStudentId, finalNotes, mapstudentResp);

        List<Student> students = Student.findStudentsbyCourseId(ex.course.id).list();
        students.forEach(student -> {
            long count = FinalResult.findFinalResultByStudentIdAndExamId(student.id, ex.id).count();
            ExamSheet sheet = ExamSheet.findExamSheetByScanAndStudentId(ex.scanfile.id, student.id).firstResult();
            if (count > 0 && sheet !=null) {
                FinalResult r = FinalResult.findFinalResultByStudentIdAndExamId(student.id, ex.id).firstResult();
                String uuid = sheet.name;
                if (dto.getSheetuuid() == null || uuid.equals(dto.getSheetuuid())) {

                    String body = dto.getBody();
                    body = body.replace("${url}", this.jHipsterProperties.mail().baseUrl() + "/copie/" + uuid + "/1");
                    body = body.replace("${firstname}", student.firstname);
                    body = body.replace("${lastname}", student.name);
                    final DecimalFormat df = new DecimalFormat("0.00");
                    body = body.replace("${note}", df.format(r.note / 100.0));
                    if (dto.isMailpdf()) {
                        InputStream in;
                        try {
                            in = this.cacheStudentPdfFService.getFile(examId, sheet.name + ".pdf");
                            byte[] bytes = IOUtils.toByteArray(in);
                            mailService.sendEmailWithAttachement(student.mail, body, dto.getSubject(),
                                    student.firstname + "_" + student.name + ".pdf", bytes, "application/pdf", replyTo);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        mailService.sendEmail(student.mail, body, dto.getSubject(), replyTo);
                    }
                }

            } else {
                if (count > 0 && sheet == null){
                 //   mailService.sendEmail("barais@irisa.fr", " FinalResult but no sheet for studentId: " + student.id + ", studentname: " + student.name + ", exam.id: " +ex.id, "[CorrectExam] strange behavior", "olivier.barais@gmail.com");
                }
                if (dto.isMailabi() && dto.getSheetuuid() == null) {
                    String body = dto.getBodyabi();
                    body = body.replace("${firstname}", student.firstname);
                    body = body.replace("${lastname}", student.name);
                    mailService.sendEmail(student.mail, body, dto.getSubject(), replyTo);
                }

            }
        });

        if (dto.getSheetuuid() == null) {
            // Results sent to every student: the corrected sheets are published in the student portal too (ADR 0003)
            examService.updateStudentAccess(examId, StudentAccess.GRADED);
        }
        return Response.ok().build();
    }

    @Operation(summary = "Get exam comments", description = "Returns all comments shared for the exam.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "The comments of the exam")
        }
    )
    @GET
    @Path("getComments/{examId}")
    @Transactional
    // @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getComments(@Parameter(description = "ID of the exam", required = true) @PathParam("examId") long examId, @Context SecurityContext ctx) {

        return Response.ok().entity(Comments.findCommentByExamId("" + examId).list()).build();

    }

    @Operation(summary = "Get question labels", description = "Returns the labels of the questions of the exam, indexed by question number.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "The question labels"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @GET
    @Path("getLibelleQuestions/{examId}")
    @Transactional
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getLibelleQuestions(@Parameter(description = "ID of the exam", required = true) @PathParam("examId") long examId, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        List<Question> qs = Question.findQuestionbyExamId(examId).list();
        Map<Integer, String> res = new HashMap<>();
        for (Question q : qs) {
            if (!res.containsKey(q.numero)) {
                if (q.libelle != null) {
                    res.put(q.numero, q.libelle);
                }
            }
        }
        return Response.ok().entity(res).build();

    }

    @Operation(summary = "Show exam results", description = "Computes and returns the results of every student for the exam, including notes per question and absent status.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The results of the students",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = StudentResultDTO.class))
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @GET
    @Path("showResult/{examId}")
    @Transactional
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response showResult(@Parameter(description = "ID of the exam", required = true) @PathParam("examId") long examId, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        Map<Long, FinalResult> finalfinalResultsByStudentId = new HashMap<>();
        Map<ExamSheet, Integer> finalNotes = new HashMap<>();
        Map<ExamSheet, List<StudentResponse>> mapstudentResp = new HashMap<>();
        Exam ex = finalNoteService.computeFinalNote(examId, finalfinalResultsByStudentId, finalNotes, mapstudentResp);

        List<StudentResultDTO> results = new ArrayList<>();
        List<Long> studentsId = new ArrayList<>();
        List<Long> sheetsId = new ArrayList<>();

        // for (Map.Entry<Long, FinalResult> finalResult1 :
        // finalfinalResultsByStudentId.entrySet()) {
        // FinalResult r = finalResult1.getValue();

        for (Map.Entry<ExamSheet, Integer> sheetEntry : finalNotes.entrySet()) {
            // List<ExamSheet> sheets = finalNotes.keySet().stream()
            // .filter(e -> e.students.stream().anyMatch(s -> s.id ==
            // finalResult1.getKey()))
            // .collect(Collectors.toList());
            // if (sheets.size() > 0) {
            // ExamSheet sheet = sheets.get(0);
            // ExamSheet sheet = ExamSheet.findExamSheetByScanAndStudentId(ex.scanfile.id,
            // r.student.id).firstResult();
            var sheet = sheetEntry.getKey();
            Integer r = sheetEntry.getValue();
            String uuid = sheet.name;
            int studentnumber = (sheet.pagemin / (sheet.pagemax - sheet.pagemin + 1)) + 1;
            if (sheet.students != null && sheet.students.size() > 0) {
                for (Student student : sheet.students) {

                    var res = new StudentResultDTO();
                    res.setId(student.id);
                    sheetsId.add(sheet.id);

                    studentsId.add(student.id);
                    res.setNom(student.name);
                    res.setPrenom(student.firstname);
                    res.setIne(student.ine);
                    res.setMail(student.mail);
                    final DecimalFormat df = new DecimalFormat("0.00");
                    res.setNote(df.format(r.doubleValue() / 100.0));
                    res.setUuid(uuid);
                    res.setStudentNumber("" + studentnumber);
                    res.setAbi(0);
                    res.setNotequestions(new HashMap<>());
                    List<StudentResponse> resp = mapstudentResp.get(sheet);

                    resp.forEach(resp1 -> {
                        // log.error("resp1 " + resp1.id);

                        if ("QCM".equals(resp1.question.type.algoName) && resp1.question.step < 0) {
                            res.getNotequestions().put(resp1.question.numero,
                                    df.format(
                                            resp1.quarternote.doubleValue() / 4));

                        } else if (!"QCM".equals(resp1.question.type.algoName) && GradeType.HYBRID.equals(resp1.question.gradeType)) {

                            res.getNotequestions().put(resp1.question.numero,
                                    df.format(
                                            resp1.quarternote.doubleValue() / 400));
                        } else {
                            res.getNotequestions().put(resp1.question.numero,
                                    df.format(
                                            ((resp1.quarternote.doubleValue() * 100.0 / 4) / resp1.question.step)
                                                    / 100.0));

                        }

                    });
                    results.add(res);
                }
            }
            else {
                    var res = new StudentResultDTO();
                    sheetsId.add(sheet.id);
                    studentsId.add(Long.valueOf(-1));
                    res.setNom("anonymous");
                    res.setPrenom("anonymous");
                    res.setIne("anonymous");
                    res.setMail("anonymous@anonymous.fr");
                    final DecimalFormat df = new DecimalFormat("0.00");
                    res.setNote(df.format(r.doubleValue() / 100.0));
                    res.setUuid(uuid);
                    res.setStudentNumber("" + studentnumber);
                    res.setAbi(0);
                    res.setNotequestions(new HashMap<>());
                    List<StudentResponse> resp = mapstudentResp.get(sheet);

                    resp.forEach(resp1 -> {
                        // log.error("resp1 " + resp1.id);

                        if ("QCM".equals(resp1.question.type.algoName) && resp1.question.step < 0) {
                            res.getNotequestions().put(resp1.question.numero,
                                    df.format(
                                            resp1.quarternote.doubleValue() / 4));

                        } else if (GradeType.HYBRID.equals(resp1.question.gradeType)) {
                            if (resp1.quarternote!= null){

                            res.getNotequestions().put(resp1.question.numero,
                                    df.format(
                                            resp1.quarternote.doubleValue() / 400));
                            } else {
                                res.getNotequestions().put(resp1.question.numero,
                                df.format(
                                        0 / 400));

                            }
                        } else {
                            res.getNotequestions().put(resp1.question.numero,
                                    df.format(
                                            ((resp1.quarternote.doubleValue() * 100.0 / 4) / resp1.question.step)
                                                    / 100.0));

                        }

                    });
                    results.add(res);

            }
        }
        // Si la correction n'a démarré pour aucun étudiant, initialise la liste avec un
        // numéro pour la requête
        if (sheetsId.size() == 0) {
            sheetsId.add(Long.valueOf(-1));
        }
        // Etudiant dont la correction n'a pas démarré.
        List<ExamSheet> sheets1 = ExamSheet.getAll4ExamIdNotInStudentIdList(examId, sheetsId).list();
        for (ExamSheet sheet : sheets1) {
            for (Student student : sheet.students) {

                String uuid = sheet.name;
                int studentnumber = (sheet.pagemin / (sheet.pagemax - sheet.pagemin + 1)) + 1;
                var res = new StudentResultDTO();
                res.setId(student.id);
                studentsId.add(student.id);
                sheetsId.add(sheet.id);
                res.setNom(student.name);
                res.setPrenom(student.firstname);
                res.setIne(student.ine);
                res.setMail(student.mail);
                final DecimalFormat df = new DecimalFormat("0.00");
                res.setNote(df.format(0));
                res.setUuid(uuid);
                res.setStudentNumber("" + studentnumber);
                res.setAbi(0);

                res.setNotequestions(new HashMap<>());
                results.add(res);
            }
        }

        Collections.sort(results, new Comparator<StudentResultDTO>() {
            @Override
            public int compare(StudentResultDTO arg0, StudentResultDTO arg1) {
                if (arg0.getNom() == null) {
                    return -1;
                } else {
                    if (arg0.getPrenom() != null && arg0.getNom().equals(arg1.getNom())) {
                        return arg0.getPrenom().compareTo(arg1.getPrenom());

                    } else {
                        return arg0.getNom().compareTo(arg1.getNom());
                    }
                }
            }

        });

        List<Student> studentsAbi = Student.findStudentsAbibyCourseId(ex.course.id, studentsId).list();
        Collections.sort(studentsAbi, new Comparator<Student>() {
            @Override
            public int compare(Student arg0, Student arg1) {
                if (arg0.name == null) {
                    return -1;
                } else {
                    if (arg0.firstname != null && arg0.name.equals(arg1.name)) {
                        return arg0.firstname.compareTo(arg1.firstname);

                    } else {
                        return arg0.name.compareTo(arg1.name);
                    }
                }
            }

        });
        log.error("studentabi" + studentsAbi.size());
        studentsAbi.forEach(student -> {
            var res = new StudentResultDTO();
            res.setId(student.id);
            res.setNom(student.name);
            res.setPrenom(student.firstname);
            res.setIne(student.ine);
            res.setMail(student.mail);
            var frs = FinalResult.findFinalResultByStudentIdAndExamId(student.id, ex.id).list();
            if (frs.size()>0 && frs.get(0).note ==VALUEFORABJ){
                res.setAbi(2);
            }else {
                res.setAbi(1);

            }
            results.add(res);
        });

        return Response.ok().entity(results).build();
    }
}
