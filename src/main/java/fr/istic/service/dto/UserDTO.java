package fr.istic.service.dto;

import fr.istic.config.Constants;
import fr.istic.domain.User;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * A DTO representing a user, with his authorities.
 */
@RegisterForReflection
@Schema(description = "User account with its authorities.")
public class UserDTO implements Serializable {

    @Schema(description = "Technical identifier of the user.")
    public Long id;

    @NotBlank
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    @Schema(description = "Login of the user (1-50 chars, alphanumeric).", required = true, minLength = 1, maxLength = 50)
    public String login;

    @Size(max = 50)
    @Schema(description = "First name of the user.", maxLength = 50)
    public String firstName;

    @Size(max = 50)
    @Schema(description = "Last name of the user.", maxLength = 50)
    public String lastName;

    @Email
    @Size(min = 5, max = 254)
    @Schema(description = "Email address of the user.", required = true, minLength = 5, maxLength = 254)
    public String email;

    @Size(max = 256)
    @Schema(description = "URL of the user's profile image.", maxLength = 256)
    public String imageUrl;

    @Schema(description = "Whether the user account is activated.", defaultValue = "false")
    public boolean activated = false;

    @Size(min = 2, max = 10)
    @Schema(description = "Locale key of the user (e.g. 'en', 'fr').", minLength = 2, maxLength = 10)
    public String langKey;

    @Schema(description = "Login of the creator of the account.")
    public String createdBy;

    @Schema(description = "Account creation date.")
    public Instant createdDate;

    @Schema(description = "Login of the last modifier of the account.")
    public String lastModifiedBy;

    @Schema(description = "Last modification date.")
    public Instant lastModifiedDate;

    @Schema(description = "Set of authorities (roles) of the user.")
    public Set<String> authorities;

    public UserDTO() {
        // Empty constructor needed for Jackson.
    }


    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return this.login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActivated() {
        return this.activated;
    }

    public boolean getActivated() {
        return this.activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getLangKey() {
        return this.langKey;
    }

    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedDate() {
        return this.createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedBy() {
        return this.lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Instant getLastModifiedDate() {
        return this.lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Set<String> getAuthorities() {
        return this.authorities;
    }

    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }


    public UserDTO(User user) {
        this.id = user.id;
        this.login = user.login;
        this.firstName = user.firstName;
        this.lastName = user.lastName;
        this.email = user.email;
        this.activated = user.activated;
        this.imageUrl = user.imageUrl;
        this.langKey = user.langKey;
        this.createdBy = user.createdBy;
        this.createdDate = user.createdDate;
        this.lastModifiedBy = user.lastModifiedBy;
        this.lastModifiedDate = user.lastModifiedDate;
        this.authorities = user.authorities.stream().map(authority -> authority.name).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return (
            "UserDTO{" +
            "login='" +
            login +
            '\'' +
            ", firstName='" +
            firstName +
            '\'' +
            ", lastName='" +
            lastName +
            '\'' +
            ", email='" +
            email +
            '\'' +
            ", imageUrl='" +
            imageUrl +
            '\'' +
            ", activated=" +
            activated +
            ", langKey='" +
            langKey +
            '\'' +
            ", createdBy=" +
            createdBy +
            ", createdDate=" +
            createdDate +
            ", lastModifiedBy='" +
            lastModifiedBy +
            '\'' +
            ", lastModifiedDate=" +
            lastModifiedDate +
            ", authorities=" +
            authorities +
            "}"
        );
    }
}
