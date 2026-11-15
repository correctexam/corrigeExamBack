package fr.istic.service.customdto.studentportal;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@RegisterForReflection
@Schema(description = "Opening of a student session with the token of the link sent by e-mail.")
public class SessionRequestDTO {
    @Schema(description = "Token of the link (single use).", required = true)
    public String token;
}
