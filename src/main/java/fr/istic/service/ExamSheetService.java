package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.Answer2HybridGradedComment;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.GradedComment;
import fr.istic.domain.HybridGradedComment;
import fr.istic.domain.Question;
import fr.istic.domain.Student;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.TextComment;
import fr.istic.domain.User;
import fr.istic.service.dto.ExamSheetDTO;
import fr.istic.service.dto.StudentResponseDTO;
import fr.istic.service.mapper.ExamSheetMapper;
import fr.istic.service.mapper.StudentResponseMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class ExamSheetService {

    private final Logger log = LoggerFactory.getLogger(ExamSheetService.class);

    @Inject
    ExamSheetMapper examSheetMapper;

    @Inject
    StudentResponseMapper studentResponseMapper;

    @Transactional
    public ExamSheetDTO persistOrUpdate(ExamSheetDTO examSheetDTO) {
        log.debug("Request to save ExamSheet : {}", examSheetDTO);
        var examSheet = examSheetMapper.toEntity(examSheetDTO);
        examSheet = ExamSheet.persistOrUpdate(examSheet);
        return examSheetMapper.toDto(examSheet);
    }

    /**
     * Delete the ExamSheet by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete ExamSheet : {}", id);
        ExamSheet.findByIdOptional(id).ifPresent(examSheet -> {
            examSheet.delete();
        });
    }

    /**
     * Get one examSheet by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<ExamSheetDTO> findOne(Long id) {
        log.debug("Request to get ExamSheet : {}", id);
        return ExamSheet.findByIdOptional(id)
                .map(examSheet -> examSheetMapper.toDto((ExamSheet) examSheet));
    }

    /**
     * Get all the examSheets.
     *
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<ExamSheetDTO> findAll(Page page) {
        log.debug("Request to get all ExamSheets");
        return new Paged<>(ExamSheet.findAll().page(page))
                .map(examSheet -> examSheetMapper.toDto((ExamSheet) examSheet));
    }

    /**
     * Get all the examSheets.
     *
     * @param page the pagination information.
     * @return the list of entities by name.
     */
    public Paged<ExamSheetDTO> findExamSheetByName(Page page, String name) {
        log.debug("Request to get all ExamSheets by name");
        return new Paged<>(ExamSheet.findExamSheetByName(name).page(page))
                .map(examSheet -> examSheetMapper.toDto((ExamSheet) examSheet));
    }

    public Paged<ExamSheetDTO> findOrCreateExamSheetByPageMinAndPageMax(Page page, Long scanId, Integer pagemin,
            Integer pagemax) throws Exception {
        long p = ExamSheet.findExamSheetByScanAndPageminAndPagemax(scanId, pagemin, pagemax).count();
        if (p == 0) {
            ExamSheetDTO dto = new ExamSheetDTO();
            dto.scanId = scanId;
            dto.pagemin = pagemin;
            dto.pagemax = pagemax;
            dto.name = UUID.randomUUID().toString();
            this.persistOrUpdate(dto);
        }
        return new Paged<>(ExamSheet.findExamSheetByScanAndPageminAndPagemax(scanId, pagemin, pagemax).page(page))
                .map(examSheet -> examSheetMapper.toDto((ExamSheet) examSheet));
    }

    public Paged<ExamSheetDTO> findExamSheetByExamId(Page page, Long examId) throws Exception {
        return new Paged<>(ExamSheet.getAll4ExamId(examId).page(page))
                .map(examSheet -> examSheetMapper.toDto((ExamSheet) examSheet));
    }

    public Paged<ExamSheetDTO> findOrCreateExamSheetByName(Page page, Long scanId, Integer pageInTemplate,
            Integer pageInScan) throws Exception {

        long nbrpage = ExamSheet.findExamSheetByScanWithoutMinusOne(scanId).count();

        if (nbrpage == pageInScan / pageInTemplate) {
            return new Paged<>(ExamSheet.findExamSheetByScanWithoutMinusOne(scanId).page(page))
                    .map(examSheet -> examSheetMapper.toDto((ExamSheet) examSheet));
        } else {
            for (int i = 0; i < pageInScan / pageInTemplate; i++) {
                long p = ExamSheet.findExamSheetByScanAndPageminAndPagemax(scanId, i * pageInTemplate,
                        i * pageInTemplate + (pageInTemplate - 1)).count();
                if (p == 0) {
                    ExamSheetDTO dto = new ExamSheetDTO();
                    dto.scanId = scanId;
                    dto.pagemin = i * pageInTemplate;
                    dto.pagemax = i * pageInTemplate + (pageInTemplate - 1);
                    dto.name = UUID.randomUUID().toString();
                    this.persistOrUpdate(dto);
                }
            }
            for (ExamSheet e : ExamSheet.findExamSheetByScanWithoutMinusOne(scanId).list()) {
                if (e.pagemax >= pageInScan) {
                    if (StudentResponse.findStudentResponsesbysheetId(e.id).count() == 0) {
                        e.delete();
                    } else {
                        e.pagemin = -1;
                        e.pagemax = -1;
                    }
                }
            }
            nbrpage = ExamSheet.findExamSheetByScanWithoutMinusOne(scanId).count();
            if (nbrpage == pageInScan / pageInTemplate) {
                return new Paged<>(ExamSheet.findExamSheetByScan(scanId).page(page))
                        .map(examSheet -> examSheetMapper.toDto((ExamSheet) examSheet));
            } else {
                throw (new Exception("Even in trying to create ExamSheets, I got a strange behavior"));
            }
        }
    }

    public ExamSheetDTO updateStudent(Long id, List<Long> studentsId) throws Exception {

        ExamSheet exO = ExamSheet.findById(id);
        if (exO == null) {
            throw new Exception("no update student");
        } else {
            List<Student> sts = Student.findStudentsbySheetId(id).list();
            List<Student> sts1 = sts.stream().filter(st -> !studentsId.contains(st.id)).collect(Collectors.toList());
            for (Student s : sts1) {
                s.examSheets.remove(exO);
                Student.persistOrUpdate(s);
            }

            List<Student> stsToUpdate = Student.findStudentsbyIds(studentsId).list();

            for (Student s : stsToUpdate) {
                List<ExamSheet> sheetsToRemove = s.examSheets.stream()
                        .filter(st -> st.scan.id == exO.scan.id && st.id != exO.id).collect(Collectors.toList());
                sheetsToRemove.forEach(st -> {
                    s.examSheets.remove(st);
                    Student.persistOrUpdate(s);
                });
                if (s.examSheets.stream().allMatch(ex -> ex.id != id)) {
                    s.examSheets.add(exO);
                    exO.students.add(s);
                    Student.persistOrUpdate(s);
                }
            }
            return this.examSheetMapper.toDto(exO);
        }

    }

    @Transactional
    public List<StudentResponseDTO> toggleTcomments(Long commentid, Long examId, int numero, boolean checked,
            List<Long> sheetsId, User updatedBy) {

        List<StudentResponse> response = StudentResponse
                .getAllStudentResponseWithExamIdNumeroAndSheetsId(examId, numero, sheetsId).list();
        TextComment comment = TextComment.findById(commentid);
        if (comment == null) {
            throw new UnsupportedOperationException("No comment found");
        } else {
            for (StudentResponse sr : response) {
                if (checked) {
                    sr.textcomments.add(comment);
                    comment.studentResponses.add(sr);
                } else {
                    sr.textcomments.remove(comment);
                    comment.studentResponses.remove(sr);
                }
                sr.lastModifiedDate = Instant.now();
                sr.correctedBy = updatedBy;

                sr = StudentResponse.persistOrUpdate(sr);
                comment = TextComment.persistOrUpdate(comment);
            }
        }
        if (checked) {

            List<Long> sheeitidswithsr = response.stream().map(sr -> sr.sheet.id).collect(Collectors.toList());
            List<Long> sheeitidswithoutsr = sheetsId.stream().filter(s -> !sheeitidswithsr.contains(s))
                    .collect(Collectors.toList());
            for (long s : sheeitidswithoutsr) {
                StudentResponse sr = new StudentResponse();
                sr.question = Question.findQuestionbyExamIdandnumero(examId, numero).firstResult();
                sr.sheet = ExamSheet.findById(s);
                sr.worststar = false;
                sr.star = false;
                sr.lastModifiedDate = Instant.now();
                sr.correctedBy = updatedBy;
                response.add(sr);
                sr.textcomments.add(comment);
                comment.studentResponses.add(sr);
                sr = StudentResponse.persistOrUpdate(sr);

                comment = TextComment.persistOrUpdate(comment);
            }
            ;
        }

        List<StudentResponseDTO> resultdto = studentResponseMapper.toDto(response);
        return resultdto;
    }


    @Transactional
    public List<StudentResponseDTO> toggleGcomments(Long commentid, long examId, int numero, boolean checked,
            List<Long> sheetsId, User updatedBy) {

        List<StudentResponse> response = StudentResponse
                .getAllStudentResponseWithExamIdNumeroAndSheetsId(examId, numero, sheetsId).list();
        GradedComment comment = GradedComment.findById(commentid);
        if (comment == null) {
            throw new UnsupportedOperationException("No comment found");
        } else {
            for (StudentResponse sr : response) {
                if (checked) {
                    sr.gradedcomments.add(comment);
                    comment.studentResponses.add(sr);

                } else {
                    sr.gradedcomments.remove(comment);
                    comment.studentResponses.remove(sr);
                }
                sr.lastModifiedDate = Instant.now();
                sr.correctedBy = updatedBy;
                sr = StudentResponse.persistOrUpdate(sr);
                comment = GradedComment.persistOrUpdate(comment);
            }
        }
        if (checked) {

            List<Long> sheeitidswithsr = response.stream().map(sr -> sr.sheet.id).collect(Collectors.toList());
            List<Long> sheeitidswithoutsr = sheetsId.stream().filter(s -> !sheeitidswithsr.contains(s))
                    .collect(Collectors.toList());
            for (long s : sheeitidswithoutsr) {
                StudentResponse sr = new StudentResponse();
                sr.question = Question.findQuestionbyExamIdandnumero(examId, numero).firstResult();

                sr.sheet = ExamSheet.findById(s);
                sr.worststar = false;
                sr.star = false;
                sr.lastModifiedDate = Instant.now();
                sr.correctedBy = updatedBy;
                sr.gradedcomments.add(comment);
                comment.studentResponses.add(sr);
                sr = StudentResponse.persistOrUpdate(sr);
                comment = GradedComment.persistOrUpdate(comment);
                response.add(sr);
            }
            ;
        }

        List<StudentResponseDTO> resultdto = studentResponseMapper.toDto(response);
        return resultdto;
    }

    @Transactional
    public List<StudentResponseDTO> toggleHcomments(Long commentid, long examId, int numero, int step,
            List<Long> sheetsId, User updatedBy) {

        List<StudentResponse> response = StudentResponse
                .getAllStudentResponseWithExamIdNumeroAndSheetsId(examId, numero, sheetsId).list();
        HybridGradedComment comment = HybridGradedComment.findById(commentid);
        if (comment == null || step > comment.step || step < 0) {
            throw new UnsupportedOperationException("No comment found or invalid step");
        } else {
            for (StudentResponse sr : response) {

                sr.lastModifiedDate = Instant.now();
                sr.correctedBy = updatedBy;
                sr = StudentResponse.persistOrUpdate(sr);
                List<Answer2HybridGradedComment> acs  =Answer2HybridGradedComment.findAllWithResponseIdAndHybridCommentId(sr.id, commentid).list();
                if (acs.size()>0){
                    acs.get(0).stepValue = step;
                    Answer2HybridGradedComment.persistOrUpdate(acs.get(0));
                }else {
                    Answer2HybridGradedComment ac = new Answer2HybridGradedComment();
                    ac.stepValue = step;
                    ac.hybridcomments = comment;
                    ac.studentResponse = sr;
                    sr.hybridcommentsValues.add(ac);
                    comment.valueAnswers.add(ac);
                    ac= Answer2HybridGradedComment.persistOrUpdate(ac);
                    sr = StudentResponse.persistOrUpdate(sr);
                    comment = HybridGradedComment.persistOrUpdate(comment);
                }
            }
        }

            List<Long> sheeitidswithsr = response.stream().map(sr -> sr.sheet.id).collect(Collectors.toList());
            List<Long> sheeitidswithoutsr = sheetsId.stream().filter(s -> !sheeitidswithsr.contains(s))
                    .collect(Collectors.toList());
            for (long s : sheeitidswithoutsr) {
                StudentResponse sr = new StudentResponse();
                sr.question = Question.findQuestionbyExamIdandnumero(examId, numero).firstResult();
                sr.sheet = ExamSheet.findById(s);
                sr.worststar = false;
                sr.star = false;
                sr.lastModifiedDate = Instant.now();
                sr.correctedBy = updatedBy;

                Answer2HybridGradedComment ac = new Answer2HybridGradedComment();
                ac.stepValue = step;
                ac.hybridcomments = comment;
                ac.studentResponse = sr;
                sr.hybridcommentsValues.add(ac);
                comment.valueAnswers.add(ac);
                ac = Answer2HybridGradedComment.persistOrUpdate(ac);
                sr = StudentResponse.persistOrUpdate(sr);
                comment = HybridGradedComment.persistOrUpdate(comment);
                response.add(sr);
            }

        List<StudentResponseDTO> resultdto = studentResponseMapper.toDto(response);
        return resultdto;
    }

    public List<StudentResponseDTO> updateNotes(Long examId, int numero, int step, List<Long> sheetsId, User updatedBy) {
        List<StudentResponse> response = StudentResponse
                .getAllStudentResponseWithExamIdNumeroAndSheetsId(examId, numero, sheetsId).list();
            for (StudentResponse sr : response) {
                sr.lastModifiedDate = Instant.now();
                sr.correctedBy = updatedBy;
                sr.quarternote = 4*step;
                sr = StudentResponse.persistOrUpdate(sr);
            }

            List<Long> sheeitidswithsr = response.stream().map(sr -> sr.sheet.id).collect(Collectors.toList());
            List<Long> sheeitidswithoutsr = sheetsId.stream().filter(s -> !sheeitidswithsr.contains(s))
                    .collect(Collectors.toList());
            for (long s : sheeitidswithoutsr) {
                StudentResponse sr = new StudentResponse();
                sr.question = Question.findQuestionbyExamIdandnumero(examId, numero).firstResult();

                sr.sheet = ExamSheet.findById(s);
                sr.worststar = false;
                sr.star = false;
                sr.quarternote = 4*step;
                sr.lastModifiedDate = Instant.now();
                sr.correctedBy = updatedBy;
                sr = StudentResponse.persistOrUpdate(sr);
                response.add(sr);
            }
            ;

        List<StudentResponseDTO> resultdto = studentResponseMapper.toDto(response);
        return resultdto;

    }


}
