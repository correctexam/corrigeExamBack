package fr.istic.service.customdto;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class EntityId {

    public EntityId(Long id){
        this.id = id;
    }

    Long id;

    public Long  getId() {
        return this.id;
    }

    public void setIds(Long  id) {
        this.id = id;
    }
}
