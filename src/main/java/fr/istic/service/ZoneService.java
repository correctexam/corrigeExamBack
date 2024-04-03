package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.Exam;
import fr.istic.domain.Question;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.Zone;
import fr.istic.service.customdto.ResizeZoneDTO;
import fr.istic.service.dto.ZoneDTO;
import fr.istic.service.mapper.ZoneMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class ZoneService {

    private final Logger log = LoggerFactory.getLogger(ZoneService.class);

    @Inject
    ZoneMapper zoneMapper;

    @Inject
    QuestionService questionSerivce;


    @Transactional
    public ZoneDTO persistOrUpdate(ZoneDTO zoneDTO) {
        log.debug("Request to save Zone : {}", zoneDTO);
        var zone = zoneMapper.toEntity(zoneDTO);
        zone = Zone.persistOrUpdate(zone);
        return zoneMapper.toDto(zone);
    }




    @Transactional
    void deleteZone (long id){
        Zone.deleteById(id);
    }


    /**
     * Delete the Zone by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete Zone : {}", id);
        Optional<Question> q = Question.findQuestionbyZoneId(id).firstResultOptional();
        if (q.isPresent()) {
            questionSerivce.cleanAllCorrectionAndComment(q.get());
            questionSerivce.delete(q.get().id);
        } else{
            Optional<Exam> exam =   Exam.findExamThatMatchZoneId(id).firstResultOptional();
            if (exam.isPresent()) {
                if (exam.get().namezone != null && exam.get().namezone.id.equals(id)){
                    Exam.removeNameZoneId(exam.get());
                }
                else if (exam.get().firstnamezone != null &&  exam.get().firstnamezone.id.equals(id)){
                    Exam.removeFirstNameZoneId(exam.get());

                }
                else if (exam.get().idzone != null && exam.get().idzone.id.equals(id)){
                    Exam.removeIdZoneId(exam.get());
                }
                else if (exam.get().notezone != null && exam.get().notezone.id.equals(id))
                {
                    Exam.removeNoteZoneId(exam.get());
                }
            }
            ZoneService.this.deleteZone(id);


        }
/*
        .ifPresentOrElse(q -> {
            q.delete();
        }, new Runnable() {
            public void run() {
                Exam.findExamThatMatchZoneId(id).firstResultOptional().ifPresentOrElse(exam-> {
                    if (exam.namezone != null && exam.namezone.id.equals(id)){
                        Exam.removeNameZoneId(exam);
                    }
                    else if (exam.firstnamezone != null &&  exam.firstnamezone.id.equals(id)){
                        Exam.removeFirstNameZoneId(exam);

                    }
                    else if (exam.idzone != null && exam.idzone.id.equals(id)){
                        Exam.removeIdZoneId(exam);
                    }
                    else if (exam.notezone != null && exam.notezone.id.equals(id))
                    {
                        Exam.removeNoteZoneId(exam);
                    }
                    ZoneService.this.deleteZone(id);
                },  new Runnable() {

                    public void run() {
                        ZoneService.this.deleteZone(id);
                    }
                }
                );


            }
        }); */
    }

    /**
     * Get one zone by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<ZoneDTO> findOne(Long id) {
        log.debug("Request to get Zone : {}", id);
        return Zone.findByIdOptional(id)
            .map(zone -> zoneMapper.toDto((Zone) zone));
    }

    /**
     * Get all the zones.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<ZoneDTO> findAll(Page page) {
        log.debug("Request to get all Zones");
        return new Paged<>(Zone.findAll().page(page))
            .map(zone -> zoneMapper.toDto((Zone) zone));
    }


        /**
     * Partially update a zone.
     *
     * @param zoneDTO the entity to update partially.
     * @return the persisted entity.
     */
    @Transactional
    public Optional<ZoneDTO> partialUpdate(ZoneDTO zoneDTO) {
        log.debug("Request to partially update Zone : {}", zoneDTO);
        Zone existingZone = Zone.findById(zoneDTO.id);

        zoneMapper.partialUpdate(existingZone, zoneDTO);
        Zone z1 = Zone.update(existingZone);
        return Optional.of(zoneMapper.toDto(z1));
    }

    @Transactional
    public Optional<ZoneDTO> partialResizeUpdate(ResizeZoneDTO rzoneDTO, long id) {
        log.debug("Request to partially update Zone : {}", rzoneDTO);
        Zone existingZone = Zone.findById(id);
        existingZone.xInit = rzoneDTO.getLeft();
        existingZone.yInit = rzoneDTO.getTop();
        existingZone.height = (int) Math.round(rzoneDTO.getY() * existingZone.height);
        existingZone.width = (int) Math.round(rzoneDTO.getX() * existingZone.width);
        Zone z1 = Zone.update(existingZone);
        return Optional.of(zoneMapper.toDto(z1));
    }




    public ZoneDTO[] getZone4Exam(Long examid) {
        log.debug("Request to get Zone 4 Exam: {}", examid);
        var resp = new ZoneDTO[3];
        Optional<Exam> ex = Exam.findByIdOptional(examid) ;
        if (ex.isPresent()){
            if (ex.get().namezone != null){
                 resp[0]=zoneMapper.toDto(ex.get().namezone);
            } else {
                 resp[0]=new ZoneDTO();
            }
            if (ex.get().firstnamezone != null){
                 resp[1]=zoneMapper.toDto(ex.get().firstnamezone);
            } else {
                 resp[1]=new ZoneDTO();
            }
            if (ex.get().idzone != null){
                 resp[2]=zoneMapper.toDto(ex.get().idzone);
            } else {
                 resp[2]=new ZoneDTO();
            }

        } else {
                 resp[0]=new ZoneDTO();
                 resp[0]=new ZoneDTO();
                 resp[0]=new ZoneDTO();

        }
        return resp;
    }

    public long getNumberOfStudentResponse4Zone(Long zoneid) {
        return Zone.getNumberOfStudentResponse4Zone(zoneid).count();
    }

    public long getNumberOfStudentResponse4Exam(Long examid) {
        return StudentResponse.getAll4ExamId(examid).count();
    }


}
