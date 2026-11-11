package fr.istic.web.rest;

import java.time.Duration;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.StorageException;
import fr.istic.service.StorageOrphanReport;
import fr.istic.service.StorageOrphanService;

/**
 * Administration of the object storage: detection and cleanup of orphaned objects
 * (files whose scan, template or exam has been deleted).
 */
@Tag(name = "StorageAdmin", description = "Object storage maintenance (administrators only).")
@Path("/api/admin/storage")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
@RolesAllowed(AuthoritiesConstants.ADMIN)
public class StorageAdminResource {

    private final Logger log = LoggerFactory.getLogger(StorageAdminResource.class);

    @Inject
    StorageOrphanService storageOrphanService;

    @Operation(summary = "List orphaned storage objects",
            description = "Dry run: lists the objects whose scan, template or exam no longer exists. Nothing is deleted.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The orphan report"),
        @APIResponse(responseCode = "409", description = "S3 storage is not enabled"),
        @APIResponse(responseCode = "503", description = "The storage could not be listed")
    })
    @GET
    @Path("/orphans")
    public Response listOrphans(
            @Parameter(description = "Orphans more recent than this number of hours are ignored")
            @QueryParam("minAgeHours") @DefaultValue("24") long minAgeHours) {
        return run(true, minAgeHours);
    }

    @Operation(summary = "Delete orphaned storage objects",
            description = "Deletes the orphaned objects. dryRun defaults to true: call with dryRun=false to actually delete.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "The cleanup report"),
        @APIResponse(responseCode = "400", description = "Invalid parameters"),
        @APIResponse(responseCode = "409", description = "S3 storage is not enabled"),
        @APIResponse(responseCode = "503", description = "The storage could not be listed")
    })
    @POST
    @Path("/orphans/cleanup")
    public Response cleanOrphans(
            @Parameter(description = "When true (default), nothing is deleted")
            @QueryParam("dryRun") @DefaultValue("true") boolean dryRun,
            @Parameter(description = "Orphans more recent than this number of hours are kept (minimum 1)")
            @QueryParam("minAgeHours") @DefaultValue("24") long minAgeHours) {
        if (!dryRun && minAgeHours < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("minAgeHours must be at least 1 when dryRun=false").build();
        }
        return run(dryRun, minAgeHours);
    }

    private Response run(boolean dryRun, long minAgeHours) {
        if (minAgeHours < 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("minAgeHours must be positive").build();
        }
        try {
            StorageOrphanReport report = storageOrphanService.cleanOrphans(dryRun, Duration.ofHours(minAgeHours));
            return Response.ok(report).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        } catch (StorageException e) {
            log.error("Storage orphan analysis failed", e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(e.getMessage()).build();
        }
    }
}
