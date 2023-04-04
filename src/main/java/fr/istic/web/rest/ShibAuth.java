package fr.istic.web.rest;

import fr.istic.security.jwt.TokenProvider;
import fr.istic.service.AuthenticationService;
import fr.istic.service.UserService;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/api/shib")
@RequestScoped
public class ShibAuth {
    private final Logger log = LoggerFactory.getLogger(ShibAuth.class);
    final AuthenticationService authenticationService;
    final TokenProvider tokenProvider;
    final UserService userService;
    @Inject
    HttpServletRequest requestNotUsed;

    @Inject
    public ShibAuth(AuthenticationService authenticationService, TokenProvider tokenProvider, UserService userService) {
        this.authenticationService = authenticationService;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    @POST
    @Path("/authenticate")
    @PermitAll
    public Response postShibAuth() {
        log.error("SHIB AUTH SERVICE CONTACTED POST");
        HttpServletRequest HSR = CDI.current().select(HttpServletRequest.class).get();
        var headersName = HSR.getHeaderNames();
        headersName.asIterator().forEachRemaining(headerName -> {
            log.error(headerName + " : " + HSR.getHeader(headerName));
        });
        return Response.ok("SHIB AUTH POST SERVICE CONTACTED").build();
    }

    @GET
    @Path("/authenticate")
    @PermitAll
    public Response getShibAuth() {

        log.error("SHIB AUTH SERVICE CONTACTED GET");
        HttpServletRequest HSR = CDI.current().select(HttpServletRequest.class).get();
        String login = HSR.getHeader("eppn");
        String email = HSR.getHeader("mail");
        String lastName = HSR.getHeader("sn");
        String firstName = HSR.getHeader("givenName");
        log.error(login);
        log.error(email);
        log.error(lastName);
        log.error(firstName);
        if (userService.getUserWithAuthoritiesByLogin(login).isEmpty()) {
            userService.createUserOnlyLogin(login, email, lastName, firstName);
        }
        QuarkusSecurityIdentity identity = authenticationService.authenticateNoPwd(login);
        String jwt = tokenProvider.createToken(identity, true);
        return Response.ok().entity(new UserJWTController.JWTToken(jwt)).header("Authorization", "Bearer " + jwt).build();
    }
}
