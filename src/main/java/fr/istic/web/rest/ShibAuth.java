package fr.istic.web.rest;

import fr.istic.security.jwt.TokenProvider;
import fr.istic.service.AuthenticationService;
import fr.istic.service.UserService;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import io.quarkus.arc.profile.IfBuildProfile;

/**
 * Shibboleth authentication: the SP (Apache mod_shib) in front of the application passes the
 * user attributes as request headers.
 *
 * <p>Reads the headers through JAX-RS {@link HttpHeaders} (Quarkus REST) instead of the
 * servlet {@code HttpServletRequest}, which is not available to Quarkus REST endpoints.</p>
 */
@IfBuildProfile("shib")
@Path("/api/shib")
@RequestScoped
public class ShibAuth {
    private final Logger log = LoggerFactory.getLogger(ShibAuth.class);
    final AuthenticationService authenticationService;
    final TokenProvider tokenProvider;
    final UserService userService;
    static final String SHIBBOLETH_UID = "eppn";
    static final String SHIBBOLETH_MAIL = "mail";
    static final String SHIBBOLETH_LAST_NAME = "sn";
    static final String SHIBBOLETH_FIRST_NAME = "givenName";

    @ConfigProperty(name="correctexam.shib.redirect.address",defaultValue = "https://correctexam-test.univ-rennes.fr?shib=true")
    String shibAdressRedirect;

    @Inject
    public ShibAuth(AuthenticationService authenticationService, TokenProvider tokenProvider, UserService userService) {
        this.authenticationService = authenticationService;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    void logHeadersShib(HttpHeaders headers) {
        if (log.isDebugEnabled()) {
            log.debug("Shibboleth headers: {}={}, {}={}, {}={}, {}={}", SHIBBOLETH_UID, headers.getHeaderString(SHIBBOLETH_UID),
                    SHIBBOLETH_MAIL, headers.getHeaderString(SHIBBOLETH_MAIL), SHIBBOLETH_LAST_NAME,
                    headers.getHeaderString(SHIBBOLETH_LAST_NAME), SHIBBOLETH_FIRST_NAME,
                    headers.getHeaderString(SHIBBOLETH_FIRST_NAME));
        }
    }

    @GET
    @Path("/redirection")
    @PermitAll
    public Response askForRedirection(@Context HttpHeaders headers)
    {
        log.debug("SHIB REDIRECTION");
        logHeadersShib(headers);
        return Response.seeOther(URI.create(shibAdressRedirect)).build();
    }

    @GET
    @Path("/authenticate")
    @PermitAll
    public Response getShibAuth(@Context HttpHeaders headers) {
        log.debug("SHIB AUTH SERVICE CONTACTED GET");
        String login = headers.getHeaderString(SHIBBOLETH_UID);
        String email = headers.getHeaderString(SHIBBOLETH_MAIL);
        String lastName = headers.getHeaderString(SHIBBOLETH_LAST_NAME);
        String firstName = headers.getHeaderString(SHIBBOLETH_FIRST_NAME);
        logHeadersShib(headers);

        // User not logged (missing header used to throw a NullPointerException)
        if (login == null || login.isBlank())
            return Response.status(400).build();

        // User not in database, create a new account
        var user = userService.getUserWithAuthoritiesByLogin(login);
        if (user.isEmpty())
            userService.createUserOnlyLogin(login, email, lastName, firstName);

        QuarkusSecurityIdentity identity = authenticationService.authenticateNoPwd(login);
        String jwt = tokenProvider.createToken(identity, true);
        return Response.ok().entity(new UserJWTController.JWTToken(jwt)).header("Authorization", "Bearer " + jwt).build();
    }
}
