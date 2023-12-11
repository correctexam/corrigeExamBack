package fr.istic.service;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.quarkus.panache.common.Page;
import fr.istic.domain.Answer2HybridGradedComment;
import fr.istic.domain.Comments;
import fr.istic.domain.Exam;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.FinalResult;
import fr.istic.domain.GradedComment;
import fr.istic.domain.HybridGradedComment;
import fr.istic.domain.Question;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.TextComment;
import fr.istic.domain.Zone;
import fr.istic.service.dto.ExamDTO;
import fr.istic.service.mapper.ExamMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class ExamService {

    private final Logger log = LoggerFactory.getLogger(ExamService.class);

    @Inject
    ExamMapper examMapper;

    @Inject
    CacheUploadService cacheService;

    @Inject
    FichierS3Service fichierS3Service;

    @Inject
    QuestionService questionService;

    @Transactional
    public ExamDTO persistOrUpdate(ExamDTO examDTO) {
        log.debug("Request to save Exam : {}", examDTO);
        var exam = examMapper.toEntity(examDTO);
        exam = Exam.persistOrUpdate(exam);
        return examMapper.toDto(exam);
    }

    /**
     * Delete the Exam by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete Exam : {}", id);
        Exam.findByIdOptional(id).ifPresent(exam -> {
            StudentResponse.getAll4ExamIdEvenOrphan(id).list().forEach(sr -> {
                sr.clearComments();
                Answer2HybridGradedComment.deleteAllAnswerHybridGradedCommentByAnswerId(sr.id);
            });

            ExamSheet.getAll4ExamIdEvenOrphan(id).list().forEach(sr -> sr.cleanBeforDelete());
            StudentResponse.getAll4ExamIdEvenOrphan(id).list().forEach(sr -> sr.delete());
            FinalResult.getAll4ExamId(id).list().forEach(f -> f.delete());
            Exam e = Exam.findById(id);
            HybridGradedComment.deleteByQIds(e.questions.stream().map(q-> q.id).collect(Collectors.toSet()));

            if (e.scanfile != null && this.fichierS3Service.isObjectExist("scan/" + e.scanfile.id + ".pdf")) {
                try {
                    this.fichierS3Service.deleteObject("scan/" + e.scanfile.id + ".pdf");
                } catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
                        | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
                        | IllegalArgumentException | IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (e.template != null && this.fichierS3Service.isObjectExist("template/" + e.template.id + ".pdf")) {
                try {
                    this.fichierS3Service.deleteObject("template/" + e.template.id + ".pdf");
                } catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
                        | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
                        | IllegalArgumentException | IOException e1) {
                    e1.printStackTrace();
                }
            }
            exam.delete();
            Comments.deleteCommentByExamId("" + id);

            this.cacheService.deleteFile(id);
        });
    }

    @Transactional
    protected void cleanStudentRssponse(long id){
        List<StudentResponse> srs = StudentResponse.getAll4ExamIdEvenOrphan(id).list();
        srs.forEach(sr -> {
            Set<TextComment> tcs = new HashSet<TextComment>(sr.textcomments);
            tcs.forEach(tc -> {
                tc.studentResponses.remove(sr);
                tc.persistOrUpdate();
            });
            sr.textcomments.clear();

            Set<GradedComment> gcs = new HashSet<GradedComment>(sr.gradedcomments);
            gcs.forEach(tc -> {
                tc.studentResponses.remove(sr);
                tc.persistOrUpdate();
            });
            sr.gradedcomments.clear();
            sr.sheet = null;
            sr.question = null;
            sr.comments.clear();
            sr.persistOrUpdate();
        });

       // srs.forEach(sr -> sr.delete());

    }

    @Transactional
    protected Set<Long>  cleanExamZone(long id){
        Exam exam = Exam.findById(id);
        Set<Long> zoneids = new HashSet<>();
        if (exam.namezone != null ){
            zoneids.add(exam.namezone.id);
        }
        else if (exam.firstnamezone != null ){
            zoneids.add(exam.firstnamezone.id);
        }
        else if (exam.idzone != null ){
            zoneids.add(exam.idzone.id);
        }
        else if (exam.notezone != null )
        {
            zoneids.add(exam.notezone.id);
        }
        Exam.removeAllZone(exam);

        return zoneids;
    }

    @Transactional
    protected void  cleanExamZone(Set<Long> zoneids){
        Zone.deleteAllZonesIds(zoneids);

    }



    @Transactional
    protected void  cleanQuestion(long examId,Set<Long> qids ){
        StudentResponse.deleteByQIds(   qids);
        TextComment.deleteByQIds(qids);
        GradedComment.deleteByQIds(qids);
        Question.deleteAllExamId(examId);

    }



    @Transactional
    protected void cleanFinalResult(long id){
        FinalResult.deleteAllByExamId(id);

    }
        /**
     * Delete the Exam by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void deleteQuestionCommentAndZone(Long id) {
        log.debug("Request to delete Exam : {}", id);
            Set<Long> qids = Question.findQuestionbyExamId(id).list().stream().map(ex -> ex.id).collect(Collectors.toSet());

            this.cleanFinalResult(id);
            Set<Long> zonesids = this.cleanExamZone(id);
            this.cleanExamZone(zonesids);
           // this.cleanStudentRssponse(id);
            this.cleanQuestion(id,qids);



    }

    /**
     * Get one exam by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional
    public Optional<ExamDTO> findOne(Long id) {
        log.debug("Request to get Exam : {}", id);
        return Exam.findByIdOptional(id)
                .map(exam -> examMapper.toDto((Exam) exam));
    }

    /**
     * Get all the exams.
     *
     * @param page the pagination information.
     * @return the list of entities.
     */
    @Transactional
    public Paged<ExamDTO> findAll(Page page) {
        log.debug("Request to get all Exams");
        return new Paged<>(Exam.findAll().page(page))
                .map(exam -> examMapper.toDto((Exam) exam));
    }

    @Transactional
    public Paged<ExamDTO> findExambyCourseId(Page page, long courseId) {
        log.debug("Request to get all Exams");
        return new Paged<>(Exam.findExambyCourseId(courseId).page(page))
                .map(exam -> examMapper.toDto((Exam) exam));
    }

    @Transactional
    public Paged<ExamDTO> findExambyScanId(Page page, long scanId) {
        log.debug("Request to get all Exams by scanId");
        return new Paged<>(Exam.findExambyScanId(scanId).page(page))
                .map(exam -> examMapper.toDto((Exam) exam));
    }

}
