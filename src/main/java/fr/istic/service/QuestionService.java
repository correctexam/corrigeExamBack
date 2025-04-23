package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.Answer2HybridGradedComment;
import fr.istic.domain.Exam;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.GradedComment;
import fr.istic.domain.HybridGradedComment;
import fr.istic.domain.Question;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.TextComment;
import fr.istic.domain.Zone;
import fr.istic.domain.enumeration.GradeType;
import fr.istic.service.customdto.exportcomments.Answer;
import fr.istic.service.customdto.exportcomments.AnswersWithPredictionDto;
import fr.istic.service.customdto.exportcomments.Comment;
import fr.istic.service.customdto.exportcomments.Prediction;
import fr.istic.service.dto.ExamDTO;
import fr.istic.service.dto.QuestionDTO;
import fr.istic.service.mapper.ExamMapper;
import fr.istic.service.mapper.QuestionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class QuestionService {

    private final Logger log = LoggerFactory.getLogger(QuestionService.class);

    @Inject
    QuestionMapper questionMapper;

    @Inject
    ExamMapper examMapper;

    @Inject
    ZoneService zoneService;

    @Transactional
    public QuestionDTO persistOrUpdate(QuestionDTO questionDTO) {
        log.debug("Request to save Question : {}", questionDTO);
        var question = questionMapper.toEntity(questionDTO);

        if (question.id != null) {
            Question q2 = Question.findById(question.id);
            if (q2 != null && question.gradeType == GradeType.HYBRID && (q2.gradeType != question.gradeType
                    || q2.defaultpoint != question.defaultpoint || q2.quarterpoint != q2.quarterpoint)) {

                List<StudentResponse> sts = StudentResponse
                        .findAllByQuestionIdfetchAnswerfetchHybridCommand(question.id).list();
                for (StudentResponse st : sts) {
                    var currentNote = 0.0;
                    var absoluteNote2Add = 0.0;
                    double pourcentage = 0.0;
                    if (question != null && question.defaultpoint != null) {
                        pourcentage = question.defaultpoint.doubleValue() * 4;
                    }

                    for (Answer2HybridGradedComment an2 : st.hybridcommentsValues) {
                        var stepValue = an2.stepValue.doubleValue();
                        if (stepValue > 0) {
                            var relative = an2.hybridcomments.relative != null ? an2.hybridcomments.relative : false;
                            var step = an2.hybridcomments.step != null ? an2.hybridcomments.step.doubleValue() : 1.0;
                            var grade = an2.hybridcomments.grade != null ? an2.hybridcomments.grade.doubleValue() : 0.0;

                            if (relative) {
                                pourcentage = pourcentage + (stepValue / step) * grade;
                            } else {
                                absoluteNote2Add = absoluteNote2Add + (stepValue / step) * grade;
                            }
                        }
                    }
                    var point = question.quarterpoint != null ? question.quarterpoint.doubleValue() : 0.0;
                    currentNote = (point * pourcentage) / 400.0 + absoluteNote2Add;
                    if (currentNote > point && !st.question.canExceedTheMax) {
                        currentNote = point;
                    } else if (currentNote < 0 && !st.question.canBeNegative) {
                        currentNote = 0;
                    }
                    st.quarternote = Double.valueOf(currentNote * 100).intValue();
                    st.persistOrUpdate();
                }
            }
        }
        question = Question.persistOrUpdate(question);
        return questionMapper.toDto(question);
    }

    @Transactional()
    public QuestionDTO cleanAllCorrectionAndComment(QuestionDTO questionDTO) {
        log.debug("Request to clean Question : {}", questionDTO);
        var question = questionMapper.toEntity(questionDTO);

        return questionMapper.toDto(cleanAllCorrectionAndComment(question));
    }

    public Question cleanAllCorrectionAndComment(Question question) {
        List<GradedComment> gradeComment = new ArrayList<GradedComment>();
        List<TextComment> textComments = new ArrayList<TextComment>();
        gradeComment.addAll(GradedComment.findByQuestionId(question.id).list());
        textComments.addAll(TextComment.findByQuestionId(question.id).list());
        Set<StudentResponse> srs = this.updateCorrectionAndAnswer(question, gradeComment, textComments);
        List<Long> gradeCommentids = gradeComment.stream().map(gc -> gc.id).collect(Collectors.toList());
        List<Long> textCommentsids = textComments.stream().map(gc -> gc.id).collect(Collectors.toList());

        this.deleteComments(gradeCommentids, textCommentsids);

        srs.forEach(sr -> {
            Answer2HybridGradedComment.deleteAllAnswerHybridGradedCommentByAnswerId(sr.id);
            sr.delete();
        });
        Set<Long> qids = new HashSet<>();
        qids.add(question.id);
        HybridGradedComment.deleteByQIds(qids);

        return question;
    }

    public Set<StudentResponse> updateCorrectionAndAnswer(Question question, List<GradedComment> gradeComment,
            List<TextComment> textComments) {
        Set<StudentResponse> srs = new HashSet(StudentResponse.findAllByQuestionId(question.id).list());
        for (GradedComment gc : gradeComment) {
            gc.studentResponses.clear();
            gc.question = null;
            gc.persistOrUpdate();
            List<StudentResponse> srs1 = StudentResponse.findAllByGradedCommentsIds(gc.id).list();
            srs.addAll(srs1);
        }
        for (TextComment gc : textComments) {
            gc.studentResponses.clear();
            gc.question = null;
            gc.persistOrUpdate();
            List<StudentResponse> srs1 = StudentResponse.findAllByTextCommentsIds(gc.id).list();
            srs.addAll(srs1);
        }
        for (StudentResponse sr : srs) {
            sr.clearCommentsAndNote();
        }

        question.gradedcomments.clear();
        question.textcomments.clear();

        Question.update(question).persistOrUpdate();
        return srs;

    }

    @Transactional
    public void deleteComments(List<Long> gradeComment, List<Long> textComments) {

        for (Long gc : gradeComment) {
            GradedComment.deleteById(gc);
        }
        for (Long gc : textComments) {
            TextComment.deleteById(gc);
        }
    }

    /**
     * Delete the Question by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete Question : {}", id);
        Question.findByIdOptional(id).ifPresent(question -> {
            question.delete();
        });

    }

    /**
     * Get one question by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<QuestionDTO> findOne(Long id) {
        log.debug("Request to get Question : {}", id);
        return Question.findByIdOptional(id)
                .map(question -> questionMapper.toDto((Question) question));
    }

    /**
     * Get all the questions.
     *
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<QuestionDTO> findAll(Page page) {
        log.debug("Request to get all Questions");
        return new Paged<>(Question.findAll().page(page))
                .map(question -> questionMapper.toDto((Question) question));
    }

    public Paged<QuestionDTO> findQuestionbyExamId(Page page, long examId) {
        log.debug("Request to get Questions by ExamID");
        return new Paged<>(Question.findQuestionbyExamId(examId).page(page))
                .map(question -> questionMapper.toDto((Question) question));
    }

    public Paged<QuestionDTO> findQuestionbyExamIdAndNumero(Page page, long examId, int numero) {
        log.debug("Request to get Questions by ExamID");
        return new Paged<>(Question.findQuestionbyExamIdandnumero(examId, numero).page(page))
                .map(question -> questionMapper.toDto((Question) question));
    }

    public Paged<QuestionDTO> findQuestionbyZoneId(Page page, long zoneId) {
        log.debug("Request to get Questions by ZoneId");
        return new Paged<>(Question.findQuestionbyZoneId(zoneId).page(page))
                .map(question -> questionMapper.toDto((Question) question));
    }

    public AnswersWithPredictionDto getallcommentsandprediction4qId(long qId) {
        log.error("ok");
        try{
            AnswersWithPredictionDto awp = new AnswersWithPredictionDto();
        Question q = Question.findById(qId);
        awp.setQid(qId);
        var point = q.quarterpoint != null ? q.quarterpoint.doubleValue() : 0.0;

        awp.setMaxgrade(point / 4.0);
        List<StudentResponse> srs = StudentResponse.findAllByQuestionId(qId).list();
        List<fr.istic.domain.Prediction> predictions = fr.istic.domain.Prediction.findByQuestionId(qId).list();

        for (StudentResponse sr : srs) {

            ExamSheet sh = sr.getCSheet();

            if (sh != null && sh.pagemin != -1 && sh.pagemax != -1) {

                Answer answer = new Answer();
                awp.getAnswer().add(answer);
                answer.setPagemin(sh.pagemin);
                answer.setPagemax(sh.pagemax);
                answer.setSheetId(sh.id);
                answer.setSheetName(sh.name);
                predictions.stream().filter(pr -> pr.sheet.id == sh.id).findAny().ifPresent(p -> {
                    Prediction p1 = new Prediction();
                    p1.setPredictionconfidence(p.predictionconfidence);
                    p1.setText(p.text);
                    answer.setPrediction(p1);
                });
                answer.setGrade(this.computeNote(sr)/100.0);

                for (TextComment t : sr.textcomments) {
                    Comment comment = new Comment();
                    comment.setText(t.text);
                    comment.setDescription(t.description);
                    answer.getComments().add(comment);
                }
                for (GradedComment t : sr.gradedcomments) {
                    Comment comment = new Comment();
                    comment.setText(t.text);
                    comment.setDescription(t.description);
                    log.error(""+q.step);
                    if (!"QCM".equals(q.type.algoName) && q.step > 0) {
                        if (q.gradeType == GradeType.POSITIVE) {
                            comment.setNoteComments(t.gradequarter / 4.0 / q.step);
                        } else if (q.gradeType == GradeType.NEGATIVE) {
                            comment.setNoteComments(-1.0*t.gradequarter / 4.0 / q.step);
                        }
                    }
                    answer.getComments().add(comment);
                }
                for (Answer2HybridGradedComment t : sr.hybridcommentsValues) {
                    Comment comment = new Comment();
                    comment.setText(t.hybridcomments.text);
                    comment.setDescription(t.hybridcomments.description);
                    comment.setNoteComments(computeNote4HybridComment(sr, t)/4.0);
                    answer.getComments().add(comment);
                }
            }
        }
        return awp;
        }catch (Exception e){
            e.printStackTrace();
            log.error("error in getallcommentsandprediction4qId", e);
            return null;
        }

    }

    private double computeNote(StudentResponse resp){
        if (resp.question.gradeType == GradeType.DIRECT && !"QCM".equals(resp.question.type.algoName)) {
            if (resp.question.step > 0) {
                return ((resp.quarternote * 100 / 4) / resp.question.step);
            }else {
                return 0.0;
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
            if (resp.question.step > 0) {
                return (currentNote * 100 / 4 / resp.question.step);
            } else {
                return 0.0;
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
            if (resp.question.step > 0) {
                return (currentNote * 100 / 4 / resp.question.step);
            }
            else {
                return 0.0;
            }

        } else if (resp.question.gradeType == GradeType.HYBRID
                && !"QCM".equals(resp.question.type.algoName)) {
            return this.computeNote4Hybrid(resp)/4;

        } else if ("QCM".equals(resp.question.type.algoName) && resp.question.step !=null && resp.question.step > 0) {
            int currentNote = 0;
            for (var g : resp.gradedcomments) {
                if (g.description.startsWith("correct")) {
                    currentNote = currentNote + resp.question.quarterpoint * resp.question.step;
                } else if (g.description.startsWith("incorrect")) {
                    currentNote = currentNote - resp.question.quarterpoint;
                }
            }
            return (currentNote * 100 / 4 / resp.question.step);
        } else if ("QCM".equals(resp.question.type.algoName) && resp.question.step <= 0) {
            int currentNote = 0;
            for (var g : resp.gradedcomments) {
                if (g.description.startsWith("correct")) {
                    currentNote = currentNote + resp.question.quarterpoint;
                }
            }
            return (currentNote * 100 / 4);
        }
        else {
            return 0.0;
        }
    }

    private double computeNote4Hybrid(StudentResponse resp) {
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

        return Double.valueOf(currentNote * 100);

    }


    private double computeNote4HybridComment(StudentResponse resp, Answer2HybridGradedComment an2) {
        var absoluteNote2Add = 0.0;
        double pourcentage = 0.0;
        if (resp.question != null && resp.question.defaultpoint != null) {
            pourcentage = resp.question.defaultpoint.doubleValue() *4;
        }
 //       log.error("default point for question " + resp.question.numero + " " + pourcentage);

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

            return absoluteNote2Add;
    }

}
