package fr.istic.service;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.quarkus.panache.common.Page;
import fr.istic.domain.Template;
import fr.istic.service.dto.TemplateDTO;
import fr.istic.service.dto.TemplateDTOContent;
import fr.istic.service.mapper.TemplateContentMapper;
import fr.istic.service.mapper.TemplateMapper;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class TemplateService {

    private final Logger log = LoggerFactory.getLogger(TemplateService.class);

    @Inject
    FichierS3Service fichierS3Service;

    @Inject
    TemplateMapper templateMapper;

    @Inject
    TemplateContentMapper templateContentMapper;

    @Transactional
    public TemplateDTOContent persistOrUpdate(TemplateDTOContent templateDTO) {
        log.debug("Request to save Template : {}", templateDTO);
        var template = templateContentMapper.toEntity(templateDTO);
        byte[] bytes = templateDTO.content;
        template.content = null;
        template = Template.persistOrUpdate(template);

        try {
            fichierS3Service.putObject("template/" + template.id + ".pdf", bytes, templateDTO.contentContentType);
        } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException | IOException e) {
            e.printStackTrace();
        }

        TemplateDTOContent dto = templateContentMapper.toDto(template);
        dto.content = bytes;

        return dto;
    }

    /**
     * Delete the Template by id.
     *
     * @param id the id of the entity.
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete Template : {}", id);
/*        Template.findByIdOptional(id).ifPresent(template -> {
            template.delete();
        });*/
        Optional<Template> templateop =Template.findByIdOptional(id);
        templateop.ifPresent(template -> {
            if (this.fichierS3Service.isObjectExist("template/" + template.id + ".pdf")) {
                    try {
                        this.fichierS3Service.deleteObject("template/" + template.id + ".pdf");
                    } catch (InvalidKeyException | ErrorResponseException | InsufficientDataException
                            | InternalException | InvalidResponseException | NoSuchAlgorithmException | ServerException
                            | XmlParserException | IllegalArgumentException | IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

            }

            template.delete();
        });

    }

    /**
     * Get one template by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<TemplateDTOContent> findOne(Long id) {
        log.debug("Request to get Template : {}", id);

        Optional<Template> templateop = Template.findByIdOptional(id);
        if (templateop.isPresent()) {
            Template template = templateop.get();
            if (this.fichierS3Service.isObjectExist("template/" + template.id + ".pdf")) {
                byte[] bytes;
                try {
                    bytes = IOUtils.toByteArray(this.fichierS3Service.getObject("template/" + template.id + ".pdf"));
                    template.content = bytes;

                } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException | IOException e) {
                    e.printStackTrace();
                }
            } else {
                byte[] bytes = template.content;
                try {
                    fichierS3Service.putObject("template/" + template.id + ".pdf", bytes, template.contentContentType);
                } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException | IOException e) {
                    e.printStackTrace();
                }

            }
        }
        return templateop
                .map(scan -> templateContentMapper.toDto((Template) scan));
    }

    /**
     * Get all the templates.
     *
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<TemplateDTO> findAll(Page page) {
        log.debug("Request to get all Templates");
        return new Paged<>(Template.findAll().page(page))
                .map(template -> templateMapper.toDto((Template) template));
    }

}
