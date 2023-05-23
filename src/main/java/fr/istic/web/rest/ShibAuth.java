package fr.istic.web.rest;

import fr.istic.security.jwt.TokenProvider;
import fr.istic.service.AuthenticationService;
import fr.istic.service.UserService;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path("/api/shib")
@RequestScoped
public class ShibAuth {
    private final Logger log = LoggerFactory.getLogger(ShibAuth.class);
    final AuthenticationService authenticationService;
    final TokenProvider tokenProvider;
    final UserService userService;
    HttpServletRequest request;
    final String shibbolethUID = "eppn";
    final String shibbolethMail = "mail";
    final String shibbolethLastName = "sn";
    final String shibbolethFirstName = "givenName";

    @Inject
    public ShibAuth(AuthenticationService authenticationService, TokenProvider tokenProvider, UserService userService, HttpServletRequest request) {
        this.authenticationService = authenticationService;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
        this.request = request;
    }

    public void logHeadersShib()
    {
        log.debug("HEADERS SHIB");
        log.debug(request.getHeader(shibbolethUID));
        log.debug(request.getHeader(shibbolethMail));
        log.debug(request.getHeader(shibbolethLastName));
        log.debug(request.getHeader(shibbolethFirstName));
    }

    @GET
    @Path("/redirection")
    @PermitAll
    public Response askForRedirection()
    {
        log.debug("SHIB REDIRECTION");
        logHeadersShib();
        return Response.seeOther(URI.create("https://correctexam-test.univ-rennes.fr?shib=true")).build();
    }

    @GET
    @Path("/authenticate")
    @PermitAll
    public Response getShibAuth() {
        log.debug("SHIB AUTH SERVICE CONTACTED GET");
        String login = request.getHeader(shibbolethUID);
        String email = request.getHeader(shibbolethMail);
        String lastName = request.getHeader(shibbolethLastName);
        String firstName = request.getHeader(shibbolethFirstName);
        logHeadersShib();

        // User not logged
        if (login.isEmpty())
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
