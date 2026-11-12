package fr.istic.web.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import fr.istic.domain.Course;
import fr.istic.domain.CourseGroup;
import fr.istic.domain.Student;
import fr.istic.domain.User;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.SecurityService;
import fr.istic.service.customdto.StudentMassDTO;
import fr.istic.web.rest.errors.AccountResourceException;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Bulk creation, update and removal of the students of a course.
 * <p>
 * Split out of the former {@code ExtendedAPI} class (T3): the REST paths are unchanged.
 */
@Tag(name = "Course students", description = "Bulk creation, update and removal of the students of a course.")
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class CourseStudentResource {

    @Inject
    SecurityService securityService;

    @Operation(summary = "Create students in bulk", description = "Creates or updates a list of students and assigns them to the course groups.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "Students created"),
            @APIResponse(responseCode = "400", description = "Invalid student data"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @POST
    @Path("createstudentmasse")
    @Transactional
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response createAllStudent(StudentMassDTO dto, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, dto.getCourse(), Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        Course c = Course.findById(dto.getCourse());
        List<String> groupes = dto.getStudents().stream().map(e -> e.getGroupe()).distinct()
                .collect(Collectors.toList());
        Map<String, CourseGroup> groupesentities = new HashMap<>();
        groupes.forEach(g -> {
            long count = CourseGroup.findByNameandCourse(c.id, g).count();
            if (count > 0) {
                CourseGroup cgdest = CourseGroup.findByNameandCourse(c.id, g).firstResult();
                groupesentities.put(g, cgdest);

            } else {
                CourseGroup g1 = new CourseGroup();
                g1.course = c;
                g1.groupName = g;
                groupesentities.put(g, g1);

            }

        });
        dto.getStudents().forEach(s -> {
            Student st = new Student();
            st.ine = s.getIne();
            st.name = s.getNom();
            st.firstname = s.getPrenom();
            st.mail = s.getMail();
            Student st1 = Student.persistOrUpdate(st);
            st1.groups.add(groupesentities.get(s.getGroupe()));
            groupesentities.get(s.getGroupe()).students.add(st1);
            Student.flush();
            CourseGroup.flush();

        });

        groupesentities.values().forEach(gc -> {
            CourseGroup.persistOrUpdate(gc);
            CourseGroup.flush();
        });

        return Response.ok().build();

    }

    @Operation(summary = "Update a student", description = "Updates the first name, last name and mail of a student identified by its INE.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "Student updated"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @PUT
    @Path("updatestudent/{courseid}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Transactional
    public Response updatestudent4Course(fr.istic.service.customdto.StudentDTO student,
            @Parameter(description = "ID of the course", required = true) @PathParam("courseid") long courseid, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, courseid, Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        var st = Student.findStudentsbyCourseIdAndINE(courseid, student.getIne()).firstResult();
        st.firstname = student.getPrenom();
        st.name = student.getNom();
        st.mail = student.getMail();
        Student.persistOrUpdate(st);
        return Response.ok().entity(st).build();
    }

    @Operation(summary = "Move a student to another group", description = "Removes a student from their current group and adds them to the target group of the course.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "Student group updated"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @PUT
    @Path("updatestudentgroup/{courseid}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Transactional
    public Response updatestudentgroup(fr.istic.service.customdto.StudentDTO student,
            @Parameter(description = "ID of the course", required = true) @PathParam("courseid") long courseid, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, courseid, Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        var st = Student.findStudentsbyCourseIdAndINE(courseid, student.getIne()).firstResult();
        CourseGroup cgorigin = CourseGroup.findByStudentINEandCourse(courseid, student.getIne()).firstResult();
        cgorigin.students.remove(st);
        CourseGroup.persistOrUpdate(cgorigin);
        long count = CourseGroup.findByNameandCourse(courseid, student.getGroupe()).count();
        if (count > 0) {
            CourseGroup cgdest = CourseGroup.findByNameandCourse(courseid, student.getGroupe()).firstResult();
            cgdest.students.add(st);
            CourseGroup.persistOrUpdate(cgdest);
            return Response.ok().build();

        } else {
            CourseGroup cgdest = new CourseGroup();
            cgdest.groupName = student.getGroupe();
            cgdest.course = Course.findById(courseid);
            cgdest.students.add(st);
            CourseGroup.persistOrUpdate(cgdest);
            return Response.ok().build();
        }
    }

    @Operation(summary = "Delete a student from a group", description = "Removes a student from their group and deletes the student from the course.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "Student deleted"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @PUT
    @Path("deletestudentgroup/{courseid}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Transactional
    public Response deletestudentgroup(fr.istic.service.customdto.StudentDTO student,
            @Parameter(description = "ID of the course", required = true) @PathParam("courseid") long courseid, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, courseid, Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        var st = Student.findStudentsbyCourseIdAndINE(courseid, student.getIne()).firstResult();
        CourseGroup cgorigin = CourseGroup.findByStudentINEandCourse(courseid, student.getIne()).firstResult();
        cgorigin.students.remove(st);
        CourseGroup.persistOrUpdate(cgorigin);
        Student.deleteById(st.id);
        return Response.ok().entity(st).build();
    }

    @Operation(summary = "Update the INE of a student", description = "Updates the INE of a student identified by first and last name.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "Student INE updated"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @PUT
    @Path("updatestudentine/{courseid}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Transactional
    public Response updatestudentine(fr.istic.service.customdto.StudentDTO student,
            @Parameter(description = "ID of the course", required = true) @PathParam("courseid") long courseid, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, courseid, Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        var st = Student.findStudentsbyCourseIdAndFirsNameAndLastName(courseid, student.getPrenom(), student.getNom())
                .firstResult();
        st.ine = student.getIne();
        Student.persistOrUpdate(st);
        return Response.ok().entity(st).build();
    }

    @Operation(summary = "Get students of a course", description = "Returns all students enrolled in the course with their group.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The students of the course",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = fr.istic.service.customdto.StudentDTO.class))
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @GET
    @Path("getstudentcours/{courseid}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Transactional
    public Response getAllStudent4Course(@Parameter(description = "ID of the course", required = true) @PathParam("courseid") long courseid, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, courseid, Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        Course c = Course.findById(courseid);
        List<fr.istic.service.customdto.StudentDTO> students = new ArrayList<>();
        if (c != null) {
            c.groups.forEach(g -> {
                CourseGroup.findOneWithEagerRelationships(g.id).get().students.forEach(s -> {
                    fr.istic.service.customdto.StudentDTO sdto = new fr.istic.service.customdto.StudentDTO();
                    sdto.setIne(s.ine);
                    sdto.setNom(s.name);
                    sdto.setPrenom(s.firstname);
                    sdto.setMail(s.mail);
                    sdto.setGroupe(g.groupName);
                    students.add(sdto);
                });
            });

        }
        return Response.ok().entity(students).build();
    }

    @Operation(summary = "Delete all students of a course", description = "Deletes all groups and students of the course.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "Students deleted"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "500", description = "Current user could not be resolved")
        }
    )
    @DELETE
    @Path("deletegroupstudents/{courseid}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Transactional
    public Response deleteAllStudent4Course(@Parameter(description = "ID of the course", required = true) @PathParam("courseid") long courseid, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, courseid, Course.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        var userLogin = Optional
                .ofNullable(ctx.getUserPrincipal().getName());
        if (!userLogin.isPresent()) {
            throw new AccountResourceException("Current user login not found");
        }
        var user = User.findOneByLogin(userLogin.get());
        if (!user.isPresent()) {
            throw new AccountResourceException("User could not be found");
        }
        Course c = Course.findById(courseid);
        c.groups.forEach(g -> {
            g.students.forEach(st -> {
                st.groups.remove(g);
                Student.update(st);
            });
            CourseGroup.deleteById(g.id);
        });
        c.groups.clear();
        Course.update(c);
        return Response.ok().build();
    }
}
