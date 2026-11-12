package fr.istic.web.rest;

import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.domain.Exam;
import fr.istic.domain.User;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.ExamService;
import fr.istic.service.SecurityService;
import fr.istic.service.customdto.answernotebooks.AnswersNoteBook;
import fr.istic.web.rest.errors.AccountResourceException;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Exam structure creation from a notebook description.
 * <p>
 * Split out of the former {@code ExtendedAPI} class (T3): the REST paths are unchanged.
 */
@Tag(name = "Notebook", description = "Exam structure creation from a notebook description.")
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class NotebookResource {

    private final Logger log = LoggerFactory.getLogger(NotebookResource.class);

    @Inject
    SecurityService securityService;

    @Inject
    ExamService examService;

    @Operation(summary = "Create the notebook exam structure", description = "Creates the exam structure (notebook) from the given answers.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "Notebook structure created"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "500", description = "Could not create the notebook structure")
        }
    )
    @POST
    @Path("/createNoteBookExamStructure")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response createNoteBookExamStructure(
            List<AnswersNoteBook> answersNoteBook,
            @Context final SecurityContext ctx) {
        if (!(answersNoteBook.size()> 0) || !securityService.canAccess(ctx, answersNoteBook.get(0).getExamId(), Exam.class)) {
            return Response.status(403, "Current user cannot access this ressource").build();
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
        try{
            this.examService.createNoteBookExamStructure(answersNoteBook,user.get());
        }catch (Exception e){
            log.error("Error in createNoteBookExamStructure",e);
            return Response.status(500, "Error in createNoteBookExamStructure").build();
        }
        return Response.ok().build();

    }
}
