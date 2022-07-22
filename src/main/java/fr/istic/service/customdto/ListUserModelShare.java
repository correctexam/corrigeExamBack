package fr.istic.service.customdto;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ListUserModelShare {
    private List<UserModelShare> shared = new ArrayList<>();
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
