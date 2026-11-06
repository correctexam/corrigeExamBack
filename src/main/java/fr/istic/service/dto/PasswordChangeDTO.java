package fr.istic.service.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * A DTO representing a password change required data - current and new password.
 */
@RegisterForReflection
@Schema(description = "Data required to change a password (current and new password).")
public class PasswordChangeDTO {
    @Schema(description = "Current password of the user.", required = true)
    public String currentPassword;
    @Schema(description = "New password of the user (4-100 chars).", required = true)
    public String newPassword;

    public PasswordChangeDTO() {
        // Empty constructor needed for Jackson.
    }

    public PasswordChangeDTO(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }
}
