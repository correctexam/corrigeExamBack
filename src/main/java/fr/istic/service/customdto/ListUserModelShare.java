package fr.istic.service.customdto;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "Users available for and currently sharing a resource.")
public class ListUserModelShare {
    @Schema(description = "Users the resource is shared with.")
    private List<UserModelShare> shared = new ArrayList<>();
    @Schema(description = "Users available to share the resource with.")
    private List<UserModelShare> availables= new ArrayList<>();;

    public List<UserModelShare> getShared() {
        return shared;
    }
    public void setShared(List<UserModelShare> shared) {
        this.shared = shared;
    }
    public List<UserModelShare> getAvailables() {
        return availables;
    }
    public void setAvailables(List<UserModelShare> availables) {
        this.availables = availables;
    }

}
