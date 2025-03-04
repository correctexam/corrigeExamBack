package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.Answer2HybridGradedComment;
import fr.istic.domain.Exam;
import fr.istic.domain.GradedComment;
import fr.istic.domain.HybridGradedComment;
import fr.istic.domain.Question;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.TextComment;
import fr.istic.domain.Zone;
import fr.istic.domain.enumeration.GradeType;
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

        if (question.id!= null){
            Question q2 = Question.findById(question.id);
            if (q2 != null && question.gradeType == GradeType.HYBRID && (
                q2.gradeType != question.gradeType || q2.defaultpoint != question.defaultpoint || q2.quarterpoint != q2.quarterpoint
            ) ){

            List<StudentResponse> sts= StudentResponse.findAllByQuestionIdfetchAnswerfetchHybridCommand(question.id).list();
            for (StudentResponse st : sts ){
                var currentNote = 0.0;
                    var absoluteNote2Add = 0.0;
                    double pourcentage = 0.0;
                    if (question != null && question.defaultpoint != null){
                        pourcentage = question.defaultpoint.doubleValue() *4;
                    }

                for( Answer2HybridGradedComment an2 : st.hybridcommentsValues){
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
                    var point = question.quarterpoint !=null ? question.quarterpoint.doubleValue(): 0.0;
                    currentNote = (point * pourcentage) / 400.0 + absoluteNote2Add;
                    if (currentNote > point && !st.question.canExceedTheMax) {
                        currentNote = point;
                    } else if (currentNote < 0 && !st.question.canBeNegative) {
                        currentNote = 0;
                    }
                    st.quarternote = Double.valueOf(currentNote*100).intValue();
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
        Set<StudentResponse> srs= this.updateCorrectionAndAnswer(question, gradeComment, textComments);
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


}
