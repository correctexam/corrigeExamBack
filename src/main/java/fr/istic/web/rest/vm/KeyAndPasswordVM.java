package fr.istic.web.rest.vm;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * View Model object for storing the user's key and password.
 */
@RegisterForReflection
@Schema(description = "Data required to finish a password reset (reset key and new password).")
public class KeyAndPasswordVM {
    @Schema(description = "Password reset key received by email.", required = true)
    public String key;

    @Schema(description = "New password of the user (4-100 chars).", required = true)
    public String newPassword;

    @Override
    public String toString() {
        return "KeyAndPasswordVM{" + "key='" + key + '\'' + ", newPassword='" + newPassword + '\'' + '}';
    }
}
