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
import fr.istic.domain.Course;
import fr.istic.domain.Exam;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.FinalResult;
import fr.istic.domain.GradedComment;
import fr.istic.domain.HybridGradedComment;
import fr.istic.domain.Prediction;
import fr.istic.domain.Question;
import fr.istic.domain.QuestionType;
import fr.istic.domain.Scan;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.TextComment;
import fr.istic.domain.User;
import fr.istic.domain.Zone;
import fr.istic.domain.enumeration.GradeType;
import fr.istic.service.customdto.answernotebooks.AnswersNoteBook;
import fr.istic.service.customdto.answernotebooks.QuestionNoteBook;
import fr.istic.service.dto.CourseDTO;
import fr.istic.service.dto.ExamDTO;
import fr.istic.service.mapper.ExamMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.transaction.Transactional;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    public void delete(Long id) {
        log.debug("Request to delete Exam : {}", id);
        this.prepareDeleteExam(id);
        this.deleteExam(id);


    }

    @Transactional
    public void prepareDeleteExam(long id){
             Exam.findByIdOptional(id).ifPresent(exam -> {
            ExamSheet.getAll4ExamIdEvenOrphan(id).list().forEach(sr -> sr.cleanBeforDelete());
            /*StudentResponse.getAll4ExamIdEvenOrphan(id).list().forEach(sr -> {
                sr.clearComments();
                Answer2HybridGradedComment.deleteAllAnswerHybridGradedCommentByAnswerId(sr.id);
            });

            StudentResponse.getAll4ExamIdEvenOrphan(id).list().forEach(sr -> sr.delete());
            FinalResult.getAll4ExamId(id).list().forEach(f -> f.delete());
            var s = HybridGradedComment.deleteByQIds(e.questions.stream().map(q -> q.id).collect(Collectors.toSet()));
            log.error("to remove" + s);*/
            this.deleteQuestionCommentAndZone(id);
            Exam e = Exam.findById(id);

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
            Comments.deleteCommentByExamId("" + id);
            this.cacheService.deleteFile(id);
        });

    }


    @Transactional
    protected void deleteExam(long id){
            var e = Exam.findById(id);
            e.delete();

    }

    @Transactional
    protected void cleanStudentRssponse(long id) {
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
    protected Set<Long> cleanExamZone(long id) {
        Exam exam = Exam.findById(id);
        Set<Long> zoneids = new HashSet<>();
        if (exam.namezone != null) {
            zoneids.add(exam.namezone.id);
        } else if (exam.firstnamezone != null) {
            zoneids.add(exam.firstnamezone.id);
        } else if (exam.idzone != null) {
            zoneids.add(exam.idzone.id);
        } else if (exam.notezone != null) {
            zoneids.add(exam.notezone.id);
        }
        Exam.removeAllZone(exam);

        return zoneids;
    }

    @Transactional
    protected void cleanExamZone(Set<Long> zoneids) {
        Zone.deleteAllZonesIds(zoneids);

    }

    @Transactional
    protected void cleanQuestion(long examId, Set<Long> qids) {
        var s  = Answer2HybridGradedComment.deleteAllByQIds(qids);
        Set<Long> cids = HybridGradedComment.findByExamId(examId).list().stream().map(ex -> ex.id).collect(Collectors.toSet());
        s  = Answer2HybridGradedComment.deleteAllAnswerHybridGradedCommentByCommentIds(cids);
        s= StudentResponse.deleteByQIds(qids);
        s= TextComment.deleteByQIds(qids);
        s= GradedComment.deleteByQIds(qids);
        s= HybridGradedComment.deleteByQIds(qids);
        s=Prediction.deleteByQIds(qids);
    }
    @Transactional
    protected void removeQuestion(long examId, Set<Long> qids) {
        Question.deleteAllExamId(examId);
    }

    @Transactional
    protected void cleanFinalResult(long id) {
        FinalResult.deleteAllByExamId(id);

    }

    /**
     * Delete the Exam by id.
     *
     * @param id the id of the entity.
     */
    public void deleteQuestionCommentAndZone(Long examid) {
        log.debug("Request to delete Exam : {}", examid);
        Set<Long> qids = Question.findQuestionbyExamId(examid).list().stream().map(ex -> ex.id).collect(Collectors.toSet());

        this.cleanFinalResult(examid);
        Set<Long> zonesids = this.cleanExamZone(examid);
        this.cleanExamZone(zonesids);
        // this.cleanStudentRssponse(id);
        this.cleanQuestion(examid, qids);
        this.removeQuestion(examid, qids);

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

            /**
     * Get all the exams.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<ExamDTO> findAll4User(Page page, User u) {
        log.debug("Request to get all Exams " + u.login );
        return new Paged<>(Exam.findExambyLogin(u.login).page(page))
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

    private static final Map<Double, Integer> knownFractions = new HashMap<>();
    static {
        knownFractions.put(1.0 / 2, 2);
        knownFractions.put(1.0 / 3, 3);
        knownFractions.put(0.33, 3);
        knownFractions.put(0.66, 3);
        knownFractions.put(2.0 / 3, 3);
        knownFractions.put(1.0 / 4, 4);
        knownFractions.put(3.0 / 4, 4);
        knownFractions.put(1.0 / 5, 5);
        knownFractions.put(2.0 / 5, 5);
        knownFractions.put(3.0 / 5, 5);
        knownFractions.put(4.0 / 5, 5);
        knownFractions.put(1.0 / 6, 6);
        knownFractions.put(0.17, 6);
        knownFractions.put(0.167, 6);
        knownFractions.put(5.0 / 6, 6);
        knownFractions.put(0.83, 6);
        knownFractions.put(0.833, 6);
        knownFractions.put(1.0 / 8, 8);
        knownFractions.put(3.0 / 8, 8);
        knownFractions.put(5.0 / 8, 8);
        knownFractions.put(7.0 / 8, 7);
        knownFractions.put(1.0 / 9, 9);
        knownFractions.put(0.11, 9);
        knownFractions.put(0.111, 9);
        knownFractions.put(2.0 / 9, 9);
        knownFractions.put(0.22, 9);
        knownFractions.put(0.222, 9);
        knownFractions.put(4.0 / 9, 9);
        knownFractions.put(0.44, 9);
        knownFractions.put(0.444, 9);
        knownFractions.put(5.0 / 9, 9);
        knownFractions.put(0.55, 9);
        knownFractions.put(0.555, 9);
        knownFractions.put(0.556, 9);
        knownFractions.put(7.0 / 9, 9);
        knownFractions.put(0.77, 9);
        knownFractions.put(0.777, 9);
        knownFractions.put(0.778, 9);
        knownFractions.put(8.0 / 9, 9);
        knownFractions.put(0.88, 9);
        knownFractions.put(0.888, 9);
        knownFractions.put(0.889, 9);
    }

    private static final Map<Double, Integer> knownFractionsNum = new HashMap<>();
    static {
        knownFractionsNum.put(1.0 / 2, 1);
        knownFractionsNum.put(1.0 / 3, 1);
        knownFractionsNum.put(0.33, 1);
        knownFractionsNum.put(0.66, 2);
        knownFractionsNum.put(2.0 / 3, 2);
        knownFractionsNum.put(1.0 / 4, 1);
        knownFractionsNum.put(3.0 / 4, 3);
        knownFractionsNum.put(1.0 / 5, 1);
        knownFractionsNum.put(2.0 / 5, 2);
        knownFractionsNum.put(3.0 / 5, 3);
        knownFractionsNum.put(4.0 / 5, 4);
        knownFractionsNum.put(1.0 / 6, 1);
        knownFractionsNum.put(0.17, 1);
        knownFractionsNum.put(0.167, 1);
        knownFractionsNum.put(5.0 / 6, 5);
        knownFractionsNum.put(0.83, 5);
        knownFractionsNum.put(0.833, 5);
        knownFractionsNum.put(1.0 / 8, 1);
        knownFractionsNum.put(3.0 / 8, 8);
        knownFractionsNum.put(5.0 / 8, 3);
        knownFractionsNum.put(7.0 / 8, 7);
        knownFractionsNum.put(1.0 / 9, 1);
        knownFractionsNum.put(0.11, 1);
        knownFractionsNum.put(0.111, 1);
        knownFractionsNum.put(2.0 / 9, 2);
        knownFractionsNum.put(0.22, 2);
        knownFractionsNum.put(0.222, 2);
        knownFractionsNum.put(4.0 / 9, 4);
        knownFractionsNum.put(0.44, 4);
        knownFractionsNum.put(0.444, 4);
        knownFractionsNum.put(5.0 / 9, 5);
        knownFractionsNum.put(0.55, 5);
        knownFractionsNum.put(0.555, 5);
        knownFractionsNum.put(0.556, 5);
        knownFractionsNum.put(7.0 / 9, 7);
        knownFractionsNum.put(0.77, 7);
        knownFractionsNum.put(0.777, 7);
        knownFractionsNum.put(0.778, 7);
        knownFractionsNum.put(8.0 / 9, 8);
        knownFractionsNum.put(0.88, 8);
        knownFractionsNum.put(0.888, 8);
        knownFractionsNum.put(0.889, 8);
    }

    public static int decimalToFractionDenominateur(double value) {
        for (Map.Entry<Double, Integer> entry : knownFractions.entrySet()) {
            if (Math.abs(value - entry.getKey()) < 0.001) {
                return entry.getValue();
            }
        }

        // Sinon, on tente une conversion automatique avec limite au dénominateur de 100
        int maxDenominator = 100;
        int bestNumerator = 1;
        int bestDenominator = 1;
        double minError = Double.MAX_VALUE;

        for (int denom = 1; denom <= maxDenominator; denom++) {
            int numer = (int) Math.round(value * denom);
            double error = Math.abs(value - (double) numer / denom);
            if (error < minError) {
                bestNumerator = numer;
                bestDenominator = denom;
                minError = error;
                if (error < 0.0001)
                    break;
            }
        }

        int gcd = gcd(bestNumerator, bestDenominator);
        return (bestDenominator / gcd);
    }

    public static int decimalToFractionNumerateur(double value) {
        for (Map.Entry<Double, Integer> entry : knownFractionsNum.entrySet()) {
            if (Math.abs(value - entry.getKey()) < 0.001) {
                return entry.getValue();
            }
        }

        // Sinon, on tente une conversion automatique avec limite au dénominateur de 100
        int maxDenominator = 100;
        int bestNumerator = 1;
        int bestDenominator = 1;
        double minError = Double.MAX_VALUE;

        for (int denom = 1; denom <= maxDenominator; denom++) {
            int numer = (int) Math.round(value * denom);
            double error = Math.abs(value - (double) numer / denom);
            if (error < minError) {
                bestNumerator = numer;
                bestDenominator = denom;
                minError = error;
                if (error < 0.0001)
                    break;
            }
        }

        int gcd = gcd(bestNumerator, bestDenominator);
        return (bestNumerator / gcd);
    }

    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    @Transactional
    public void createNoteBookExamStructure(List<AnswersNoteBook> answersNoteBook, User u) {
        Exam e = Exam.findById(answersNoteBook.get(0).getExamId());

        Integer maxLength = answersNoteBook.stream()
                .mapToInt(answersNoteBook1 -> answersNoteBook1.getQuestions().size()).max().orElse(0);
        AnswersNoteBook questionMaxLength = null;
        for (AnswersNoteBook answersNoteBook1 : answersNoteBook) {
            if (answersNoteBook1.getQuestions().size() == maxLength) {
                questionMaxLength = answersNoteBook1;
                break;
            }
        }
        Set<Integer> qsnumero = questionMaxLength.getQuestions().stream().map(e1 -> e1.getNumero())
                .collect(Collectors.toSet());

        Scan scan = new Scan();
        scan.name = e.name + "Scan";
        scan.contentContentType = "application/zip";
        e.scanfile = scan;
        Scan.persistOrUpdate(scan);
        e.persistOrUpdate();
        Map<Integer, Question> questionCaches = new HashMap<>();
        Map<Integer, HybridGradedComment> hcCaches = new HashMap<>();
        int qIndex = 0;
        for (QuestionNoteBook qnb : questionMaxLength.getQuestions()) {
            Question q = new Question();
            q.exam = e;
            q.gradeType = GradeType.HYBRID;
            q.type = QuestionType.findQuestionTypebyAlgoName("manual").firstResult();
            q.numero = qIndex + 1;
            q.randomHorizontalCorrection = false;
            q.canBeNegative = false;
            q.canExceedTheMax = false;
            q.mustBeIgnoreInGlobalScale = false;
            q.defaultpoint = 0;
            q.quarterpoint = Double.valueOf(qnb.getNotemax() * 4).intValue();

            Zone z = new Zone();
            z.pageNumber = qIndex+1;
            z.xInit = -1;
            z.yInit = -1;
            z.height = -1;
            z.width = -1;

            Zone.persistOrUpdate(z);

            q.zone = z;
            q.exam = e;
            e.questions.add(q);
            Question.persistOrUpdate(q);

            questionCaches.put(qnb.getNumero(), q);

            // Hybride comment à créer
            HybridGradedComment hybridGradedComment = new HybridGradedComment();
            hybridGradedComment.text = "Note";
            hybridGradedComment.description = "Nbgrader automatic evaluation";
            hybridGradedComment.question = q;
            q.hybridcomments.add(hybridGradedComment);
            hybridGradedComment.relative = true;
            // Compute minimum step
            Integer maxStep = 1;
            for (AnswersNoteBook answerNoteBook : answersNoteBook) {
                if (qIndex < answerNoteBook.getQuestions().size()) {
                    QuestionNoteBook q1 = answerNoteBook.getQuestions().get(qIndex);
                    if (q1.getNotemax() > 0.0 && q1.getNote() > 0.0) {
                        Integer step = decimalToFractionDenominateur(q1.getNote() / q1.getNotemax());
                        if (step > maxStep) {
                            maxStep = step;
                        }
                    }
                }
            }

            hybridGradedComment.step = maxStep;
            hybridGradedComment.grade = 400;
            HybridGradedComment.persistOrUpdate(hybridGradedComment);
            hcCaches.put(q.numero, hybridGradedComment);

            qIndex = qIndex + 1;

        }

        Integer studentIndex = 0;
        for (AnswersNoteBook answerNoteBook : answersNoteBook) {

            ExamSheet es = new ExamSheet();
            es.pagemin = (studentIndex * maxLength);
            es.pagemax = (studentIndex * maxLength) + maxLength - 1;
            es.name = answerNoteBook.getSheetName();
            scan.sheets.add(es);
            es.scan = scan;
            ExamSheet.persistOrUpdate(es);

            for (int numero : qsnumero) {
                Optional<QuestionNoteBook> q1 = answerNoteBook.getQuestions().stream()
                        .filter(qnb -> qnb.getNumero() == numero).findFirst();
                q1.ifPresent(qnb -> {
                    // QuestionNoteBook qnb : answerNoteBook.getQuestions()) {
                    StudentResponse sr = new StudentResponse();
                    sr.sheet = es;
                    sr.lastModifiedDate = Instant.now();
                    sr.correctedBy = u;
                    sr.question = questionCaches.get(qnb.getNumero());
                    sr.worststar = false;
                    sr.star = false;
                    StudentResponse.persistOrUpdate(sr);
                    Answer2HybridGradedComment answer2HybridGradedComment = new Answer2HybridGradedComment();
                    answer2HybridGradedComment.hybridcomments = hcCaches.get(numero);
                    hcCaches.get(numero).valueAnswers.add(answer2HybridGradedComment);
                    HybridGradedComment.persistOrUpdate(hcCaches.get(numero));
                    answer2HybridGradedComment.studentResponse = sr;
                    if (qnb.getNotemax() > 0.0 && qnb.getNote() > 0.0) {
                        answer2HybridGradedComment.stepValue = decimalToFractionNumerateur(
                                qnb.getNote() / qnb.getNotemax());
                    } else {
                        answer2HybridGradedComment.stepValue = 0;
                    }
                    Answer2HybridGradedComment.persistOrUpdate(answer2HybridGradedComment);
                    StudentResponse.persistOrUpdate(sr);
                });
                if (q1.isEmpty()) {
                    StudentResponse sr = new StudentResponse();
                    sr.sheet = es;
                    sr.lastModifiedDate = Instant.now();
                    sr.correctedBy = u;
                    sr.question = questionCaches.get(numero);
                    sr.worststar = false;
                    sr.star = false;
                    sr.quarternote = 0;
                    StudentResponse.persistOrUpdate(sr);

                }

            }
            studentIndex = studentIndex + 1;
        }

    }

}
