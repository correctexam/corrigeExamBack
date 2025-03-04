package fr.istic.service.customdto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class EntityId {

    public EntityId(long id){
        this.id = id;
    }

    long id;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
