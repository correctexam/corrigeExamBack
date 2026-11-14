package fr.istic.service.customdto.studentportal;

import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@RegisterForReflection
@Schema(description = "Student session: bearer token for /api/student/me.")
public class StudentSessionDTO {
    @Schema(description = "JWT with the ROLE_STUDENT role, to send as Authorization: Bearer.")
    public String idToken;

    @Schema(description = "End of the session (no refresh: ask for a new link).")
    public Instant expiresAt;

    @Schema(description = "E-mail address of the student.")
    public String email;

    public StudentSessionDTO() {
    }

    public StudentSessionDTO(String idToken, Instant expiresAt, String email) {
        this.idToken = idToken;
        this.expiresAt = expiresAt;
        this.email = email;
    }
}
