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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST controller to manage authorities.
 */
@Tag(
    name = "Authority",
    description = "Management of authorities (roles): retrieve the list of all authorities."
)
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
    @Operation(summary = "Get all authorities", description = "Returns a list of all authorities (roles). Requires the admin role.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The list of authorities",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = String.class))
            ),
            @APIResponse(responseCode = "403", description = "Admin role required")
        }
    )
    @GET
    @Path("/authorities")
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public List<String> getAuthorities() {
        return userService.getAuthorities();
    }
}
