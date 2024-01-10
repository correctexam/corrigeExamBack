package fr.istic.web.rest;

import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.UserService;
import java.util.List;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * REST controller to manage authorities.
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class AuthorityResource {

    final UserService userService;

    @Inject
    public AuthorityResource(UserService userService) {
        this.userService = userService;
    }

    /**
     * Gets a list of all roles.
     *
     * @return a string list of all roles.
     */
    @GET
    @Path("/authorities")
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public List<String> getAuthorities() {
        return userService.getAuthorities();
    }
}
