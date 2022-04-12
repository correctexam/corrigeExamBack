package fr.istic.domain;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import javax.json.bind.annotation.JsonbTransient;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * A Template.
 */
@Entity
@Table(name = "template")
@Cacheable
@RegisterForReflection
public class Template extends PanacheEntityBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    public String name;

    @Lob
    @Column(name = "content")
    public byte[] content;

    @Column(name = "content_content_type")
      public String contentContentType;

    @Column(name = "mark")
    public Boolean mark;

    @Column(name = "auto_map_student_copy_to_list")
    public Boolean autoMapStudentCopyToList;

    @OneToOne(mappedBy = "template")
    @JsonIgnore
    public Exam exam;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Template)) {
            return false;
        }
        return id != null && id.equals(((Template) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Template{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", content='" + content + "'" +
            ", contentContentType='" + contentContentType() + "'" +
            ", mark='" + mark + "'" +
            ", autoMapStudentCopyToList='" + autoMapStudentCopyToList + "'" +
            "}";
    }

    public Template update() {
        return update(this);
    }

    public Template persistOrUpdate() {
        return persistOrUpdate(this);
    }

    public static Template update(Template template) {
        if (template == null) {
            throw new IllegalArgumentException("template can't be null");
        }
        var entity = Template.<Template>findById(template.id);
        if (entity != null) {
            entity.name = template.name;
            entity.content = template.content;
            entity.mark = template.mark;
            entity.autoMapStudentCopyToList = template.autoMapStudentCopyToList;
            entity.exam = template.exam;
        }
        return entity;
    }

    public static Template persistOrUpdate(Template template) {
        if (template == null) {
            throw new IllegalArgumentException("template can't be null");
        }
        if (template.id == null) {
            persist(template);
            return template;
        } else {
            return update(template);
        }
    }


}
