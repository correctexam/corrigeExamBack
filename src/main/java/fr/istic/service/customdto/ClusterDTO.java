package fr.istic.service.customdto;

import java.util.List;
import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
@RegisterForReflection
@Schema(description = "Associates a template with the page indices of its clustered copies.")
public class ClusterDTO {
    @Schema(description = "Identifier of the template.")
    private int templat;
    @Schema(description = "Page indices of the copies belonging to the cluster.")
    private List<Integer> copies;

    public int getTemplat() { return templat; }
    public void setTemplat(int value) { this.templat = value; }

    public List<Integer> getCopies() { return copies; }
    public void setCopies(List<Integer> value) { this.copies = value; }
}
