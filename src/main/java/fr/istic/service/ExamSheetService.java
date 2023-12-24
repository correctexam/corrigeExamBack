package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.Student;
import fr.istic.service.dto.ExamSheetDTO;
import fr.istic.service.mapper.ExamSheetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class ExamSheetService {

    private final Logger log = LoggerFactory.getLogger(ExamSheetService.class);

    @Inject
    ExamSheetMapper examSheetMapper;

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
                  long p = ExamSheet.findExamSheetByScanAndPageminAndPagemax(scanId,pagemin,pagemax).count();
                    if (p==0){
                        ExamSheetDTO dto = new ExamSheetDTO();
                        dto.scanId = scanId;
                        dto.pagemin = pagemin;
                        dto.pagemax = pagemax;
                        dto.name = UUID.randomUUID().toString();
                        this.persistOrUpdate(dto);
                    }
                    return new Paged<>(ExamSheet.findExamSheetByScanAndPageminAndPagemax(scanId,pagemin,pagemax).page(page))
                .map(examSheet -> examSheetMapper.toDto((ExamSheet) examSheet));
            }



    public Paged<ExamSheetDTO> findOrCreateExamSheetByName(Page page, Long scanId, Integer pageInTemplate,
            Integer pageInScan) throws Exception {


            long nbrpage =     ExamSheet.findExamSheetByScanWithoutMinusOne(scanId).count();


            if (nbrpage == pageInScan/pageInTemplate){
                 return new Paged<>(ExamSheet.findExamSheetByScan(scanId).page(page))
            .map(examSheet -> examSheetMapper.toDto((ExamSheet) examSheet));
            } else {
                for (int i = 0; i< pageInScan/pageInTemplate ;i++){
                    long p = ExamSheet.findExamSheetByScanAndPageminAndPagemax(scanId,i*pageInTemplate,i*pageInTemplate + (pageInTemplate-1)).count();
                    if (p==0){
                        ExamSheetDTO dto = new ExamSheetDTO();
                        dto.scanId = scanId;
                        dto.pagemin = i*pageInTemplate;
                        dto.pagemax = i*pageInTemplate + (pageInTemplate-1);
                        dto.name = UUID.randomUUID().toString();
                        this.persistOrUpdate(dto);
                    }
                }
            nbrpage =   ExamSheet.findExamSheetByScanWithoutMinusOne(scanId).count();
            if (nbrpage == pageInScan/pageInTemplate){
                    return new Paged<>(ExamSheet.findExamSheetByScan(scanId).page(page))
                .map(examSheet -> examSheetMapper.toDto((ExamSheet) examSheet));
            } else {
                throw (new Exception("Even in trying to create ExamSheets, I got a strange behavior"));
            }
        }
    }

    public ExamSheetDTO updateStudent(Long id, List<Long> studentsId) throws Exception {

        ExamSheet exO = ExamSheet.findById(id);
        if (exO == null){
            throw new Exception("no update student");
        } else {
            List<Student> sts = Student.findStudentsbySheetId(id).list();
            List<Student> sts1 = sts.stream().filter(st -> !studentsId.contains(st.id)).collect(Collectors.toList());
            for (Student s : sts1){
                s.examSheets.remove(exO);
                Student.persistOrUpdate(s);
            }

            List<Student> stsToUpdate = Student.findStudentsbyIds(studentsId).list();


            for (Student s : stsToUpdate){
                List<ExamSheet> sheetsToRemove = s.examSheets.stream().filter(st -> st.scan.id ==exO.scan.id && st.id!= exO.id).collect(Collectors.toList());
                sheetsToRemove.forEach(st-> {
                    s.examSheets.remove(st);
                    Student.persistOrUpdate(s);
                });
                if (s.examSheets.stream().allMatch(ex -> ex.id != id)){
                    s.examSheets.add(exO);
                    exO.students.add(s);
                    Student.persistOrUpdate(s);
                }
            }
        return this.examSheetMapper.toDto(exO);
        }

    }


}
