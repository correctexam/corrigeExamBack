package fr.istic.service.customdto;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "List of professor email addresses.")
public class ListEmailProf {
    @Schema(description = "Email addresses of the professors.")
    private List<String> emails = new ArrayList<>();

    public List<String> getEmails() {
        return emails;
    }
    public void setEmails(List<String> emails) {
        this.emails = emails;
    }
}
