package fr.istic.web.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestForm;

import fr.istic.domain.Course;
import fr.istic.domain.User;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.CourseExportService;
import fr.istic.service.CourseImportService;
import fr.istic.service.SecurityService;
import fr.istic.service.dto.CourseDTO;
import fr.istic.web.rest.errors.AccountResourceException;

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
 * JSON export and import of courses and exams, with or without student data.
 * <p>
 * Split out of the former {@code ExtendedAPI} class (T3): the REST paths are unchanged.
 */
@Tag(name = "Course import/export", description = "JSON export and import of courses and exams, with or without student data.")
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class CourseImportExportResource {

    @Inject
    SecurityService securityService;

    @Inject
    CourseExportService courseExportService;

    @Inject
    CourseImportService courseImportService;

    @Operation(summary = "Export a course", description = "Streams a JSON export of the whole course with its students.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The course export as a JSON attachment",
                content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM)
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "204", description = "The export could not be generated")
        }
    )
    @GET
    @Path("/exportCourse/{courseId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response exportCourse(@Parameter(description = "ID of the course", required = true) @PathParam("courseId") long courseId, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, courseId, Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        try {
            return Response.ok(
                    new StreamingOutput() {
                        @Override
                        public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                            OutputStreamWriter w = new OutputStreamWriter(outputStream);
                            Gson gson = new GsonBuilder().create();
                            gson.toJson(courseExportService.export(courseId, true, -1), w);
                            w.flush();
                            w.close();
                        };

                    }, MediaType.APPLICATION_OCTET_STREAM)

                    .header("Content-Disposition", "attachment;filename=" + courseId + ".json")
                    .build();
        } catch (Exception e) {

            e.printStackTrace();
            return Response.noContent().build();
        }
    }

    @Operation(summary = "Export an exam", description = "Streams a JSON export of the course restricted to the given exam.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The exam export as a JSON attachment",
                content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM)
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "204", description = "The export could not be generated")
        }
    )
    @GET
    @Path("/exportExam/{courseId}/{examId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response exportExam(@Parameter(description = "ID of the course", required = true) @PathParam("courseId") long courseId, @Parameter(description = "ID of the exam", required = true) @PathParam("examId") long examId,
            @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, courseId, Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        try {
            return Response.ok(

                    new StreamingOutput() {
                        @Override
                        public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                            OutputStreamWriter w = new OutputStreamWriter(outputStream);
                            Gson gson = new GsonBuilder().create();
                            gson.toJson(courseExportService.export(courseId, true, examId), w);
                            w.flush();
                            w.close();
                        };

                    }, MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment;filename=" + courseId + ".json")
                    .build();
        } catch (Exception e) {

            e.printStackTrace();
            return Response.noContent().build();
        }
    }

    @Operation(summary = "Export a course without student data", description = "Streams a JSON export of the course without any student data.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The course export as a JSON attachment",
                content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM)
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "204", description = "The export could not be generated")
        }
    )
    @GET
    @Path("/exportCourseWithoutStudentData/{courseId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })

    public Response exportCourseWithoutStudentData(@Parameter(description = "ID of the course", required = true) @PathParam("courseId") long courseId, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, courseId, Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        try {
            return Response.ok(
                    new StreamingOutput() {
                        @Override
                        public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                            OutputStreamWriter w = new OutputStreamWriter(outputStream);
                            Gson gson = new GsonBuilder().create();
                            gson.toJson(courseExportService.export(courseId, false, -1), w);
                            w.flush();
                            w.close();
                        };

                    }, MediaType.APPLICATION_OCTET_STREAM)

                    .header("Content-Disposition", "attachment;filename=" + courseId + ".json")
                    .build();
        } catch (Exception e) {

            e.printStackTrace();
            return Response.noContent().build();
        }
    }

    @Operation(summary = "Export an exam without student data", description = "Streams a JSON export of the course restricted to the given exam without any student data.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The exam export as a JSON attachment",
                content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM)
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "204", description = "The export could not be generated")
        }
    )
    @GET
    @Path("/exportExamWithoutStudentData/{courseId}/{examId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response exportExamWithoutStudentData(@Parameter(description = "ID of the course", required = true) @PathParam("courseId") long courseId, @Parameter(description = "ID of the exam", required = true) @PathParam("examId") long examId,
            @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, courseId, Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        try {
            return Response.ok(
                    new StreamingOutput() {
                        @Override
                        public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                            OutputStreamWriter w = new OutputStreamWriter(outputStream);
                            Gson gson = new GsonBuilder().create();
                            gson.toJson(courseExportService.export(courseId, false, examId), w);
                            w.flush();
                            w.close();
                        };

                    }, MediaType.APPLICATION_OCTET_STREAM)

                    .header("Content-Disposition", "attachment;filename=" + courseId + ".json")
                    .build();
        } catch (Exception e) {

            e.printStackTrace();
            return Response.noContent().build();
        }
    }

    @Operation(summary = "Import a course", description = "Imports a course from an uploaded JSON file, including its students.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The imported course",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CourseDTO.class))
            ),
            @APIResponse(responseCode = "204", description = "No course could be created"),
            @APIResponse(responseCode = "400", description = "Invalid import data"),
            @APIResponse(responseCode = "500", description = "Import failed")
        }
    )
    @POST
    @Path("/importCourse")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })

    public Response importCourse(@RestForm("file") org.jboss.resteasy.reactive.multipart.FileUpload file, @Context SecurityContext ctx) {
        var userLogin = Optional
                .ofNullable(ctx.getUserPrincipal().getName());
        if (!userLogin.isPresent()) {
            throw new AccountResourceException("Current user login not found");
        }
        var user = User.findOneByLogin(userLogin.get());
        if (!user.isPresent()) {
            throw new AccountResourceException("User could not be found");
        }
        if (!userLogin.equals("system")) {
            try {
                CourseDTO dto = courseImportService.importCourse(file, user.get(), true);
                if (dto != null) {
                    return Response.ok().entity(dto).build();
                } else {
                    return Response.noContent().build();

                }
            } catch (Exception e) {
                return Response.serverError().build();

            }
        }
        throw new AccountResourceException("User could not be found");
    }

    @Operation(summary = "Import a course without student data", description = "Imports a course from an uploaded JSON file without any student data.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The imported course",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CourseDTO.class))
            ),
            @APIResponse(responseCode = "204", description = "No course could be created"),
            @APIResponse(responseCode = "400", description = "Invalid import data"),
            @APIResponse(responseCode = "500", description = "Import failed")
        }
    )
    @POST
    @Path("/importCourseWithoutStudentData")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })

    public Response importCourseWithoutStudentData(@RestForm("file") org.jboss.resteasy.reactive.multipart.FileUpload file,
            @Context SecurityContext ctx) {
        var userLogin = Optional
                .ofNullable(ctx.getUserPrincipal().getName());
        if (!userLogin.isPresent()) {
            throw new AccountResourceException("Current user login not found");
        }
        var user = User.findOneByLogin(userLogin.get());
        if (!user.isPresent()) {
            throw new AccountResourceException("User could not be found");
        }
        if (!userLogin.equals("system")) {
            try {
                CourseDTO dto = courseImportService.importCourse(file, user.get(), false);
                if (dto != null) {
                    return Response.ok().entity(dto).build();
                } else {
                    return Response.noContent().build();

                }
            } catch (Exception e) {
                return Response.serverError().build();

            }
        }
        throw new AccountResourceException("User could not be found");
    }
}
