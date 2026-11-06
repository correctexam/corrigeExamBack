package fr.istic.web.rest;

import fr.istic.service.ManagementInfoService;
import fr.istic.service.dto.ManagementInfoDTO;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(
    name = "ManagementInfo",
    description = "Application management information: active profiles and active version details."
)
@Path("/management/info")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class ManagementInfoResource {

    private final ManagementInfoService managementInfoService;

    @Inject
    public ManagementInfoResource(ManagementInfoService managementInfoService) {
        this.managementInfoService = managementInfoService;
    }

    @Operation(summary = "Get application management info", description = "Returns the application's active profiles and version details.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The application management information",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ManagementInfoDTO.class))
            ),
            @APIResponse(responseCode = "500", description = "Could not retrieve management information")
        }
    )
    @GET
    public Response info(){
        return Response.ok(managementInfoService.getManagementInfo()).build();
    }
}
