package fr.istic.web.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestForm;

import fr.istic.domain.Exam;
import fr.istic.domain.Scan;
import fr.istic.domain.Template;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.CacheStudentPdfFService;
import fr.istic.service.CacheUploadService;
import fr.istic.service.FichierS3Service;
import fr.istic.service.ScanService;
import fr.istic.service.SecurityService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.StreamingOutput;

/**
 * Upload and download of scans, templates, cache files and student PDFs.
 * <p>
 * Split out of the former {@code ExtendedAPI} class (T3): the REST paths are unchanged.
 */
@Tag(name = "Exam files", description = "Upload and download of scans, templates, cache files and student PDFs.")
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ExamFileResource {

    @Inject
    SecurityService securityService;

    @Inject
    CacheUploadService cacheUploadService;

    @Inject
    ScanService scanService;

    @Inject
    CacheStudentPdfFService cacheStudentPdfFService;

    @Inject
    FichierS3Service fichierS3Service;

    @Operation(summary = "Upload a student PDF export", description = "Uploads a multipart PDF file used to build the corrected copies of the exam.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "PDF uploaded"),
            @APIResponse(responseCode = "500", description = "Upload failed")
        }
    )
    @POST
    @Path("/uploadExportFinalStudent/{examId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response fileUploadStudentPdf(@RestForm("file") org.jboss.resteasy.reactive.multipart.FileUpload file,
            @Parameter(description = "ID of the exam", required = true) @PathParam("examId") long examId) {
        try {
            cacheStudentPdfFService.uploadFile(file, examId);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();

        }
        return Response.ok().build();
    }

    @Operation(summary = "Upload a cache file", description = "Uploads a multipart file into the shared cache store.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "File uploaded"),
            @APIResponse(responseCode = "500", description = "Upload failed")
        }
    )
    @POST
    @Path("/uploadCache")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response fileUpload(
            @RestForm("file") org.jboss.resteasy.reactive.multipart.FileUpload file) {
        try {
            cacheUploadService.uploadFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();

        }
        return Response.ok().build();
    }

    @Operation(summary = "Upload a scan file", description = "Uploads a multipart PDF file and attaches it to the given scan without merging.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "Scan uploaded"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "500", description = "Upload failed")
        }
    )
    @POST
    @Path("/uploadScan/{scanId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response scanUpload(@RestForm("file") org.jboss.resteasy.reactive.multipart.FileUpload file, @Parameter(description = "ID of the scan", required = true) @PathParam("scanId") long scanId,
            @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, scanId, Scan.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        try {
            scanService.uploadFile(file, scanId, false);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();

        }
        return Response.ok().build();
    }

    @Operation(summary = "Upload and merge a scan file", description = "Uploads a multipart PDF file and merges it into the given scan.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "Scan uploaded and merged"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "500", description = "Upload failed")
        }
    )
    @POST
    @Path("/uploadAndMergeScan/{scanId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response scanAndMergeUpload(@RestForm("file") org.jboss.resteasy.reactive.multipart.FileUpload file, @Parameter(description = "ID of the scan", required = true) @PathParam("scanId") long scanId,
            @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, scanId, Scan.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        try {
            scanService.uploadFile(file, scanId, true);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
        return Response.ok().build();
    }

    @Operation(summary = "Get cached template page", description = "Returns the content of the cached template page of the exam.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The cached page content",
                content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = SchemaType.STRING))
            ),
            @APIResponse(responseCode = "500", description = "Could not retrieve the cached page")
        }
    )
    @GET
    @Path("/getCachePageInTemplate/{examId}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getCachePageInTemplate(@Parameter(description = "ID of the exam", required = true) @PathParam("examId") long examId) {
        try {
            return Response
                    .status(Response.Status.OK)
                    .entity(cacheUploadService.getCachePageInTemplate(examId))
                    .type(MediaType.TEXT_PLAIN)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();

        }
    }

    @Operation(summary = "Get cached aligned page", description = "Returns the content of the cached aligned page of the exam.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The cached aligned page content",
                content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = SchemaType.STRING))
            ),
            @APIResponse(responseCode = "500", description = "Could not retrieve the cached page")
        }
    )
    @GET
    @Path("/getCacheAlignPage/{examId}/{pageId}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getCachePageAlign(@Parameter(description = "ID of the exam", required = true) @PathParam("examId") long examId, @Parameter(description = "Page number in the exam", required = true) @PathParam("pageId") int pageId) {
        try {
            return Response
                    .status(Response.Status.OK)
                    .entity(cacheUploadService.getAlignPage(examId, pageId, false))
                    .type(MediaType.TEXT_PLAIN)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();

        }
    }
    /*
     * @GET
     *
     * @Path("/getCacheAlignPageSqlite/{examId}/{pageId}")
     *
     * @Produces(MediaType.TEXT_PLAIN)
     * public Response getCacheAlignPageSqlite(@PathParam("examId") long
     * examId, @PathParam("pageId") int pageId) {
     * try {
     * return Response
     * .status(Response.Status.OK)
     * .entity(cacheUploadService.getAlignPageSqlite(examId, pageId, false))
     * .type(MediaType.TEXT_PLAIN)
     * .build();
     *
     * } catch (Exception e) {
     * return Response.serverError().build();
     *
     * }
     * }
     */

    @Operation(summary = "Get cached non-aligned page", description = "Returns the content of the cached non-aligned page of the exam.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The cached non-aligned page content",
                content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = SchemaType.STRING))
            ),
            @APIResponse(responseCode = "500", description = "Could not retrieve the cached page")
        }
    )
    @GET
    @Path("/getCacheNonAlignPage/{examId}/{pageId}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getCachePageNoAlign(@Parameter(description = "ID of the exam", required = true) @PathParam("examId") long examId, @Parameter(description = "Page number in the exam", required = true) @PathParam("pageId") int pageId) {
        try {
            return Response
                    .status(Response.Status.OK)
                    .entity(cacheUploadService.getAlignPage(examId, pageId, true))
                    .type(MediaType.TEXT_PLAIN)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();

        }
    }
    @Operation(summary = "Get cache timestamp", description = "Returns the last modification timestamp of the cache for the exam.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The cache timestamp",
                content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = SchemaType.STRING))
            ),
            @APIResponse(responseCode = "500", description = "Could not retrieve the cache timestamp")
        }
    )
    @GET
    @Path("/getCacheTimeStamp/{examId}/{cacheType}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getCacheTimeStamp(
        @Parameter(description = "ID of the exam", required = true) @PathParam("examId") long examId,
        @Parameter(description = "IType of the cache (e.g., 'sqlite', 'indexdb')", required = true) @PathParam("cacheType") String cacheType) {
            try{
            return Response
                    .status(Response.Status.OK)
                    .entity(cacheUploadService.getCacheTimeStamp(examId,cacheType))
                    .type(MediaType.TEXT_PLAIN)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();

        }
    }

    /*
     * @GET
     *
     * @Path("/getCacheNonAlignPageSqlite/{examId}/{pageId}")
     *
     * @Produces(MediaType.TEXT_PLAIN)
     * public Response getCachePageNoAlignSqlite(@PathParam("examId") long
     * examId, @PathParam("pageId") int pageId) {
     * try {
     * return Response
     * .status(Response.Status.OK)
     * .entity(cacheUploadService.getAlignPageSqlite(examId, pageId, true))
     * .type(MediaType.TEXT_PLAIN)
     * .build();
     *
     *
     * } catch (Exception e) {
     * return Response.serverError().build();
     *
     * }
     * }
     */

    @Operation(summary = "Get a cached file", description = "Streams a cached file (PDF or image) stored in the shared cache as an octet stream.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The cached file content",
                content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM)
            ),
            @APIResponse(responseCode = "204", description = "The file does not exist")
        }
    )
    @GET
    @Path("/getCache/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile(@Parameter(description = "Name of the cached file to retrieve", required = true) @PathParam("fileName") String fileName) {
        try {
            return Response.ok(
                    new StreamingOutput() {
                        @Override
                        public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                            InputStream source = null;
                            try {
                                source = cacheUploadService.getFile(fileName);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return;
                            }
                            byte[] buf = new byte[8192];
                            int length;
                            while ((length = source.read(buf)) != -1) {
                                outputStream.write(buf, 0, length);
                            }
                        }
                    }, MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment;filename=" + fileName).build();
        } catch (Exception e) {

            e.printStackTrace();
            return Response.noContent().build();
        }
    }

    @Operation(summary = "Get a template PDF", description = "Streams the PDF of the template.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The template PDF",
                content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM)
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "204", description = "The template PDF does not exist")
        }
    )
    @GET
    @Path("/getTemplatePdf/{templateId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getTemplate(@Parameter(description = "ID of the template", required = true) @PathParam("templateId") long templateId, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, templateId, Template.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        try {
            if (this.fichierS3Service.isObjectExist("template/" + templateId + ".pdf")) {

                return Response.ok(
                        new StreamingOutput() {
                            @Override
                            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                                InputStream source = null;
                                try {

                                    source = fichierS3Service.getObject("template/" + templateId + ".pdf");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return;
                                }
                                byte[] buf = new byte[8192];
                                int length;
                                while ((length = source.read(buf)) != -1) {
                                    outputStream.write(buf, 0, length);
                                }
                            }
                        }, MediaType.APPLICATION_OCTET_STREAM)
                        .header("Content-Disposition", "attachment;filename=" + "template_" + templateId + ".pdf")
                        .build();
            } else {
                return Response.noContent().build();

            }
        }

        catch (Exception e) {

            e.printStackTrace();
            return Response.noContent().build();
        }
    }

    @Operation(summary = "Get a scan PDF", description = "Streams the PDF of the scan.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The scan PDF",
                content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM)
            ),
            @APIResponse(responseCode = "204", description = "The scan PDF does not exist")
        }
    )
    @GET
    @Path("/getScanPdf/{scanId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    // @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getScan(@Parameter(description = "ID of the scan", required = true) @PathParam("scanId") long scanId, @Context SecurityContext ctx) {
        /*
         * if (!securityService.canAccess(ctx, scanId, Scan.class)) {
         * return Response.status(403,
         * "Current user cannot access to this ressource").build();
         * }
         */
        try {
            if (this.fichierS3Service.isObjectExist("scan/" + scanId + ".pdf")) {

                return Response.ok(
                        new StreamingOutput() {
                            @Override
                            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                                InputStream source = null;
                                try {

                                    source = fichierS3Service.getObject("scan/" + scanId + ".pdf");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return;
                                }
                                byte[] buf = new byte[8192];
                                int length;
                                while ((length = source.read(buf)) != -1) {
                                    outputStream.write(buf, 0, length);
                                }
                            }
                        }, MediaType.APPLICATION_OCTET_STREAM)
                        .header("Content-Disposition", "attachment;filename=" + "scan_" + scanId + ".pdf")
                        .build();
            } else {
                return Response.noContent().build();

            }
        }

        catch (Exception e) {

            e.printStackTrace();
            return Response.noContent().build();
        }
    }
}
