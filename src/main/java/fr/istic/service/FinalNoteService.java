package fr.istic.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.domain.Answer2HybridGradedComment;
import fr.istic.domain.Exam;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.FinalResult;
import fr.istic.domain.Student;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.enumeration.GradeType;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * Computes and stores the final note of each student of an exam.
 * <p>
 * Moved out of the former {@code ExtendedAPI} REST class (T3).
 */
@ApplicationScoped
public class FinalNoteService {

    private final Logger log = LoggerFactory.getLogger(FinalNoteService.class);

    /** Note stored in {@code FinalResult.note} for a justified absence (ABJ). */
    public static final int VALUEFORABJ = -100000;

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
                        if (resp.quarternote!= null){

                        finalnote = finalnote + (resp.quarternote / 4);
                    }

                    } else if (QcmDslGrading.isDsl(resp.question)) {
                        // Grading DSL (ADR 0002): percentages stored on the options, note in hundredths of a point
                        int currentNote = QcmDslGrading.quarternote(resp.question, resp.gradedcomments);
                        if (resp.quarternote == null || currentNote != resp.quarternote) {
                            resp.quarternote = currentNote;
                            StudentResponse.update(resp);
                        }
                        finalnote = finalnote + (currentNote / 4);
                    } else if ("QCM".equals(resp.question.type.algoName) && resp.question.step !=null && resp.question.step > 0) {
                        int currentNote = 0;
                        for (var g : resp.gradedcomments) {
                            if (g.description.startsWith("correct")) {
                                currentNote = currentNote + resp.question.quarterpoint * resp.question.step;
                            } else if (g.description.startsWith("incorrect")) {
                                currentNote = currentNote - resp.question.quarterpoint;
                            }
                        }
                        // Once per answer, after all its options (it used to be added again for each option)
                        if (resp.quarternote == null || currentNote != resp.quarternote) {
                            resp.quarternote = currentNote;
                            StudentResponse.update(resp);
                        }
                        finalnote = finalnote + (currentNote * 100 / 4 / resp.question.step);
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

    public void computeNote4Hybrid(StudentResponse resp) {
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

        if (currentNote > point && !resp.question.canExceedTheMax) {
//            log.error("currentNote " + currentNote + " " + point + " " + resp.question.numero);
            currentNote = point;
        } else if (currentNote < 0 && !resp.question.canBeNegative) {
            currentNote = 0;
        }
       // log.error("question " + resp.question.numero+ " currentNote " + Double.valueOf(currentNote /4));
        if (resp.quarternote == null || Math.round(Double.valueOf(currentNote * 100.0)) != resp.quarternote) {
            resp.quarternote =(int) Math.round(Double.valueOf(currentNote * 100.0));
            StudentResponse.update(resp);
        }

    }
}
