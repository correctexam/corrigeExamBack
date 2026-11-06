package fr.istic.service.customdto;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "Wrapper for a single entity identifier.")
public class EntityId {

    public EntityId(Long id){
        this.id = id;
    }

    @Schema(description = "Identifier of the entity.")
    Long id;

    public Long  getId() {
        return this.id;
    }

    public void setIds(Long  id) {
        this.id = id;
    }
}
