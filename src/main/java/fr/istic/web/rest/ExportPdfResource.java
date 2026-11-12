package fr.istic.web.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import fr.istic.domain.Exam;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.ExportPdfService;
import fr.istic.service.SecurityService;
import fr.istic.service.customdto.exportpdf.ExportPDFDto;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Data used by the browser to build the PDF of corrected copies.
 * <p>
 * Split out of the former {@code ExtendedAPI} class (T3): the REST paths are unchanged.
 */
@Tag(name = "Export PDF", description = "Data used by the browser to build the PDF of corrected copies.")
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ExportPdfResource {

    @Inject
    SecurityService securityService;

    @Inject
    ExportPdfService exportPdfService;

    @Operation(summary = "Export exam PDF data", description = "Returns the exportable PDF structure (zones, sheets, notes and comments) for the given exam.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The exported PDF data",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExportPDFDto.class))
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @GET
    @Path("exportpdf/{examId}")
    @Transactional
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getExportPdfRoute(@Parameter(description = "ID of the exam", required = true) @PathParam("examId") long examId, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        ExportPDFDto exportPDFDto = exportPdfService.getExportPdf(examId, null);
        return Response.ok().entity(exportPDFDto).build();
    }

    @Operation(summary = "Export PDF data for one sheet", description = "Returns the exportable PDF structure for a specific sheet of the exam identified by its UUID.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The exported PDF data for the sheet",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExportPDFDto.class))
            ),
            @APIResponse(responseCode = "400", description = "No sheet found for the given UUID")
        }
    )
    @GET
    @Path("exportpdf4sheet/{examId}/{sheetuuid}")
    @Transactional
    public Response getExportPdfRoute4sheet(@Parameter(description = "ID of the exam", required = true) @PathParam("examId") long examId,
            @Parameter(description = "Name (UUID) of the exam sheet", required = true) @PathParam("sheetuuid") String sheetname) {
        ExportPDFDto exportPDFDto = exportPdfService.getExportPdf(examId, sheetname);
        return Response.ok().entity(exportPDFDto).build();
    }
}
