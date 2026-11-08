package fr.istic.web.rest.vm;

import io.quarkus.runtime.annotations.RegisterForReflection;

import fr.istic.service.dto.UserDTO;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * View Model extending the UserDTO, which is meant to be used in the user management UI.
 */
@RegisterForReflection
@Schema(description = "User view model used for registration and user management, extends UserDTO with a password.")
public class ManagedUserVM extends UserDTO {
    public static final int PASSWORD_MIN_LENGTH = 4;

    public static final int PASSWORD_MAX_LENGTH = 100;

    public ManagedUserVM() {
        // Empty constructor needed for Jackson.
    }

    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    @Schema(description = "Password of the user (4-100 chars).", required = true, minLength = 4, maxLength = 100)
    public String password;

    @Override
    public String toString() {
        return "ManagedUserVM{" + super.toString() + "} ";
    }
}
