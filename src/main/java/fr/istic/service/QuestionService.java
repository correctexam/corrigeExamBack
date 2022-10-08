package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.GradedComment;
import fr.istic.domain.Question;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.TextComment;
import fr.istic.service.dto.QuestionDTO;
import fr.istic.service.mapper.QuestionMapper;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

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

    @Transactional
    public QuestionDTO persistOrUpdate(QuestionDTO questionDTO) {
        log.debug("Request to save Question : {}", questionDTO);
        var question = questionMapper.toEntity(questionDTO);
        question = Question.persistOrUpdate(question);
        return questionMapper.toDto(question);
    }

    @Transactional()
    public QuestionDTO cleanAllCorrectionAndComment(QuestionDTO questionDTO) {
        log.debug("Request to clean Question : {}", questionDTO);
        var question = questionMapper.toEntity(questionDTO);
        List<GradedComment> gradeComment = new ArrayList<GradedComment>();
        List<TextComment> textComments = new ArrayList<TextComment>();
        gradeComment.addAll(GradedComment.findByQuestionId(question.id).list());
        textComments.addAll(TextComment.findByQuestionId(question.id).list());
        this.updateCorrectionAndAnswer(question, gradeComment,  textComments);
        List<Long> gradeCommentids = gradeComment.stream().map(gc -> gc.id).collect(Collectors.toList());
        List<Long> textCommentsids = textComments.stream().map(gc -> gc.id).collect(Collectors.toList());

        this.deleteComments(gradeCommentids,  textCommentsids);

        return questionMapper.toDto(question);
    }

    protected void updateCorrectionAndAnswer (Question question,  List<GradedComment> gradeComment , List<TextComment> textComments){
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



    }
    @Transactional
    protected void deleteComments (List<Long> gradeComment , List<Long> textComments){


    for (Long gc : gradeComment) {
        System.err.println("delete gc" + gc);
        //GradedComment.deleteById(gc.id);
        GradedComment.deleteById(gc);
      //  gc.delete();
    }
    for (Long gc : textComments) {
        System.err.println("delete gc" + gc);
        //GradedComment.deleteById(gc.id);
        TextComment.deleteById(gc);
      //  gc.delete();
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
        return new Paged<>(Question.findQuestionbyExamIdandnumero(examId,numero).page(page))
            .map(question -> questionMapper.toDto((Question) question));
    }


    public Paged<QuestionDTO> findQuestionbyZoneId(Page page, long zoneId) {
        log.debug("Request to get Questions by ZoneId");
        return new Paged<>(Question.findQuestionbyZoneId(zoneId).page(page))
            .map(question -> questionMapper.toDto((Question) question));
    }



}
