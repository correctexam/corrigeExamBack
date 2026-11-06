package fr.istic.web.rest.vm;

import io.quarkus.runtime.annotations.RegisterForReflection;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
/**
 * View Model object for storing a user's credentials.
 */
@RegisterForReflection
@Schema(description = "Credentials used to log a user in.")
public class LoginVM {
    @NotNull
    @Size(min = 1, max = 50)
    @Schema(description = "Login of the user.", required=true, minLength=1, maxLength=50)
    public String username;

    @NotNull
    @Size(min = 4, max = 100)
    @Schema(description = "Password of the user.", required=true, minLength=4, maxLength=100)
    public String password;

    @Schema(description = "Whether the login should be remembered.")
    public Boolean rememberMe;

    @Override
    public String toString() {
        return "LoginVM{" + "username='" + username + '\'' + ", rememberMe=" + rememberMe + '}';
    }
}
