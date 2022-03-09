package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.Template;
import fr.istic.service.dto.TemplateDTO;
import fr.istic.service.mapper.TemplateMapper;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class TemplateService {

    private final Logger log = LoggerFactory.getLogger(TemplateService.class);

    @Inject
    TemplateMapper templateMapper;

    @Transactional
    public TemplateDTO persistOrUpdate(TemplateDTO templateDTO) {
        log.debug("Request to save Template : {}", templateDTO);
        var template = templateMapper.toEntity(templateDTO);
        template = Template.persistOrUpdate(template);
        return templateMapper.toDto(template);
    }

    /**
     * Delete the Template by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete Template : {}", id);
        Template.findByIdOptional(id).ifPresent(template -> {
            template.delete();
        });
    }

    /**
     * Get one template by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<TemplateDTO> findOne(Long id) {
        log.debug("Request to get Template : {}", id);
        return Template.findByIdOptional(id)
            .map(template -> templateMapper.toDto((Template) template)); 
    }

    /**
     * Get all the templates.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<TemplateDTO> findAll(Page page) {
        log.debug("Request to get all Templates");
        return new Paged<>(Template.findAll().page(page))
            .map(template -> templateMapper.toDto((Template) template));
    }



}
