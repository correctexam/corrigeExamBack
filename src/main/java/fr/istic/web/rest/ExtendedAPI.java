package fr.istic.web.rest;

import fr.istic.config.JHipsterProperties;
import fr.istic.domain.Answer2HybridGradedComment;
import fr.istic.domain.Comments;
import fr.istic.domain.Course;
import fr.istic.domain.CourseGroup;
import fr.istic.domain.Exam;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.FinalResult;
import fr.istic.domain.GradedComment;
import fr.istic.domain.HybridGradedComment;
import fr.istic.domain.Question;
import fr.istic.domain.Scan;
import fr.istic.domain.Student;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.Template;
import fr.istic.domain.TextComment;
import fr.istic.domain.User;
import fr.istic.domain.enumeration.GradeType;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.Answer2HybridGradedCommentService;
import fr.istic.service.CacheStudentPdfFService;
import fr.istic.service.CacheUploadService;
import fr.istic.service.CourseGroupService;
import fr.istic.service.CourseService;
import fr.istic.service.ExamService;
import fr.istic.service.ExamSheetService;
import fr.istic.service.FichierS3Service;
import fr.istic.service.HybridGradedCommentService;
import fr.istic.service.ImportExportService;
import fr.istic.service.MailService;
import fr.istic.service.QuestionService;
import fr.istic.service.ScanService;
import fr.istic.service.SecurityService;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import fr.istic.service.StudentService;
import fr.istic.service.UserService;
import fr.istic.service.ZoneService;
import fr.istic.service.customdto.Answer4QuestionDTO;
import fr.istic.service.customdto.ClusterDTO;
import fr.istic.service.customdto.ListUserModelShare;
import fr.istic.service.customdto.MailResultDTO;
import fr.istic.service.customdto.StudentMassDTO;
import fr.istic.service.customdto.StudentResultDTO;
import fr.istic.service.customdto.WorstAndBestSolution;
import fr.istic.service.customdto.ZoneSameCommentDTO;
import fr.istic.service.customdto.correctexamstate.MarkingExamStateDTO;
import fr.istic.service.customdto.correctexamstate.QuestionStateDTO;
import fr.istic.service.customdto.correctexamstate.SheetStateDTO;
import fr.istic.service.customdto.exportpdf.ExportPDFDto;
import fr.istic.service.customdto.exportpdf.Gradedcommentspdf;
import fr.istic.service.customdto.exportpdf.Hybridcommentspdf;
import fr.istic.service.customdto.exportpdf.Questionspdf;
import fr.istic.service.customdto.exportpdf.Sheetspdf;
import fr.istic.service.customdto.exportpdf.StudentResponsepdf;
import fr.istic.service.customdto.exportpdf.Studentpdf;
import fr.istic.service.customdto.exportpdf.Textcommentspdf;
import fr.istic.service.customdto.exportpdf.Zonepdf;
import fr.istic.service.dto.Answer2HybridGradedCommentDTO;
import fr.istic.service.dto.CourseDTO;
import fr.istic.service.dto.ExamDTO;
import fr.istic.service.dto.GradedCommentDTO;
import fr.istic.service.dto.HybridGradedCommentDTO;
import fr.istic.service.dto.QuestionDTO;
import fr.istic.service.dto.TextCommentDTO;
import fr.istic.service.mapper.CommentsMapper;
import fr.istic.service.mapper.ExamMapper;
import fr.istic.service.mapper.GradedCommentMapper;
import fr.istic.service.mapper.HybridGradedCommentMapper;
import fr.istic.service.mapper.QuestionMapper;
import fr.istic.service.mapper.TextCommentMapper;
import fr.istic.service.mapper.ZoneMapper;
import fr.istic.web.util.HeaderUtil;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static jakarta.ws.rs.core.UriBuilder.fromPath;


/**
 * REST controller for managing {@link fr.istic.domain.Comments}.
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ExtendedAPI {
    final int VALUEFORABJ = -100000;

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

    @Inject
    ScanService scanService;

    @Inject
    QuestionMapper questionMapper;
    @Inject
    ZoneMapper zoneMapper;
    @Inject
    CommentsMapper commentsMapper;
    @Inject
    GradedCommentMapper gradedCommentMapper;

    @Inject
    TextCommentMapper textCommentMapper;

    @Inject
    ZoneService zoneService;

    @Inject
    ExamMapper examMapper;

    @Inject
    ImportExportService importExportService;

    @Inject
    ExamService examService;

    @Inject
    ExamSheetService examSheetService;

    @Inject
    CacheStudentPdfFService cacheStudentPdfFService;

    @Inject
    Answer2HybridGradedCommentService answer2HybridGradedCommentService;

    @Inject
    HybridGradedCommentService hybridGradedCommentService;

    @Inject
    HybridGradedCommentMapper hybridCommentMapper;

    @Inject
    FichierS3Service fichierS3Service;

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

    @GET
    @Path("exportpdf/{examId}")
    @Transactional
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getExportPdfRoute(@PathParam("examId") long examId, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        ExportPDFDto exportPDFDto = this.getExportPdf(examId, null);
        return Response.ok().entity(exportPDFDto).build();
    }

    @GET
    @Path("exportpdf4sheet/{examId}/{sheetuuid}")
    @Transactional
    public Response getExportPdfRoute4sheet(@PathParam("examId") long examId,
            @PathParam("sheetuuid") String sheetname) {
        ExportPDFDto exportPDFDto = this.getExportPdf(examId, sheetname);
        return Response.ok().entity(exportPDFDto).build();
    }

    public ExportPDFDto getExportPdf(long examId, String sheetname) {
        this.computeFinalNote(examId, new HashMap<>(), new HashMap<>(), new HashMap<>());

        ExportPDFDto exportPDFDto = new ExportPDFDto();
        Exam ex = Exam.findById(examId);
        if (ex.firstnamezone != null) {
            exportPDFDto.setFirstnamezonepdf(new Zonepdf());
            exportPDFDto.getFirstnamezonepdf().setHeight(ex.firstnamezone.height);
            exportPDFDto.getFirstnamezonepdf().setWidth(ex.firstnamezone.width);
            exportPDFDto.getFirstnamezonepdf().setXInit(ex.firstnamezone.xInit);
            exportPDFDto.getFirstnamezonepdf().setYInit(ex.firstnamezone.yInit);
            exportPDFDto.getFirstnamezonepdf().setPageNumber(ex.firstnamezone.pageNumber);
        }
        if (ex.namezone != null) {
            exportPDFDto.setNamezonepdf(new Zonepdf());
            exportPDFDto.getNamezonepdf().setHeight(ex.namezone.height);
            exportPDFDto.getNamezonepdf().setWidth(ex.namezone.width);
            exportPDFDto.getNamezonepdf().setXInit(ex.namezone.xInit);
            exportPDFDto.getNamezonepdf().setYInit(ex.namezone.yInit);
            exportPDFDto.getNamezonepdf().setPageNumber(ex.namezone.pageNumber);
        }
        exportPDFDto.setID(ex.id);
        exportPDFDto.setScanfileID(ex.scanfile.id);
        exportPDFDto.setName(ex.name);

        List<ExamSheet> sheets = null;
        List<StudentResponse> studentResp = null;
        if (sheetname != null) {
            sheets = ExamSheet.findExamSheetByName(sheetname).list();
            studentResp = StudentResponse.getAllStudentResponseWithexamIdAndSheetName(examId, sheetname).list();
        } else {
            sheets = ExamSheet.getAll4ExamId(examId).list();
            studentResp = StudentResponse.getAllStudentResponseWithexamId(examId).list();

        }

        Map<ExamSheet, List<StudentResponse>> mapstudentResp = new HashMap<>();
        sheets.forEach((sh) -> mapstudentResp.put(sh, new ArrayList<>()));

        Map<ExamSheet, List<StudentResponse>> mapstudentResp1 = studentResp.stream()
                .collect(Collectors.groupingBy(StudentResponse::getCSheet));
        mapstudentResp1.forEach((sheet, responses) -> {
            mapstudentResp.get(sheet).addAll(responses);
        });
        List<Sheetspdf> sheetPdfs = new ArrayList<Sheetspdf>();
        exportPDFDto.setSheetspdf(sheetPdfs);
        mapstudentResp.forEach((sheet, responses) -> {
            Sheetspdf sheetpdf = new Sheetspdf();
            sheetPdfs.add(sheetpdf);
            sheetpdf.setName(sheet.name);
            sheetpdf.setID(sheet.id);
            if (sheet.students.size() > 0) {
                var studentId = sheet.students.iterator().next().id;
                FinalResult fr = FinalResult
                        .findFinalResultByStudentIdAndExamId(studentId, examId).firstResult();

                if (fr != null) {
                    sheetpdf.setFinalresult(fr.note);
                } else {
                    sheetpdf.setFinalresult(0);
                }
            } else {
                sheetpdf.setFinalresult(0);
            }
            sheetpdf.setName(sheet.name);
            sheetpdf.setPagemin(sheet.pagemin);
            sheetpdf.setPagemax(sheet.pagemax);
            sheetpdf.setStudentpdf(new ArrayList<>());
            sheet.students.forEach((st) -> {
                Studentpdf stpdf = new Studentpdf();
                stpdf.setFirstname(st.firstname);
                stpdf.setName(st.name);
                stpdf.setID(st.id);
                stpdf.setIne(st.ine);
                stpdf.setMail(st.mail);
                sheetpdf.getStudentpdf().add(stpdf);
            });
            if (sheet.students.size()==0){
                Studentpdf stpdf = new Studentpdf();
                stpdf.setFirstname("anonymous");
                stpdf.setName("anonymous");
                stpdf.setID(-1);
                stpdf.setIne("anonymous");
                stpdf.setMail("anonymous@anonymous.fr");
                sheetpdf.getStudentpdf().add(stpdf);
            }
            sheetpdf.setStudentResponsepdf(new ArrayList<>());
            responses.forEach((resp) -> {
                StudentResponsepdf stpdf = new StudentResponsepdf();

                stpdf.setID(resp.id);
                stpdf.setNote(resp.quarternote * 1 / 4);
                stpdf.setQuestionID(resp.question.id);
                stpdf.setQuestionNumero("" + resp.question.numero);
                if (resp.star != null) {
                    stpdf.setStar(resp.star);
                } else {
                    stpdf.setStar(false);
                }
                if (resp.worststar != null) {
                    stpdf.setWorststar(resp.worststar);
                } else {
                    stpdf.setWorststar(false);
                }
                sheetpdf.getStudentResponsepdf().add(stpdf);
                List<Answer2HybridGradedComment> ans = Answer2HybridGradedComment
                        .findAllAnswerHybridGradedCommentByAnswerId(resp.id).list();
                stpdf.setHybridcommentspdf(new ArrayList<>());

                ans.forEach(hc -> {
                    Hybridcommentspdf hcpdf = new Hybridcommentspdf();
                    hcpdf.setDescription(hc.hybridcomments.description);
                    hcpdf.setText(hc.hybridcomments.text);
                    hcpdf.setRelative(hc.hybridcomments.relative);
                    hcpdf.setStepMax(hc.hybridcomments.step);
                    hcpdf.setStepValue(hc.stepValue);
                    hcpdf.setGrade(hc.hybridcomments.grade / 4.0);
                    if (hc.studentResponse.quarternote != null) {
                        hcpdf.setAnswerGrade(hc.studentResponse.quarternote.doubleValue() / 400.0);
                    }

                    stpdf.getHybridcommentspdf().add(hcpdf);
                });
                stpdf.setGradedcommentspdf(new ArrayList<>());
                resp.gradedcomments.forEach(gc -> {
                    Gradedcommentspdf gcpdf = new Gradedcommentspdf();
                    gcpdf.setDescription(gc.description);
                    gcpdf.setText(gc.text);
                    gcpdf.setZonegeneratedid(gc.zonegeneratedid);
                    gcpdf.setGrade(gc.gradequarter * 1.0 / 4);
                    stpdf.getGradedcommentspdf().add(gcpdf);
                });
                stpdf.setTextcommentspdf(new ArrayList<>());
                resp.textcomments.forEach(gc -> {
                    Textcommentspdf gcpdf = new Textcommentspdf();
                    gcpdf.setDescription(gc.description);
                    gcpdf.setText(gc.text);
                    stpdf.getTextcommentspdf().add(gcpdf);
                });
            });
        });
        exportPDFDto.setQuestionspdf(new ArrayList<>());
        ex.questions.forEach(q -> {
            Questionspdf qpdf = new Questionspdf();
            qpdf.setGradeType(q.gradeType.name());
            qpdf.setID(q.id);
            qpdf.setNumero(q.numero);
            qpdf.setPoint(q.quarterpoint * 1.0 / 4);
            qpdf.setStep(q.step);
            qpdf.setTypeAlgoName(q.type.algoName);
            qpdf.setTypeID(q.type.id);
            qpdf.setZonepdf(new Zonepdf());
            qpdf.getZonepdf().setHeight(q.zone.height);
            qpdf.getZonepdf().setWidth(q.zone.width);
            qpdf.getZonepdf().setXInit(q.zone.xInit);
            qpdf.getZonepdf().setYInit(q.zone.yInit);
            qpdf.getZonepdf().setPageNumber(q.zone.pageNumber);
            exportPDFDto.getQuestionspdf().add(qpdf);

        });
        return exportPDFDto;
    }

    @Transactional
    public Exam computeFinalNote(long examId, Map<Long, FinalResult> finalfinalResultsByStudentId,
            Map<ExamSheet, Integer> finalNotes, Map<ExamSheet, List<StudentResponse>> mapstudentResp1) {
        List<StudentResponse> studentResp = StudentResponse.getAllStudentResponseWithexamIdWithOrphanId(examId).list();

        Map<ExamSheet, List<StudentResponse>> mapstudentResp = studentResp.stream()
                .collect(Collectors.groupingBy(StudentResponse::getCSheet));

        Exam ex = Exam.findById(examId);

        mapstudentResp.forEach((sh, resps) -> {
            mapstudentResp1.put(sh, resps);

            boolean hasRealSheet = true;

            var finalnote = 0;
            for (StudentResponse resp : resps) {
                hasRealSheet = true;
                if (sh != null && sh.pagemin != -1 && sh.pagemax != -1) {

                    if (resp.question.gradeType == GradeType.DIRECT && !"QCM".equals(resp.question.type.algoName)) {
                        if (resp.question.step > 0) {
                            finalnote = finalnote + ((resp.quarternote * 100 / 4) / resp.question.step);
                        }
                    } else if (resp.question.gradeType == GradeType.POSITIVE
                            && !"QCM".equals(resp.question.type.algoName)) {
                        int currentNote = 0;
                        for (var g : resp.gradedcomments) {
                            if (g.gradequarter != null) {
                                currentNote = currentNote + g.gradequarter;
                            }
                        }

                        if (currentNote > (resp.question.quarterpoint) * resp.question.step) {
                            currentNote = (resp.question.quarterpoint) * resp.question.step;
                        }
                        if (currentNote != resp.quarternote) {
                            resp.quarternote = currentNote;
                            StudentResponse.update(resp);
                        }
                        if (resp.question.step > 0) {
                            finalnote = finalnote + (currentNote * 100 / 4 / resp.question.step);
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
                            finalnote = finalnote + (currentNote * 100 / 4 / resp.question.step);
                        }

                    } else if (resp.question.gradeType == GradeType.HYBRID
                            && !"QCM".equals(resp.question.type.algoName)) {
                        this.computeNote4Hybrid(resp);

                        // resp.persistOrUpdate();
                        finalnote = finalnote + (resp.quarternote / 4);

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
                            finalnote = finalnote + (currentNote * 100 / 4 / resp.question.step);

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
                        finalnote = finalnote + (currentNote * 100 / 4);
                    }
                } else {
                    hasRealSheet = false;
                }
            }
            final var finalnote1 = finalnote;
            final var hasRealSheet1 = hasRealSheet;
            if (!hasRealSheet1) {
                sh.students.forEach(student -> {
                    var q = FinalResult.findFinalResultByStudentIdAndExamId(student.id, examId);
                    long count = q.count();
                    var fr = q.list();
                    // For managing ABJ
                    if (count > 0 && fr.get(0).note != VALUEFORABJ) {
                        FinalResult.deleteById(q.firstResult().id);
                    } else  if (count > 0 && fr.get(0).note == VALUEFORABJ) {
                        finalfinalResultsByStudentId.put(student.id, fr.get(0));
                    }
                });
            } else {
                finalNotes.put(sh, finalnote1);
            }
        });
        // Map<Long,FinalResult> finalfinalResultsByStudentId = new HashMap<>();
        List<FinalResult> finalResults = FinalResult.getAll4ExamId(examId).list();
        Map<Long, List<FinalResult>> finalResultsByStudentId = finalResults.stream()
                .collect(Collectors.groupingBy(FinalResult::getStudentID));
        for (Map.Entry<ExamSheet, Integer> finalNote : finalNotes.entrySet()) {
            for (Student s : finalNote.getKey().students) {
                if (finalResultsByStudentId.containsKey(s.id)) {
                    for (FinalResult fr : finalResultsByStudentId.get(s.id)) {
                        if (fr.note != finalNote.getValue()) {
                            fr.note = finalNote.getValue();
                            fr = FinalResult.update(fr);
                            finalfinalResultsByStudentId.put(s.id, fr);
                        } else {
                            finalfinalResultsByStudentId.put(s.id, fr);

                        }
                    }
                } else {
                    FinalResult r = new FinalResult();
                    r.student = s;
                    r.exam = ex;
                    r.note = finalNote.getValue();
                    r = FinalResult.persistOrUpdate(r);
                    finalfinalResultsByStudentId.put(s.id, r);
                }
            }
        }
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

        this.computeFinalNote(examId, new HashMap<>(), new HashMap<>(), new HashMap<>());
        return Response.ok().build();
    }

    @PUT
    @Path("toggleAsAbJ/{studentId}/{examId}/{abi}")
    @Transactional
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response toggleAsAbJ(@PathParam("studentId") long studentId, @PathParam("examId") long examId, @PathParam("abi") long abi, @Context SecurityContext ctx) {
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

    @POST
    @Path("sendResult/{examId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response sendResultToStudent(MailResultDTO dto, @PathParam("examId") long examId,
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

        Exam ex = this.computeFinalNote(examId, finalfinalResultsByStudentId, finalNotes, mapstudentResp);

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
                    mailService.sendEmail("barais@irisa.fr", " FinalResult but no sheet for studentId: " + student.id + ", studentname: " + student.name + ", exam.id: " +ex.id, "[CorrectExam] strange behavior", "olivier.barais@gmail.com");
                }
                if (dto.isMailabi() && dto.getSheetuuid() == null) {
                    String body = dto.getBodyabi();
                    body = body.replace("${firstname}", student.firstname);
                    body = body.replace("${lastname}", student.name);
                    mailService.sendEmail(student.mail, body, dto.getSubject(), replyTo);
                }

            }
        });

        return Response.ok().build();
    }

    @GET
    @Path("getComments/{examId}")
    @Transactional
    // @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getComments(@PathParam("examId") long examId, @Context SecurityContext ctx) {

        return Response.ok().entity(Comments.findCommentByExamId("" + examId).list()).build();

    }

    @GET
    @Path("getLibelleQuestions/{examId}")
    @Transactional
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getLibelleQuestions(@PathParam("examId") long examId, @Context SecurityContext ctx) {
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

    @GET
    @Path("showResult/{examId}")
    @Transactional
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response showResult(@PathParam("examId") long examId, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        Map<Long, FinalResult> finalfinalResultsByStudentId = new HashMap<>();
        Map<ExamSheet, Integer> finalNotes = new HashMap<>();
        Map<ExamSheet, List<StudentResponse>> mapstudentResp = new HashMap<>();
        Exam ex = this.computeFinalNote(examId, finalfinalResultsByStudentId, finalNotes, mapstudentResp);

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
            st1.groups.add(groupesentities.get(s.getGroupe()));
            groupesentities.get(s.getGroupe()).students.add(st1);
            Student.flush();
            CourseGroup.flush();

        });

        groupesentities.values().forEach(gc -> {
            CourseGroup.persistOrUpdate(gc);
            CourseGroup.flush();
        });

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
    @Path("/uploadExportFinalStudent/{examId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response fileUploadStudentPdf(@MultipartForm MultipartFormDataInput input,
            @PathParam("examId") long examId) {
        try {
            cacheStudentPdfFService.uploadFile(input, examId);
        } catch (Exception e) {
            return Response.serverError().build();

        }
        return Response.ok().build();
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

    @POST
    @Path("/uploadScan/{scanId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response scanUpload(@MultipartForm MultipartFormDataInput input, @PathParam("scanId") long scanId,
            @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, scanId, Scan.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        try {
            scanService.uploadFile(input, scanId, false);
        } catch (Exception e) {
            return Response.serverError().build();

        }
        return Response.ok().build();
    }

    @POST
    @Path("/uploadAndMergeScan/{scanId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response scanAndMergeUpload(@MultipartForm MultipartFormDataInput input, @PathParam("scanId") long scanId,
            @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, scanId, Scan.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        try {
            scanService.uploadFile(input, scanId, true);
        } catch (Exception e) {
            return Response.serverError().build();
        }
        return Response.ok().build();
    }

    @GET
    @Path("/getCachePageInTemplate/{examId}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getCachePageInTemplate(@PathParam("examId") long examId) {
        try {
            return Response
                    .status(Response.Status.OK)
                    .entity(cacheUploadService.getCachePageInTemplate(examId))
                    .type(MediaType.TEXT_PLAIN)
                    .build();

        } catch (Exception e) {
            return Response.serverError().build();

        }
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
    /*
     * @GET
     *
     * @Path("/getCacheAlignPageSqlite/{examId}/{pageId}")
     *
     * @Produces(MediaType.TEXT_PLAIN)
     * public Response getCacheAlignPageSqlite(@PathParam("examId") long
     * examId, @PathParam("pageId") int pageId) {
     * try {
     * return Response
     * .status(Response.Status.OK)
     * .entity(cacheUploadService.getAlignPageSqlite(examId, pageId, false))
     * .type(MediaType.TEXT_PLAIN)
     * .build();
     *
     * } catch (Exception e) {
     * return Response.serverError().build();
     *
     * }
     * }
     */

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

    /*
     * @GET
     *
     * @Path("/getCacheNonAlignPageSqlite/{examId}/{pageId}")
     *
     * @Produces(MediaType.TEXT_PLAIN)
     * public Response getCachePageNoAlignSqlite(@PathParam("examId") long
     * examId, @PathParam("pageId") int pageId) {
     * try {
     * return Response
     * .status(Response.Status.OK)
     * .entity(cacheUploadService.getAlignPageSqlite(examId, pageId, true))
     * .type(MediaType.TEXT_PLAIN)
     * .build();
     *
     *
     * } catch (Exception e) {
     * return Response.serverError().build();
     *
     * }
     * }
     */

    @GET
    @Path("/getCache/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile(@PathParam("fileName") String fileName) {
        try {
            return Response.ok(
                    new StreamingOutput() {
                        @Override
                        public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                            InputStream source = null;
                            try {
                                source = cacheUploadService.getFile(fileName);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return;
                            }
                            byte[] buf = new byte[8192];
                            int length;
                            while ((length = source.read(buf)) != -1) {
                                outputStream.write(buf, 0, length);
                            }
                        }
                    }, MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment;filename=" + fileName).build();
        } catch (Exception e) {

            e.printStackTrace();
            return Response.noContent().build();
        }
    }

    @GET
    @Path("/exportCourse/{courseId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response exportCourse(@PathParam("courseId") long courseId, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, courseId, Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        try {
            return Response.ok(
                    new StreamingOutput() {
                        @Override
                        public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                            OutputStreamWriter w = new OutputStreamWriter(outputStream);
                            Gson gson = new GsonBuilder().create();
                            gson.toJson(importExportService.export(courseId, true, -1), w);
                            w.flush();
                            w.close();
                        };

                    }, MediaType.APPLICATION_OCTET_STREAM)

                    .header("Content-Disposition", "attachment;filename=" + courseId + ".json")
                    .build();
        } catch (Exception e) {

            e.printStackTrace();
            return Response.noContent().build();
        }
    }

    @GET
    @Path("/exportExam/{courseId}/{examId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response exportExam(@PathParam("courseId") long courseId, @PathParam("examId") long examId,
            @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, courseId, Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        try {
            return Response.ok(

                    new StreamingOutput() {
                        @Override
                        public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                            OutputStreamWriter w = new OutputStreamWriter(outputStream);
                            Gson gson = new GsonBuilder().create();
                            gson.toJson(importExportService.export(courseId, true, examId), w);
                            w.flush();
                            w.close();
                        };

                    }, MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment;filename=" + courseId + ".json")
                    .build();
        } catch (Exception e) {

            e.printStackTrace();
            return Response.noContent().build();
        }
    }

    @GET
    @Path("/getTemplatePdf/{templateId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getTemplate(@PathParam("templateId") long templateId, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, templateId, Template.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        try {
            if (this.fichierS3Service.isObjectExist("template/" + templateId + ".pdf")) {

                return Response.ok(
                        new StreamingOutput() {
                            @Override
                            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                                InputStream source = null;
                                try {

                                    source = fichierS3Service.getObject("template/" + templateId + ".pdf");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return;
                                }
                                byte[] buf = new byte[8192];
                                int length;
                                while ((length = source.read(buf)) != -1) {
                                    outputStream.write(buf, 0, length);
                                }
                            }
                        }, MediaType.APPLICATION_OCTET_STREAM)
                        .header("Content-Disposition", "attachment;filename=" + "template_" + templateId + ".pdf")
                        .build();
            } else {
                return Response.noContent().build();

            }
        }

        catch (Exception e) {

            e.printStackTrace();
            return Response.noContent().build();
        }
    }

    @GET
    @Path("/getScanPdf/{scanId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    // @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getScan(@PathParam("scanId") long scanId, @Context SecurityContext ctx) {
        /*
         * if (!securityService.canAccess(ctx, scanId, Scan.class)) {
         * return Response.status(403,
         * "Current user cannot access to this ressource").build();
         * }
         */
        try {
            if (this.fichierS3Service.isObjectExist("scan/" + scanId + ".pdf")) {

                return Response.ok(
                        new StreamingOutput() {
                            @Override
                            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                                InputStream source = null;
                                try {

                                    source = fichierS3Service.getObject("scan/" + scanId + ".pdf");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return;
                                }
                                byte[] buf = new byte[8192];
                                int length;
                                while ((length = source.read(buf)) != -1) {
                                    outputStream.write(buf, 0, length);
                                }
                            }
                        }, MediaType.APPLICATION_OCTET_STREAM)
                        .header("Content-Disposition", "attachment;filename=" + "scan_" + scanId + ".pdf")
                        .build();
            } else {
                return Response.noContent().build();

            }
        }

        catch (Exception e) {

            e.printStackTrace();
            return Response.noContent().build();
        }
    }

    @GET
    @Path("/exportCourseWithoutStudentData/{courseId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })

    public Response exportCourseWithoutStudentData(@PathParam("courseId") long courseId, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, courseId, Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        try {
            return Response.ok(
                    new StreamingOutput() {
                        @Override
                        public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                            OutputStreamWriter w = new OutputStreamWriter(outputStream);
                            Gson gson = new GsonBuilder().create();
                            gson.toJson(importExportService.export(courseId, false, -1), w);
                            w.flush();
                            w.close();
                        };

                    }, MediaType.APPLICATION_OCTET_STREAM)

                    .header("Content-Disposition", "attachment;filename=" + courseId + ".json")
                    .build();
        } catch (Exception e) {

            e.printStackTrace();
            return Response.noContent().build();
        }
    }

    @GET
    @Path("/exportExamWithoutStudentData/{courseId}/{examId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response exportExamWithoutStudentData(@PathParam("courseId") long courseId, @PathParam("examId") long examId,
            @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, courseId, Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        try {
            return Response.ok(
                    new StreamingOutput() {
                        @Override
                        public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                            OutputStreamWriter w = new OutputStreamWriter(outputStream);
                            Gson gson = new GsonBuilder().create();
                            gson.toJson(importExportService.export(courseId, false, examId), w);
                            w.flush();
                            w.close();
                        };

                    }, MediaType.APPLICATION_OCTET_STREAM)

                    .header("Content-Disposition", "attachment;filename=" + courseId + ".json")
                    .build();
        } catch (Exception e) {

            e.printStackTrace();
            return Response.noContent().build();
        }
    }

    @POST
    @Path("/importCourse")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })

    public Response importCourse(@MultipartForm MultipartFormDataInput input, @Context SecurityContext ctx) {
        var userLogin = Optional
                .ofNullable(ctx.getUserPrincipal().getName());
        if (!userLogin.isPresent()) {
            throw new AccountResourceException("Current user login not found");
        }
        var user = User.findOneByLogin(userLogin.get());
        if (!user.isPresent()) {
            throw new AccountResourceException("User could not be found");
        }
        if (!userLogin.equals("system")) {
            try {
                CourseDTO dto = importExportService.importCourse(input, user.get(), true);
                if (dto != null) {
                    return Response.ok().entity(dto).build();
                } else {
                    return Response.noContent().build();

                }
            } catch (Exception e) {
                return Response.serverError().build();

            }
        }
        throw new AccountResourceException("User could not be found");
    }

    @POST
    @Path("/importCourseWithoutStudentData")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })

    public Response importCourseWithoutStudentData(@MultipartForm MultipartFormDataInput input,
            @Context SecurityContext ctx) {
        var userLogin = Optional
                .ofNullable(ctx.getUserPrincipal().getName());
        if (!userLogin.isPresent()) {
            throw new AccountResourceException("Current user login not found");
        }
        var user = User.findOneByLogin(userLogin.get());
        if (!user.isPresent()) {
            throw new AccountResourceException("User could not be found");
        }
        if (!userLogin.equals("system")) {
            try {
                CourseDTO dto = importExportService.importCourse(input, user.get(), false);
                if (dto != null) {
                    return Response.ok().entity(dto).build();
                } else {
                    return Response.noContent().build();

                }
            } catch (Exception e) {
                return Response.serverError().build();

            }
        }
        throw new AccountResourceException("User could not be found");
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
     * {@code GET /users} : get all users.
     *
     * @param pagination the pagination information.
     * @return the {@link Response} with status {@code 200 (OK)} and with body all
     *         users.
     */
    @GET
    @Path("/getAllEmailProfs4course/{courseId}")
    public Response getAllEmailProfs4course(@PathParam("courseId") long courseId) {
        var res = this.courseService.getAllProfMail(courseId);
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

    @Transactional
    void deleteOphanExamSheet(long examId) {
        List<ExamSheet> list = ExamSheet.getAllOrphan4ExamId(examId).list();
        if (list.size() > 0) {
            for (ExamSheet examSheet : list) {
                if (StudentResponse.findStudentResponsesbysheetId(examSheet.id).count() == 0) {
                    if (ExamSheet.getAllDouble4Same(examSheet.id).count() > 0) {
                        examSheetService.delete(examSheet.id);
                    }
                    if (examSheet.pagemin == -1 && examSheet.pagemax == -1) {
                        if (examSheet.students.size() > 0) {
                            List<Student> students = new ArrayList<Student>(examSheet.students);
                            for (Student student : students) {
                                student.examSheets.remove(examSheet);
                                examSheet.students.remove(student);
                                studentService.persistOrUpdate(student);
                            }
                        }
                        examSheetService.delete(examSheet.id);
                    }
                }
                ;
            }

        }
    }

    @DELETE()
    @Path("/cleanExamSheet/{examId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response cleanAllFalseSheet(@PathParam("examId") long examId, @Context UriInfo uriInfo,
            @Context SecurityContext ctx) {
        log.debug("REST request to clean all Questions and zone : {}");
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        this.deleteOphanExamSheet(examId);

        ExamSheet.getEntityManager().clear();
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, "exam", "-1")
                .forEach(response::header);
        return response.build();
    }

    @DELETE()
    @Path("/cleanExam/{examId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response cleanAllQuestionZone4Exam(@PathParam("examId") long examId, @Context UriInfo uriInfo,
            @Context SecurityContext ctx) {
        log.debug("REST request to clean all Questions and zone : {}");
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        Optional<Exam> optEx = Exam.findByIdOptional(examId);

        if (optEx.isPresent()) {
            examService.deleteQuestionCommentAndZone(examId);
            /*
             * List<Zone> zones = Zone.getQuestionZone4ExamId(examId).list();
             * zones.forEach(z -> {
             * this.zoneService.delete(z.id);
             * });
             *
             * Exam ex = optEx.get();
             * if (ex.idzone != null){
             * this.zoneService.delete(ex.idzone.id);
             * }
             * if (ex.namezone != null){
             *
             * this.zoneService.delete(ex.namezone.id);
             * }
             * if (ex.firstnamezone != null){
             * this.zoneService.delete(ex.firstnamezone.id);
             * }
             * if (ex.notezone != null){
             * this.zoneService.delete(ex.notezone.id);
             * }
             */
            ExamDTO result = this.examMapper.toDto(optEx.get());

            var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build())
                    .entity(result);
            HeaderUtil.createEntityCreationAlert(applicationName, true, "exam", result.id.toString())
                    .forEach(response::header);
            return response.build();

        } else {
            var response = Response.noContent();
            HeaderUtil.createEntityDeletionAlert(applicationName, true, "exam", "-1")
                    .forEach(response::header);
            return response.build();
        }

    }

    @DELETE
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Path("/deleteAnswerAndUnsetComment/{studentResponseId}")
    @Transactional
    public Response deleteAnswerAndUnsetComment(@PathParam("studentResponseId") long studentResponseId,
            @Context UriInfo uriInfo,
            @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, studentResponseId, StudentResponse.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        Optional<StudentResponse> sr = StudentResponse.findByIdOptional(studentResponseId);
        if (sr.isPresent()) {
            sr.get().gradedcomments.forEach(gc -> {

                gc.studentResponses.remove(sr.get());
                gc.persistOrUpdate();
            });
            sr.get().textcomments.forEach(tc -> {

                tc.studentResponses.remove(sr.get());
                tc.persistOrUpdate();
            });
            sr.get().clearComments();
            this.answer2HybridGradedCommentService.deleteAllAnswerHybridGradedCommentByAnswerId(sr.get().id);
            sr.get().delete();
        }
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, "studentResponse", "-1")
                .forEach(response::header);
        return response.build();
    }

    @DELETE
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Path("/deleteAllAnswerAndComment/{examId}")
    @Transactional
    public Response deleteAllAnswerAndComment(@PathParam("examId") long examId, @Context UriInfo uriInfo,
            @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        Optional<Exam> ex = Exam.findByIdOptional(examId);
        if (ex.isPresent()) {
            List<Question> qs = Question.findQuestionbyExamId(examId).list();

            for (Question question : qs) {

                List<GradedComment> gradeComment = new ArrayList<GradedComment>();
                List<TextComment> textComments = new ArrayList<TextComment>();
                questionService.updateCorrectionAndAnswer(question, gradeComment, textComments);
                List<Long> gradeCommentids = gradeComment.stream().map(gc -> gc.id).collect(Collectors.toList());
                List<Long> textCommentsids = textComments.stream().map(gc -> gc.id).collect(Collectors.toList());
                questionService.deleteComments(gradeCommentids, textCommentsids);
                List<StudentResponse> srs = StudentResponse.findAllByQuestionId(question.id).list();
                for (StudentResponse studentResponse : srs) {
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
    @Path("/getExamStatusFinish/{examId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getExamStatusFinish(@PathParam("examId") final long examId, @Context final UriInfo uriInfo,
            @Context final SecurityContext ctx) {

        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access this ressource").build();
        }

        final List<StudentResponse> stdResponses = StudentResponse.getAll4ExamId(examId).list();
        final Map<Long, List<StudentResponse>> byQuestion = stdResponses.stream()
                .collect(Collectors.groupingBy(StudentResponse::getQuestionNumero));

        var nbreSheet = ExamSheet.getAll4ExamId(examId).count();


        var res = byQuestion.keySet().stream().allMatch(e-> byQuestion.get(e).size()==nbreSheet);

        if (nbreSheet ==0 || stdResponses.size()==0){
            res = false;
        }

        return Response.ok(res).build();
    }

    /**
     * Provides summary data of a given exam.
     *
     * @param examId The ID of the exam
     */
    @GET
    @Path("/getExamStatus/{examId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getExamStatus(@PathParam("examId") final long examId, @Context final UriInfo uriInfo,
            @Context final SecurityContext ctx) {

        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access this ressource").build();
        }

        final MarkingExamStateDTO result = new MarkingExamStateDTO();
        final Exam exam = Exam.findById(examId);
        final List<StudentResponse> stdResponses = StudentResponse.getAll4ExamId(examId).list();
        final List<Question> questionsExam = Question.findQuestionbyExamId(examId).list();
        final Map<Long, List<StudentResponse>> byQuestion = stdResponses.stream()
                .collect(Collectors.groupingBy(StudentResponse::getQuestionNumero));

        result.setNameExam(exam.name);

        // Populate initial questions

        // ExamSheetID
        Map<Long, QuestionStateDTO> q = new LinkedHashMap<>();
        questionsExam.sort(new Comparator<Question>() {

            @Override
            public int compare(Question arg0, Question arg1) {
                return arg0.numero - arg1.numero;
            }

        });

        for (Question quest : questionsExam) {
            // if (!q.containsKey(quest.numero)){
            final var res = new QuestionStateDTO();
            res.setAnsweredSheets(0);
            res.setFirstUnmarkedSheet(0);
            res.setId(quest.id);
            res.setNumero(quest.numero);
            res.setLibelle(quest.libelle);
            if (!q.containsKey(quest.numero.longValue())) {
                q.put(quest.numero.longValue(), res);
            }
            // result.getQuestions().add(res);
            // }
        }

        // Populate initial sheets

        // ExamSheetID
        Map<Long, SheetStateDTO> s = new LinkedHashMap<>();
        List<ExamSheet> sheets = exam.scanfile.sheets.stream().collect(Collectors.toList());
        sheets.sort(new Comparator<ExamSheet>() {

            @Override
            public int compare(ExamSheet arg0, ExamSheet arg1) {
                return arg0.pagemin - arg1.pagemin;
            }

        });

        for (ExamSheet sh : sheets) {
            if (/* sh.students != null && sh.students.size() > 0 && */ sh.pagemin != -1 && sh.pagemax != -1) {
                final var res = new SheetStateDTO();
                res.setAnsweredSheets(0);
                res.setFirstUnmarkedQuestion(1);
                res.setId(sh.id);
                s.put(sh.id, res);
                result.getSheets().add(res);
            } else if (sh.pagemin == -1 || sh.pagemax == -1) {
                // log.error("sheet not linked with a real pdf");
            } else {
                // log.error("sheet without student");

            }
        }

        // The ID of all the sheet. Used to find the first sheet that has a given
        // question not answered yet

        // Filling the questions part of the DTO
        for (Question quest : questionsExam) {
            // The responses for this question
            final List<StudentResponse> _responsesForQ = byQuestion.computeIfAbsent(quest.numero.longValue(),
                    i -> new ArrayList<>());
            final List<StudentResponse> responsesForQ = _responsesForQ.stream()
                    .filter(r -> r.sheet != null && r.sheet.pagemin != -1 && r.sheet.pagemax != -1)
                    .collect(Collectors.toList());
            // Getting the ID of the sheets that have an answer for this question
            responsesForQ.sort(new ComparatorImplementation());
            QuestionStateDTO qs = q.get(quest.numero.longValue());
            if (responsesForQ.size() > 0 && responsesForQ.get(0).sheet.pagemin == 0) {
                if (responsesForQ.size() == 1) {
                    qs.setFirstUnmarkedSheet(Long.valueOf(responsesForQ.get(0).sheet.pagemax + 1));
                }
                for (int i = 0; i < responsesForQ.size() - 1; i++) {
                    StudentResponse sl1 = responsesForQ.get(i);
                    StudentResponse sl2 = responsesForQ.get(i + 1);
                    qs.setFirstUnmarkedSheet(Long.valueOf(sl1.sheet.pagemax + 1));
                    if (sl1.sheet.pagemax + 1 < sl2.sheet.pagemin) {
                        break;
                    } else if (i == responsesForQ.size() - 2) {
                        qs.setFirstUnmarkedSheet(Long.valueOf(sl2.sheet.pagemax + 1));
                    }
                }
            }
            qs.setAnsweredSheets(responsesForQ.size());
        }

        /*
         * List<QuestionStateDTO> toRemove = q.values().stream().filter(q2 -> {
         * return q.values().stream()
         * .anyMatch(q1 -> q1 != q2 && q1.getNumero() == q2.getNumero()
         * && (q2.getAnsweredSheets() < q1.getAnsweredSheets()
         * || (q2.getAnsweredSheets() <= q1.getAnsweredSheets() && q2.getId() >
         * q1.getId())));
         * }).collect(Collectors.toList());
         *
         * for (QuestionStateDTO tor : toRemove) {
         * q.remove(tor.getId());
         * }
         */
        result.getQuestions().addAll(q.values());
        /*   */

        // Filling the sheet part of the DTO

        /*
         * final Map<Set<Long>, List<StudentResponse>> byStudent = stdResponses
         * .stream()
         * .collect(Collectors.groupingBy(resp -> Set.copyOf(resp.getStudentId())));
         */

        stdResponses.sort(new ComparatorImplementation());

        Map<Long, List<StudentResponse>> byStudent = stdResponses.stream()
                .collect(Collectors.groupingBy(StudentResponse::getSheetId));
        /*
         * var students = new HashMap<Long, List<StudentResponse>>();
         * byStudent.entrySet().stream().forEach(e-> {
         * for (long id : e.getKey()){
         * List<StudentResponse> sts = students.getOrDefault(id, new
         * ArrayList<StudentResponse>());
         * sts.addAll(e.getValue());
         * if (!students.containsKey(id)){
         * students.put(id,sts);
         * }
         * }
         * });
         */
        /*
         * byStudent.values().stream().forEach(std -> {
         * std.sort(new ComparatorImplementation2());
         *
         * });
         * byStudent.entrySet().stream().forEach(ent -> {
         * log.error("pass par la");
         * ent.getKey().stream().forEach(e-> log.error("" +e));
         * });
         */

        // The ID of all the questions. Used to find the first question that has a given
        // sheet not answered yet

        for (Entry<Long, List<StudentResponse>> ent : byStudent.entrySet()) {
            final var res = s.get(ent.getKey());
            if (res != null) {
                List<StudentResponse> l = ent.getValue();
                l.sort(new ComparatorImplementation2());
                res.setAnsweredSheets(Long.valueOf(l.size()));

                if (l.size() == 0) {
                    res.setFirstUnmarkedQuestion(Long.valueOf(1));

                } else if (l.size() == 1 && l.get(0).question.numero == 1) {
                    res.setFirstUnmarkedQuestion(Long.valueOf(2));

                } else if (l.size() > 0 && l.get(0).question.numero != 1) {
                    res.setFirstUnmarkedQuestion(Long.valueOf(1));
                } else {
                    for (int i = 0; i < l.size() - 1; i++) {
                        StudentResponse sl1 = l.get(i);
                        StudentResponse sl2 = l.get(i + 1);
                        if (sl1.question.numero + 1 < sl2.question.numero) {
                            res.setFirstUnmarkedQuestion(Long.valueOf(sl1.question.numero + 1));
                            break;
                        } else if (i == l.size() - 2) {
                            res.setFirstUnmarkedQuestion(Long.valueOf(sl1.question.numero + 2));
                        }
                    }
                }
            }

        }

        return Response.ok().entity(result).build();
    }

    @GET
    @Path("/getZone4GradedComment/{examId}/{gradedCommentId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getZone4GradedComment(@PathParam("examId") final long examId,
            @PathParam("gradedCommentId") final long gradedCommentId, @Context final UriInfo uriInfo,
            @Context final SecurityContext ctx) {

        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access this ressource").build();
        }
        List<StudentResponse> r = StudentResponse.getAllStudentResponse4examIdGradedCommentId(examId, gradedCommentId)
                .list();
        ZoneSameCommentDTO dto = new ZoneSameCommentDTO();
        List<Answer4QuestionDTO> answers = new ArrayList<>();
        // Map<Long,TextCommentDTO> textComments = new HashMap<>();
        Map<Long, GradedCommentDTO> comments = new HashMap<>();

        if (r.size() > 0 && r.get(0).question != null) {
            int numero = r.get(0).question.numero;
            dto.setNumero(numero);
            List<Question> questions = Question.findQuestionbyExamIdandnumero(examId, numero).list();
            if (questions.size() > 0) {
                dto.setZones(zoneMapper.toDto(questions.stream().map(q -> q.zone).collect(Collectors.toList())));
                dto.setGradeType(questions.get(0).gradeType);
                dto.setPoint(Integer.valueOf(questions.get(0).quarterpoint).doubleValue() / 4);
                dto.setStep(questions.get(0).step);
                dto.setValidExpression(questions.get(0).validExpression);
                dto.setAlgoName(questions.get(0).type.algoName);

            }
        }

        for (StudentResponse studentResponse : r) {
            Answer4QuestionDTO answerdto = new Answer4QuestionDTO();
            answerdto.setPagemin(studentResponse.sheet.pagemin);
            answerdto.setPagemax(studentResponse.sheet.pagemax);

            if (studentResponse.star != null) {
                answerdto.setStar(studentResponse.star);
            } else {
                answerdto.setStar(false);
            }
            if (studentResponse.worststar != null) {
                answerdto.setWorststar(studentResponse.worststar);
            } else {
                answerdto.setWorststar(false);
            }
            Set<Student> students = studentResponse.sheet.students;
            String studentName = "";
            long nbeStudent = students.size();
            long i = 0;
            for (Student student : students) {
                i = i + 1;
                studentName = studentName + student.firstname + " " + student.name;
                if (i != nbeStudent) {
                    studentName = studentName + ", ";
                }

            }
            answerdto.setStudentName(studentName);
            answerdto.setNote(Integer.valueOf(studentResponse.quarternote).doubleValue() / 4);
            answerdto.setComments(commentsMapper.toDto(new ArrayList<>(studentResponse.comments)));
            answerdto.setGradedComments(
                    studentResponse.gradedcomments.stream().map(gc -> gc.id).collect(Collectors.toList()));
            for (GradedComment gc : studentResponse.gradedcomments) {
                if (!comments.containsKey(gc.id)) {
                    comments.put(gc.id, gradedCommentMapper.toDto(gc));
                }
            }
            answers.add(answerdto);
        }

        dto.setAnswers(answers);
        dto.setGradedComments(new ArrayList(comments.values()));
        return Response.ok().entity(dto).build();

    }

    private void computeNote4Hybrid(StudentResponse resp) {
        var currentNote = 0.0;
        var absoluteNote2Add = 0.0;
        double pourcentage = 0.0;
        if (resp.question != null && resp.question.defaultpoint != null) {
            pourcentage = resp.question.defaultpoint.doubleValue() *4;
        }
 //       log.error("default point for question " + resp.question.numero + " " + pourcentage);

        for (Answer2HybridGradedComment an2 : resp.hybridcommentsValues) {
            var stepValue = an2.stepValue !=null ? an2.stepValue.doubleValue(): 0.0;
            if (stepValue > 0) {
                var relative = an2.hybridcomments.relative != null ? an2.hybridcomments.relative : false;
                var step = an2.hybridcomments.step != null ? an2.hybridcomments.step.doubleValue() : 1.0;
                var grade = an2.hybridcomments.grade != null ? an2.hybridcomments.grade.doubleValue() : 0.0;

                if (relative) {
                    pourcentage = pourcentage + ((stepValue / step) * grade);

                } else {
                    absoluteNote2Add = absoluteNote2Add + (stepValue / step) * grade;

                }
            }
        }
        var point = resp.question.quarterpoint != null ? resp.question.quarterpoint.doubleValue() : 0.0;
      //  log.error("point for question " + resp.question.numero + " " + absoluteNote2Add + " " + point + " " + pourcentage);

        currentNote = ((point * pourcentage) / 400.0) + absoluteNote2Add;

        if (currentNote > point) {
//            log.error("currentNote " + currentNote + " " + point + " " + resp.question.numero);
            currentNote = point;
        } else if (currentNote < 0) {
            currentNote = 0;
        }
       // log.error("question " + resp.question.numero+ " currentNote " + Double.valueOf(currentNote /4));
        if (Double.valueOf(currentNote * 100).intValue() != resp.quarternote) {
            resp.quarternote = Double.valueOf(currentNote * 100).intValue();
            StudentResponse.update(resp);
        }

    }

    @GET
    @Path("/getZone4HybridComment/{examId}/{hybridCommentId}/{stepValue}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getZone4HybridComment(@PathParam("examId") final long examId,
            @PathParam("hybridCommentId") final long hybridCommentId, @PathParam("stepValue") final int stepValue,
            @Context final UriInfo uriInfo,
            @Context final SecurityContext ctx) {

        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access this ressource").build();
        }
        List<Answer2HybridGradedComment> _r = StudentResponse
                .getAllStudentResponse4examIdHybridCommentId(examId, hybridCommentId, stepValue)
                .list();
        List<Answer2HybridGradedComment> r = new ArrayList<>();
        r.addAll(_r);

        if (stepValue == 0) {
            Optional<HybridGradedComment> s = HybridGradedComment.findByIdOptional(hybridCommentId);
            if (s.isPresent()) {
                long qId = s.get().question.id;
                List<Long> answerIds = Answer2HybridGradedComment
                        .findAllAnswerHybridGradedCommentByCommentId(hybridCommentId).list().stream()
                        .map(hc -> hc.studentResponse.id).collect(Collectors.toList());
                List<StudentResponse> resps = StudentResponse
                        .getAllStudentResponse4QidNotInResponseIdLists(qId, answerIds).list();
                for (StudentResponse resp : resps) {
                    Answer2HybridGradedComment an = new Answer2HybridGradedComment();
                    an.hybridcomments = s.get();
                    an.stepValue = 0;
                    an.studentResponse = resp;
                    r.add(an);
                }
            }
        }
        ZoneSameCommentDTO dto = new ZoneSameCommentDTO();
        List<Answer4QuestionDTO> answers = new ArrayList<>();
        // Map<Long,TextCommentDTO> textComments = new HashMap<>();
        Map<Long, HybridGradedCommentDTO> comments = new HashMap<>();

        if (r.size() > 0 && r.get(0).studentResponse.question != null) {
            int numero = r.get(0).studentResponse.question.numero;
            dto.setNumero(numero);
            List<Question> questions = Question.findQuestionbyExamIdandnumero(examId, numero).list();
            if (questions.size() > 0) {
                dto.setZones(zoneMapper.toDto(questions.stream().map(q -> q.zone).collect(Collectors.toList())));
                dto.setGradeType(questions.get(0).gradeType);
                dto.setPoint(Integer.valueOf(questions.get(0).quarterpoint).doubleValue() / 4);
                dto.setStep(questions.get(0).step);
                dto.setValidExpression(questions.get(0).validExpression);
                dto.setAlgoName(questions.get(0).type.algoName);

            }
        }
        Set<StudentResponse> processSt = new HashSet<>();
        for (Answer2HybridGradedComment an : r) {
            StudentResponse studentResponse = an.studentResponse;
            this.computeNote4Hybrid(studentResponse);

            if (!processSt.contains(studentResponse)) {
                Answer4QuestionDTO answerdto = new Answer4QuestionDTO();
                answerdto.setPagemin(studentResponse.sheet.pagemin);
                answerdto.setPagemax(studentResponse.sheet.pagemax);

                if (studentResponse.star != null) {
                    answerdto.setStar(studentResponse.star);
                } else {
                    answerdto.setStar(false);
                }
                if (studentResponse.worststar != null) {
                    answerdto.setWorststar(studentResponse.worststar);
                } else {
                    answerdto.setWorststar(false);
                }
                Set<Student> students = studentResponse.sheet.students;
                String studentName = "";
                long nbeStudent = students.size();
                long i = 0;
                for (Student student : students) {
                    i = i + 1;
                    studentName = studentName + student.firstname + " " + student.name;
                    if (i != nbeStudent) {
                        studentName = studentName + ", ";
                    }
                }
                answerdto.setStudentName(studentName);
                answerdto.setNote(Integer.valueOf(studentResponse.quarternote).doubleValue() / 4);
                answerdto.setComments(commentsMapper.toDto(new ArrayList<>(studentResponse.comments)));
                Map<Long, Integer> hybridC = new HashMap<>();
                r.stream().filter(an1 -> an1.studentResponse.id == studentResponse.id && an1.stepValue > 0)
                        .forEach(anh -> hybridC.put(anh.hybridcomments.id, anh.stepValue));
                answerdto.setHybridgradedComments(hybridC);
                processSt.add(studentResponse);
                answers.add(answerdto);

            }
            if (!comments.containsKey(an.hybridcomments.id)) {
                comments.put(an.hybridcomments.id, hybridCommentMapper.toDto(an.hybridcomments));
            }

        }

        dto.setAnswers(answers);
        dto.setHybridComments(new ArrayList(comments.values()));
        return Response.ok().entity(dto).build();

    }

    @GET
    @Path("/getZone4TextComment/{examId}/{textCommentId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getZone4TextComment(@PathParam("examId") final long examId,
            @PathParam("textCommentId") final long textCommentId, @Context final UriInfo uriInfo,
            @Context final SecurityContext ctx) {

        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access this ressource").build();
        }
        List<StudentResponse> r = StudentResponse.getAllStudentResponse4examIdTextCommentId(examId, textCommentId)
                .list();
        ZoneSameCommentDTO dto = new ZoneSameCommentDTO();
        List<Answer4QuestionDTO> answers = new ArrayList<>();
        Map<Long, TextCommentDTO> comments = new HashMap<>();
        // Map<Long,GradedCommentDTO> comments = new HashMap<>();

        if (r.size() > 0 && r.get(0).question != null) {
            int numero = r.get(0).question.numero;
            dto.setNumero(numero);
            List<Question> questions = Question.findQuestionbyExamIdandnumero(examId, numero).list();
            if (questions.size() > 0) {
                dto.setZones(zoneMapper.toDto(questions.stream().map(q -> q.zone).collect(Collectors.toList())));
                dto.setGradeType(questions.get(0).gradeType);
                dto.setPoint(Integer.valueOf(questions.get(0).quarterpoint).doubleValue() / 4);
                dto.setStep(questions.get(0).step);
                dto.setValidExpression(questions.get(0).validExpression);
                dto.setAlgoName(questions.get(0).type.algoName);

            } else {
                return Response.noContent().build();
            }
        } else {
            return Response.noContent().build();
        }

        for (StudentResponse studentResponse : r) {
            Answer4QuestionDTO answerdto = new Answer4QuestionDTO();
            answerdto.setPagemin(studentResponse.sheet.pagemin);
            answerdto.setPagemax(studentResponse.sheet.pagemax);

            if (studentResponse.star != null) {
                answerdto.setStar(studentResponse.star);
            } else {
                answerdto.setStar(false);
            }
            if (studentResponse.worststar != null) {
                answerdto.setWorststar(studentResponse.worststar);
            } else {
                answerdto.setWorststar(false);
            }
            Set<Student> students = studentResponse.sheet.students;
            String studentName = "";
            long nbeStudent = students.size();
            long i = 0;
            for (Student student : students) {
                i = i + 1;
                studentName = studentName + student.firstname + " " + student.name;
                if (i != nbeStudent) {
                    studentName = studentName + ", ";
                }

            }
            answerdto.setStudentName(studentName);
            answerdto.setNote(Integer.valueOf(studentResponse.quarternote).doubleValue() / 4);
            answerdto.setComments(commentsMapper.toDto(new ArrayList<>(studentResponse.comments)));
            answerdto.setTextComments(
                    studentResponse.textcomments.stream().map(gc -> gc.id).collect(Collectors.toList()));
            for (TextComment gc : studentResponse.textcomments) {
                if (!comments.containsKey(gc.id)) {
                    comments.put(gc.id, textCommentMapper.toDto(gc));
                }
            }
            answers.add(answerdto);
        }

        dto.setAnswers(answers);
        dto.setTextComments(new ArrayList(comments.values()));
        return Response.ok().entity(dto).build();

    }

    @GET
    @Path("/getZone4Mark/{examId}/{respid}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getZone4Mark(@PathParam("examId") final long examId, @PathParam("respid") final long respid,
            @Context final UriInfo uriInfo,
            @Context final SecurityContext ctx) {

        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access this ressource").build();
        }
        List<StudentResponse> r = StudentResponse.getAllStudentResponseWithSameGrade4examIdRespId(examId, respid)
                .list();
        ZoneSameCommentDTO dto = new ZoneSameCommentDTO();
        List<Answer4QuestionDTO> answers = new ArrayList<>();
        Map<Long, TextCommentDTO> textcomments = new HashMap<>();
        Map<Long, GradedCommentDTO> gradedcomments = new HashMap<>();
        Map<Long, HybridGradedCommentDTO> hybridcomments = new HashMap<>();

        if (r.size() > 0 && r.get(0).question != null) {
            int numero = r.get(0).question.numero;
            List<HybridGradedComment> hc = HybridGradedComment.findByQuestionId(r.get(0).question.id).list();
            hc.forEach(hc1 -> hybridcomments.put(hc1.id, this.hybridCommentMapper.toDto(hc1)));

            dto.setNumero(numero);
            List<Question> questions = Question.findQuestionbyExamIdandnumero(examId, numero).list();
            if (questions.size() > 0) {
                dto.setZones(zoneMapper.toDto(questions.stream().map(q -> q.zone).collect(Collectors.toList())));
                dto.setGradeType(questions.get(0).gradeType);
                dto.setPoint(Integer.valueOf(questions.get(0).quarterpoint).doubleValue() / 4);
                dto.setStep(questions.get(0).step);
                dto.setValidExpression(questions.get(0).validExpression);
                dto.setAlgoName(questions.get(0).type.algoName);

            } else {
                return Response.noContent().build();
            }
        } else {
            return Response.noContent().build();
        }

        for (StudentResponse studentResponse : r) {
            Answer4QuestionDTO answerdto = new Answer4QuestionDTO();
            answerdto.setPagemin(studentResponse.sheet.pagemin);
            answerdto.setPagemax(studentResponse.sheet.pagemax);

            if (studentResponse.star != null) {
                answerdto.setStar(studentResponse.star);
            } else {
                answerdto.setStar(false);
            }
            if (studentResponse.worststar != null) {
                answerdto.setWorststar(studentResponse.worststar);
            } else {
                answerdto.setWorststar(false);
            }
            Set<Student> students = studentResponse.sheet.students;
            String studentName = "";
            long nbeStudent = students.size();
            long i = 0;
            for (Student student : students) {
                i = i + 1;
                studentName = studentName + student.firstname + " " + student.name;
                if (i != nbeStudent) {
                    studentName = studentName + ", ";
                }

            }
            answerdto.setStudentName(studentName);
            if (studentResponse.question.gradeType == GradeType.HYBRID
                    && !"QCM".equals(studentResponse.question.type.algoName)) {
                this.computeNote4Hybrid(studentResponse);
            }
            answerdto.setNote(Integer.valueOf(studentResponse.quarternote).doubleValue() / 4);

            answerdto.setComments(commentsMapper.toDto(new ArrayList<>(studentResponse.comments)));
            answerdto.setTextComments(
                    studentResponse.textcomments.stream().map(gc -> gc.id).collect(Collectors.toList()));
            answerdto.setGradedComments(
                    studentResponse.gradedcomments.stream().map(gc -> gc.id).collect(Collectors.toList()));

            for (TextComment gc : studentResponse.textcomments) {
                if (!textcomments.containsKey(gc.id)) {
                    textcomments.put(gc.id, textCommentMapper.toDto(gc));
                }
            }
            for (GradedComment gc : studentResponse.gradedcomments) {
                if (!gradedcomments.containsKey(gc.id)) {
                    gradedcomments.put(gc.id, gradedCommentMapper.toDto(gc));
                }
            }
            List<Answer2HybridGradedComment> r1 = Answer2HybridGradedComment
                    .findAllAnswerHybridGradedCommentByAnswerId(studentResponse.id).list();
            Map<Long, Integer> hybridC = new HashMap<>();
            r1.stream().filter(an1 -> an1.studentResponse.id == studentResponse.id && an1.stepValue > 0)
                    .forEach(anh -> hybridC.put(anh.hybridcomments.id, anh.stepValue));
            answerdto.setHybridgradedComments(hybridC);
            answers.add(answerdto);
        }

        dto.setAnswers(answers);
        dto.setTextComments(new ArrayList(textcomments.values()));
        dto.setGradedComments(new ArrayList(gradedcomments.values()));
        dto.setHybridComments(new ArrayList(hybridcomments.values()));

        return Response.ok().entity(dto).build();

    }

    @GET
    @Path("/getZone4Numero/{examId}/{qid}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getZone4Question(@PathParam("examId") final long examId, @PathParam("qid") final long qid,
            @Context final UriInfo uriInfo,
            @Context final SecurityContext ctx) {

        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access this ressource").build();
        }

        Question question = Question.findById(qid);
        ZoneSameCommentDTO dto = new ZoneSameCommentDTO();
        List<Answer4QuestionDTO> answers = new ArrayList<>();
        Map<Long, TextCommentDTO> textcomments = new HashMap<>();
        Map<Long, GradedCommentDTO> gradedcomments = new HashMap<>();
        Map<Long, HybridGradedCommentDTO> hybridcomments = new HashMap<>();

        int numero = question.numero;
        dto.setNumero(numero);
        List<Question> questions = Question.findQuestionbyExamIdandnumero(examId, numero).list();
        if (questions.size() > 0) {
            List<HybridGradedComment> hc = HybridGradedComment.findByQuestionId(questions.get(0).id).list();
            hc.forEach(hc1 -> hybridcomments.put(hc1.id, this.hybridCommentMapper.toDto(hc1)));

            dto.setZones(zoneMapper.toDto(questions.stream().map(q -> q.zone).collect(Collectors.toList())));
            dto.setGradeType(questions.get(0).gradeType);
            dto.setPoint(Integer.valueOf(questions.get(0).quarterpoint).doubleValue() / 4);
            dto.setStep(questions.get(0).step);
            dto.setValidExpression(questions.get(0).validExpression);
            dto.setAlgoName(questions.get(0).type.algoName);

        } else {
            return Response.noContent().build();
        }
        List<ExamSheet> sheets = ExamSheet.getAll4ExamId(examId).list();

        for (ExamSheet sheet : sheets) {
            Answer4QuestionDTO answerdto = new Answer4QuestionDTO();
            answerdto.setPagemin(sheet.pagemin);
            answerdto.setPagemax(sheet.pagemax);
            Set<Student> students = sheet.students;
            String studentName = "";
            long nbeStudent = students.size();
            long i = 0;
            for (Student student : students) {
                i = i + 1;
                studentName = studentName + student.firstname + " " + student.name;
                if (i != nbeStudent) {
                    studentName = studentName + ", ";
                }

            }
            answerdto.setStudentName(studentName);

            List<StudentResponse> r = StudentResponse
                    .getAllStudentResponseWithExamIdNumeroAndSheetId(examId, numero, sheet.id)
                    .list();
            if (r.size() > 0) {
                StudentResponse studentResponse = r.get(0);
                if (studentResponse.star != null) {
                    answerdto.setStar(studentResponse.star);
                } else {
                    answerdto.setStar(false);
                }
                if (studentResponse.worststar != null) {
                    answerdto.setWorststar(studentResponse.worststar);
                } else {
                    answerdto.setWorststar(false);
                }
                answerdto.setNote(Integer.valueOf(studentResponse.quarternote).doubleValue() / 4);
                answerdto.setComments(commentsMapper.toDto(new ArrayList<>(studentResponse.comments)));
                answerdto.setTextComments(
                        studentResponse.textcomments.stream().map(gc -> gc.id).collect(Collectors.toList()));
                answerdto.setGradedComments(
                        studentResponse.gradedcomments.stream().map(gc -> gc.id).collect(Collectors.toList()));
                for (TextComment gc : studentResponse.textcomments) {
                    if (!textcomments.containsKey(gc.id)) {
                        textcomments.put(gc.id, textCommentMapper.toDto(gc));
                    }
                }
                for (GradedComment gc : studentResponse.gradedcomments) {
                    if (!gradedcomments.containsKey(gc.id)) {
                        gradedcomments.put(gc.id, gradedCommentMapper.toDto(gc));
                    }
                }
                List<Answer2HybridGradedComment> r1 = Answer2HybridGradedComment
                        .findAllAnswerHybridGradedCommentByAnswerId(studentResponse.id).list();
                Map<Long, Integer> hybridC = new HashMap<>();
                r1.stream().filter(an1 -> an1.studentResponse.id == studentResponse.id && an1.stepValue > 0)
                        .forEach(anh -> hybridC.put(anh.hybridcomments.id, anh.stepValue));
                answerdto.setHybridgradedComments(hybridC);
            }
            answers.add(answerdto);
        }

        dto.setAnswers(answers);
        dto.setTextComments(new ArrayList(textcomments.values()));
        dto.setGradedComments(new ArrayList(gradedcomments.values()));
        dto.setHybridComments(new ArrayList(hybridcomments.values()));

        return Response.ok().entity(dto).build();

    }

    @PUT
    @Transactional()
    @Path("/update-2-hybrid-graded-comments-with-stepvalue/{responseId}/{hybridCommentId}/{stepValue}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response updateAnswerHybridCommentWithStepValue(@PathParam("responseId") final long responseId,
            @PathParam("hybridCommentId") final long hybridCommentId,
            @PathParam("stepValue") final int stepValue,
            @Context final UriInfo uriInfo,
            @Context final SecurityContext ctx) {
        if (!securityService.canAccess(ctx, responseId, StudentResponse.class)) {
            return Response.status(403, "Current user cannot access this ressource").build();
        }
        Answer2HybridGradedCommentDTO result = this.answer2HybridGradedCommentService
                .setStepValueWithResponseIdAndHybridCommentId(responseId, hybridCommentId, stepValue);
        return Response.ok().entity(result).build();

    }

    @PUT
    @Transactional()
    @Path("/update-2-hybrid-graded-comments/{responseId}/{hybridCommentId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response updateAnswerHybridCommentWith(@PathParam("responseId") final long responseId,
            @PathParam("hybridCommentId") final long hybridCommentId,
            @Context final UriInfo uriInfo,
            @Context final SecurityContext ctx) {
        if (!securityService.canAccess(ctx, responseId, StudentResponse.class)) {
            return Response.status(403, "Current user cannot access this ressource").build();
        }
        Answer2HybridGradedCommentDTO result = this.answer2HybridGradedCommentService
                .incrementWithResponseIdAndHybridCommentId(responseId, hybridCommentId);
        return Response.ok().entity(result).build();

    }

    @POST
    @Transactional()
    @Path("/updateStudentResponse4Cluster/{examId}/{qid}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response updateStudentResponse4Cluster(ClusterDTO clusterDto, @PathParam("examId") final long examId,
            @PathParam("qid") final long qid,
            @Context final UriInfo uriInfo,
            @Context final SecurityContext ctx) {

        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access this ressource").build();
        }

        Question question = Question.findById(qid);
        // ZoneSameCommentDTO dto = new ZoneSameCommentDTO();
        List<SheetQuestion> answers = new ArrayList<>();
        Map<Long, TextComment> textcomments = new HashMap<>();
        Map<Long, GradedComment> gradedcomments = new HashMap<>();

        int numero = question.numero;
        if (question == null || "QCM".equals(question.type.algoName)) {
            return Response.noContent().build();
        }
        List<ExamSheet> sheets = ExamSheet.getAll4ExamId(examId).list();

        for (ExamSheet sheet : sheets) {
            SheetQuestion answerdto = new SheetQuestion();
            answerdto.sheet = sheet;
            answerdto.question = question;

            List<StudentResponse> r = StudentResponse
                    .getAllStudentResponseWithExamIdNumeroAndSheetId(examId, numero, sheet.id)
                    .list();
            if (r.size() > 0) {
                StudentResponse studentResponse = r.get(0);
                answerdto.studentResponse = studentResponse;
                answerdto.studentResponseId = studentResponse.id;
                if (studentResponse.star != null) {
                    answerdto.star = (studentResponse.star);
                } else {
                    answerdto.star = false;
                }
                if (studentResponse.worststar != null) {
                    answerdto.worststar = studentResponse.worststar;
                } else {
                    answerdto.worststar = false;
                }
                answerdto.quarternote = studentResponse.quarternote;
                answerdto.textComments = studentResponse.textcomments.stream().map(gc -> gc.id)
                        .collect(Collectors.toList());
                answerdto.gradedComments = studentResponse.gradedcomments.stream().map(gc -> gc.id)
                        .collect(Collectors.toList());
                for (TextComment gc : studentResponse.textcomments) {
                    if (!textcomments.containsKey(gc.id)) {
                        textcomments.put(gc.id, gc);
                    }
                }
                for (GradedComment gc : studentResponse.gradedcomments) {
                    if (!gradedcomments.containsKey(gc.id)) {
                        gradedcomments.put(gc.id, gc);
                    }
                }
            } else {
                answerdto.studentResponseId = -1;
                answerdto.star = false;
                answerdto.worststar = false;

            }

            answers.add(answerdto);
        }

        SheetQuestion answerdtotempalate = answers.get(clusterDto.getTemplat());
        if (answerdtotempalate.studentResponseId == -1) {
            return Response.noContent().build();
        }
        List<Answer2HybridGradedComment> answerToCommentTemplate = new ArrayList<>();
        if (question.gradeType == GradeType.HYBRID) {
            answerToCommentTemplate = Answer2HybridGradedComment.findAllAnswerHybridGradedCommentByAnswerId(
                    answerdtotempalate.studentResponseId).list();
        }

        for (int toUpdate : clusterDto.getCopies()) {
            SheetQuestion answerdtoUpdate = answers.get(toUpdate);
            if (answerdtoUpdate.studentResponseId == -1) {
                StudentResponse stToUpdate = new StudentResponse();
                stToUpdate.quarternote = answerdtotempalate.quarternote;
                stToUpdate.star = answerdtotempalate.star;
                stToUpdate.worststar = answerdtotempalate.worststar;
                stToUpdate.question = answerdtoUpdate.question;
                stToUpdate.sheet = answerdtoUpdate.sheet;

                if (question.gradeType == GradeType.DIRECT) {
                    stToUpdate.textcomments.addAll(textcomments.values().stream()
                            .filter(gs2 -> answerdtotempalate.textComments.contains(gs2.id))
                            .collect(Collectors.toList()));
                    StudentResponse.persist(stToUpdate);

                } else if (question.gradeType == GradeType.HYBRID) {
                    StudentResponse.persist(stToUpdate);
                    Answer2HybridGradedComment.findAllAnswerHybridGradedCommentByAnswerId(
                            answerdtotempalate.studentResponseId).list().forEach(an1 -> {
                                Answer2HybridGradedCommentDTO an = new Answer2HybridGradedCommentDTO();
                                an.hybridcommentsId = an1.hybridcomments.id;
                                an.studentResponseId = stToUpdate.id;
                                an.stepValue = an1.stepValue;
                                this.answer2HybridGradedCommentService.persistOrUpdate(an);

                            });
                }

                else {
                    stToUpdate.gradedcomments.addAll(gradedcomments.values().stream()
                            .filter(gs2 -> answerdtotempalate.gradedComments.contains(gs2.id))
                            .collect(Collectors.toList()));
                    StudentResponse.persist(stToUpdate);

                }

            } else {
                StudentResponse stToUpdate = StudentResponse.cleanCommentAndGrade(answerdtoUpdate.studentResponse);
                stToUpdate.quarternote = answerdtotempalate.quarternote;
                stToUpdate.star = answerdtotempalate.star;
                stToUpdate.worststar = answerdtotempalate.worststar;
                if (stToUpdate.question.id != answerdtoUpdate.question.id) {
                    log.error("strange to update a StudentResponse that do not target the same question");
                }
                if (stToUpdate.sheet.id != answerdtoUpdate.sheet.id) {
                    log.error("strange to update a StudentResponse that do not target the same sheet");
                }

                if (question.gradeType == GradeType.DIRECT) {
                    stToUpdate.textcomments.addAll(textcomments.values().stream()
                            .filter(gs2 -> answerdtotempalate.textComments.contains(gs2.id))
                            .collect(Collectors.toList()));
                } else if (question.gradeType == GradeType.HYBRID) {
                    List<Answer2HybridGradedComment> existingComments = Answer2HybridGradedComment
                            .findAllAnswerHybridGradedCommentByAnswerId(stToUpdate.id).list();
                    existingComments.forEach(cc -> {
                        cc.stepValue = 0;
                        cc.persistOrUpdate();
                    });

                    answerToCommentTemplate.forEach(an1 -> {
                        if (existingComments.stream().map(cc -> cc.hybridcomments.id)
                                .anyMatch(ccid -> ccid == an1.hybridcomments.id)) {
                            var commentsToUpdate = existingComments.stream()
                                    .filter(cc -> cc.hybridcomments.id == an1.hybridcomments.id).findFirst();
                            if (commentsToUpdate.isPresent()) {
                                commentsToUpdate.get().stepValue = an1.stepValue;
                                // log.error("should " + commentsToUpdate.get().hybridcomments.text + " "+
                                // commentsToUpdate.get().studentResponse.sheet.pagemin + " "+
                                // commentsToUpdate.get().stepValue);

                                commentsToUpdate.get().persistOrUpdate();
                            } else {
                                log.error("should never go there");
                            }
                        } else {
                            Answer2HybridGradedCommentDTO an = new Answer2HybridGradedCommentDTO();
                            an.hybridcommentsId = an1.hybridcomments.id;
                            an.studentResponseId = stToUpdate.id;
                            an.stepValue = an1.stepValue;
                            this.answer2HybridGradedCommentService.persistOrUpdate(an);
                        }
                    });

                } else {
                    stToUpdate.gradedcomments.addAll(gradedcomments.values().stream()
                            .filter(gs2 -> answerdtotempalate.gradedComments.contains(gs2.id))
                            .collect(Collectors.toList()));
                }
                stToUpdate.persistOrUpdate();

            }

        }

        return Response.ok().build();

    }

    class SheetQuestion {
        ExamSheet sheet;
        Question question;
        long studentResponseId;
        StudentResponse studentResponse;
        int quarternote;
        boolean star = false;
        boolean worststar = false;
        List<Long> textComments = new ArrayList<>();
        List<Long> gradedComments = new ArrayList<>();

    }

}
