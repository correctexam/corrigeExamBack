package fr.istic.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fr.istic.domain.Answer2HybridGradedComment;
import fr.istic.domain.Exam;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.FinalResult;
import fr.istic.domain.StudentResponse;
import fr.istic.service.customdto.exportpdf.ExportPDFDto;
import fr.istic.service.customdto.exportpdf.Gradedcommentspdf;
import fr.istic.service.customdto.exportpdf.Hybridcommentspdf;
import fr.istic.service.customdto.exportpdf.Questionspdf;
import fr.istic.service.customdto.exportpdf.Sheetspdf;
import fr.istic.service.customdto.exportpdf.StudentResponsepdf;
import fr.istic.service.customdto.exportpdf.Studentpdf;
import fr.istic.service.customdto.exportpdf.Textcommentspdf;
import fr.istic.service.customdto.exportpdf.Zonepdf;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Builds the data the browser needs to generate the PDF of corrected copies.
 * <p>
 * Moved out of the former {@code ExtendedAPI} REST class (T3).
 */
@ApplicationScoped
public class ExportPdfService {

    @Inject
    FinalNoteService finalNoteService;

    public ExportPDFDto getExportPdf(long examId, String sheetname) {
        finalNoteService.computeFinalNote(examId, new HashMap<>(), new HashMap<>(), new HashMap<>());

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
            if (q.step != null) {
                qpdf.setStep(q.step);
            } else {
                qpdf.setStep(1);
            }
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
}
