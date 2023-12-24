package fr.istic.service;

import fr.istic.domain.Answer2HybridGradedComment;
import fr.istic.domain.GradedComment;
import fr.istic.domain.HybridGradedComment;
import fr.istic.service.dto.HybridGradedCommentDTO;
import fr.istic.service.mapper.HybridGradedCommentMapper;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Transactional
public class HybridGradedCommentService {

    private final Logger log = LoggerFactory.getLogger(HybridGradedCommentService.class);

    @Inject
    HybridGradedCommentMapper hybridGradedCommentMapper;

    @Inject
    Answer2HybridGradedCommentService answer2HybridGradedCommentService;

    @Transactional
    public HybridGradedCommentDTO persistOrUpdate(HybridGradedCommentDTO hybridGradedCommentDTO) {
        log.debug("Request to save HybridGradedComment : {}", hybridGradedCommentDTO);
        var hybridGradedComment = hybridGradedCommentMapper.toEntity(hybridGradedCommentDTO);
        boolean shouldUpdate =false;

        if (hybridGradedComment.id!= null){
            HybridGradedComment h2 = HybridGradedComment.findById(hybridGradedComment.id);
            if (h2 != null){
                var oldGrade  = h2.grade;
                var oldRelative  = h2.relative;
                var oldStep  = h2.step;
                if (hybridGradedComment.grade != oldGrade || hybridGradedComment.step != oldStep || hybridGradedComment.relative!= oldRelative){
                    shouldUpdate  =true;
                }

            }
        }
        hybridGradedComment = HybridGradedComment.persistOrUpdate(hybridGradedComment);
        if (shouldUpdate){
                List<Answer2HybridGradedComment> ans = Answer2HybridGradedComment.findAllAnswerHybridGradedCommentByCommentIdWithFetchWithStepvalueUpperThan0(hybridGradedComment.id).list(); //.stream().filter(an3 -> an3.stepValue>0).collect(Collectors.toList());
                for( Answer2HybridGradedComment an : ans){
                    var st = an.studentResponse;
                    var ans2 = an.studentResponse.hybridcommentsValues;

                    var currentNote = 0.0;
                    var absoluteNote2Add = 0.0;
                    double pourcentage = 0.0;
                    if (st.question != null && st.question.defaultpoint != null){
                        pourcentage = st.question.defaultpoint.doubleValue() *4;

                    }
                    for (Answer2HybridGradedComment an2 : ans2){
                        var stepValue = an2.stepValue !=null ? an2.stepValue.doubleValue(): 0.0;
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
                    var point = st.question.quarterpoint !=null ? st.question.quarterpoint.doubleValue(): 0.0;
                    currentNote = (point * pourcentage) / 400.0 + absoluteNote2Add;
                    if (currentNote > point) {
                        currentNote = point;
                    } else if (currentNote < 0) {
                        currentNote = 0;
                    }
                    st.quarternote = Double.valueOf(currentNote*100).intValue();
                    st.persistOrUpdate();
                }
            }


        return hybridGradedCommentMapper.toDto(hybridGradedComment);
    }

    /**
     * Delete the HybridGradedComment by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete HybridGradedComment : {}", id);
        this.answer2HybridGradedCommentService.deleteAllAnswerHybridGradedCommentByCommentId(id);
        HybridGradedComment
            .findByIdOptional(id)
            .ifPresent(hybridGradedComment -> {
                hybridGradedComment.delete();
            });
    }

    /**
     * Get one hybridGradedComment by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<HybridGradedCommentDTO> findOne(Long id) {
        log.debug("Request to get HybridGradedComment : {}", id);
        return HybridGradedComment
            .findByIdOptional(id)
            .map(hybridGradedComment -> hybridGradedCommentMapper.toDto((HybridGradedComment) hybridGradedComment));
    }

    /**
     * Get all the hybridGradedComments.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<HybridGradedCommentDTO> findAll(Page page) {
        log.debug("Request to get all HybridGradedComments");
        return new Paged<>(HybridGradedComment.findAll().page(page))
            .map(hybridGradedComment -> hybridGradedCommentMapper.toDto((HybridGradedComment) hybridGradedComment));
    }

    public Paged<HybridGradedCommentDTO> findHybridGradedCommentByQuestionId(Page page, long qId) {
         log.debug("Request to get all GradedComments by QID");
        return new Paged<>(HybridGradedComment.findByQuestionId(qId).page(page))
            .map(hgradedComment -> hybridGradedCommentMapper.toDto((HybridGradedComment) hgradedComment));    }
}
