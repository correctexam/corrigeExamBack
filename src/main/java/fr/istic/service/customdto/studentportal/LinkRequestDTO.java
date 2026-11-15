package fr.istic.service.customdto.studentportal;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@RegisterForReflection
@Schema(description = "Request of a link to the student portal.")
public class LinkRequestDTO {
    @Schema(description = "E-mail address of the student.", required = true)
    public String email;

    @Schema(description = "Solution of the ALTCHA challenge (base 64 payload of the widget).", required = true)
    public String altcha;

    @Schema(description = "Language of the e-mail: fr or en (default fr).")
    public String lang;
}
