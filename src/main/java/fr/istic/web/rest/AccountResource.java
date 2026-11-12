package fr.istic.web.rest;

import fr.istic.domain.User;
import fr.istic.service.InvalidPasswordException;
import fr.istic.service.MailService;
import fr.istic.service.UserService;
import fr.istic.service.UsernameAlreadyUsedException;
import fr.istic.service.dto.PasswordChangeDTO;
import fr.istic.service.dto.UserDTO;
import fr.istic.web.rest.errors.EmailAlreadyUsedException;
import fr.istic.web.rest.errors.EmailNotFoundException;
import fr.istic.web.rest.errors.LoginAlreadyUsedException;
import fr.istic.web.rest.vm.KeyAndPasswordVM;
import fr.istic.web.rest.vm.ManagedUserVM;

import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;

import java.security.Principal;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for managing the current user's account.
 */
@Tag(
    name = "Account",
    description = "Management of the current user's account: get, update, register, activate, password reset."
)
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class AccountResource {
    private final Logger log = LoggerFactory.getLogger(AccountResource.class);

    private static class AccountResourceException extends RuntimeException {

        private AccountResourceException(String message) {
            super(message);
        }
    }

    final MailService mailService;

    final UserService userService;

    @Inject
    public AccountResource(MailService mailService, UserService userService) {
        this.mailService = mailService;
        this.userService = userService;
    }

    /**
     * {@code GET /account} : get the current user.
     *
     * @return the current user.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user couldn't be returned.
     */
    @Operation(summary = "Get the current user", description = "Returns the information of the authenticated user's account.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The current user",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserDTO.class))
            ),
            @APIResponse(responseCode = "401", description = "User not authenticated"),
            @APIResponse(responseCode = "500", description = "Could not retrieve the current user")
        }
    )
    @GET
    @Path("/account")
    @Authenticated
    public UserDTO getAccount(@Context SecurityContext ctx) {
        return userService
            .getUserWithAuthoritiesByLogin(ctx.getUserPrincipal().getName())
            .map(UserDTO::new)
            .orElseThrow(() -> new AccountResourceException("User could not be found"));
    }

    /**
     * {@code POST /account} : update the current user information.
     *
     * @param userDTO the current user information.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already used.
     * @throws RuntimeException          {@code 500 (Internal Server Error)} if the user login wasn't found.
     */
    @Operation(
        summary = "Update the current user",
        description = "Updates the information (first name, last name, email, language, image) of the current user."
    )
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "Information updated"),
            @APIResponse(responseCode = "400", description = "Email already used by another account"),
            @APIResponse(responseCode = "401", description = "User not authenticated"),
            @APIResponse(responseCode = "500", description = "Current user not found")
        }
    )
    @POST
    @Path("/account")
    @Authenticated
    public Response saveAccount(@Valid UserDTO userDTO, @Context SecurityContext ctx) {
        var userLogin = Optional
            .ofNullable(ctx.getUserPrincipal().getName())
            .orElseThrow(() -> new AccountResourceException("Current user login not found"));
        var existingUser = User.findOneByEmailIgnoreCase(userDTO.email);
        if (existingUser.isPresent() && (!existingUser.get().login.equalsIgnoreCase(userLogin))) {
            throw new EmailAlreadyUsedException();
        }
        var user = User.findOneByLogin(userLogin);
        if (!user.isPresent()) {
            throw new AccountResourceException("User could not be found");
        }
        userService.updateUser(userLogin, userDTO.firstName, userDTO.lastName, userDTO.email, userDTO.langKey, userDTO.imageUrl);
        return Response.ok().build();
    }

    /**
     * {@code POST /register} : register the user.
     *
     * @param managedUserVM the managed user View Model.
     * @throws InvalidPasswordException  {@code 400 (Bad Request)} if the password is incorrect.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already used.
     * @throws LoginAlreadyUsedException {@code 400 (Bad Request)} if the login is already used.
     */
    @Operation(summary = "Register a new user", description = "Creates a new user account and sends an activation email.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "201", description = "Account created, activation email sent"),
            @APIResponse(responseCode = "400", description = "Invalid password, or email/login already in use")
        }
    )
    @POST
    @Path("/register")
    @Blocking
    @PermitAll
    public CompletionStage<Response> registerAccount(@Valid ManagedUserVM managedUserVM) {
        if (!checkPasswordLength(managedUserVM.password)) {
            throw new InvalidPasswordException();
        }
        try {
            var user = userService.registerUser(managedUserVM, managedUserVM.password);
            return mailService.sendActivationEmail(user).thenApply(it -> Response.created(null).build());
        } catch (UsernameAlreadyUsedException e) {
            throw new LoginAlreadyUsedException();
        } catch (fr.istic.service.EmailAlreadyUsedException e) {
            throw new EmailAlreadyUsedException();
        }
    }

    /**
     * {@code GET /activate} : activate the registered user.
     *
     * @param key the activation key.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user couldn't be activated.
     */
    @Operation(
        summary = "Activate a registered user",
        description = "Activates the user account from the activation key received by email."
    )
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "Account activated"),
            @APIResponse(responseCode = "500", description = "No user found for this activation key")
        }
    )
    @GET
    @Path("/activate")
    @PermitAll
    public void activateAccount(
        @Parameter(description = "Activation key sent by email", required = true) @QueryParam(value = "key") String key
    ) {
        var user = userService.activateRegistration(key);
        if (!user.isPresent()) {
            throw new AccountResourceException("No user was found for this activation key");
        }
    }

    /**
     * {@code GET /authenticate} : check if the user is authenticated, and return its login.
     *
     * @param ctx the request security context.
     * @return the login if the user is authenticated.
     */
    @Operation(
        summary = "Check authentication",
        description = "Indicates whether the user is authenticated and returns their login (empty string otherwise)."
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "Login of the authenticated user (or empty string)",
                content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = SchemaType.STRING))
            )
        }
    )
    @GET
    @Path("/authenticate")
    @PermitAll
    @Produces(MediaType.TEXT_PLAIN)
    public String isAuthenticated(@Context SecurityContext ctx) {
        log.debug("REST request to check if the current user is authenticated");
        return Optional.ofNullable(ctx.getUserPrincipal()).map(Principal::getName).orElse("");
    }

    /**
     * {@code POST /account/change-password} : changes the current user's password.
     *
     * @param passwordChangeDto current and new password.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the new password is incorrect.
     */
    @Operation(
        summary = "Change the current user's password",
        description = "Changes the current user's password (current and new password)."
    )
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "Password changed"),
            @APIResponse(responseCode = "400", description = "Invalid new password"),
            @APIResponse(responseCode = "401", description = "User not authenticated")
        }
    )
    @POST
    @Path("/account/change-password")
    @Authenticated
    public Response changePassword(PasswordChangeDTO passwordChangeDto, @Context SecurityContext ctx) {
        var userLogin = Optional
            .ofNullable(ctx.getUserPrincipal().getName())
            .orElseThrow(() -> new AccountResourceException("Current user login not found"));
        if (!checkPasswordLength(passwordChangeDto.newPassword)) {
            throw new InvalidPasswordException();
        }
        userService.changePassword(userLogin, passwordChangeDto.currentPassword, passwordChangeDto.newPassword);
        return Response.ok().build();
    }

    /**
     * {@code POST /account/reset-password/init} : Send an email to reset the password of the user.
     *
     * @param mail the mail of the user.
     * @throws EmailNotFoundException {@code 400 (Bad Request)} if the email address is not registered.
     */
    @Operation(summary = "Request a password reset", description = "Sends a password reset email to the provided address.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "Reset email sent"),
            @APIResponse(responseCode = "400", description = "No account associated with this email")
        }
    )
    @POST
    @Path("/account/reset-password/init")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response requestPasswordReset(String mail) {
        mailService.sendPasswordResetMail(userService.requestPasswordReset(mail).orElseThrow(EmailNotFoundException::new));
        return Response.ok().build();
    }

    /**
     * {@code POST /account/reset-password/finish} : Finish to reset the password of the user.
     *
     * @param keyAndPassword the generated key and the new password.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the password is incorrect.
     * @throws RuntimeException         {@code 500 (Internal Server Error)} if the password could not be reset.
     */
    @Operation(
        summary = "Finish a password reset",
        description = "Completes the password reset from the received key and the new password."
    )
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "Password reset"),
            @APIResponse(responseCode = "400", description = "Invalid password or wrong key"),
            @APIResponse(responseCode = "500", description = "No user found for this reset key")
        }
    )
    @POST
    @Path("/account/reset-password/finish")
    public Response finishPasswordReset(KeyAndPasswordVM keyAndPassword) {
        if (!checkPasswordLength(keyAndPassword.newPassword)) {
            throw new InvalidPasswordException();
        }
        var user = userService.completePasswordReset(keyAndPassword.newPassword, keyAndPassword.key);

        if (!user.isPresent()) {
            throw new AccountResourceException("No user was found for this reset key");
        }
        return Response.ok().build();
    }

    private static boolean checkPasswordLength(String password) {
        return (
            !password.isEmpty() &&
            password.length() >= ManagedUserVM.PASSWORD_MIN_LENGTH &&
            password.length() <= ManagedUserVM.PASSWORD_MAX_LENGTH
        );
    }
}
