package fr.istic.web.rest;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import fr.istic.domain.Question;
import fr.istic.domain.StudentResponse;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.QuestionService;
import fr.istic.service.SecurityService;
import fr.istic.service.customdto.WorstAndBestSolution;
import fr.istic.service.customdto.exportcomments.AnswersWithPredictionDto;

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
 * Best and starred answers, comments and predictions used during correction.
 * <p>
 * Split out of the former {@code ExtendedAPI} class (T3): the REST paths are unchanged.
 */
@Tag(name = "Answer review", description = "Best and starred answers, comments and predictions used during correction.")
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class AnswerReviewResource {

    @Inject
    SecurityService securityService;

    @Inject
    QuestionService questionService;

    @Operation(summary = "Get the best answer for a question", description = "Returns the names of the best answers for a given question number of the exam.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The names of the best answers",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = String.class))
            )
        }
    )
    @GET
    @Path("getBestAnswer/{examId}/{nume}")
    @Transactional
    public Response getBestAnswer(@Parameter(description = "ID of the exam", required = true) @PathParam("examId") long examId, @Parameter(description = "Question number to retrieve the best answer for", required = true) @PathParam("nume") int nume) {
        List<String> res = StudentResponse.getBestAnswerforQuestionNoAndExamId(examId, nume).list().stream()
                .map(e -> e.name).collect(Collectors.toList());

        return Response.ok().entity(res).build();
    }

    @Operation(summary = "Get the best and worst answers", description = "Returns the best and worst answers for every question of the exam.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The best and worst solutions per question",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = WorstAndBestSolution.class))
            )
        }
    )
    @GET
    // @RolesAllowed({AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN})
    @Path("getAllStarAnswer/{examId}")
    @Transactional
    public Response getAllStarAnswer(@Parameter(description = "ID of the exam", required = true) @PathParam("examId") long examId) {
        Base64.Encoder encoder = Base64.getEncoder();

        Map<Integer, WorstAndBestSolution> res = new HashMap<Integer, WorstAndBestSolution>();
        List<StudentResponse> resbest = StudentResponse.getAllBestAnswerforExamId(examId).list();
        List<StudentResponse> resworst = StudentResponse.getAllWorstAnswerforExamId(examId).list();

        resbest.forEach(st -> {
            if (!res.containsKey(st.question.numero)) {
                res.put(st.question.numero, new WorstAndBestSolution(st.question.numero));
            }
            WorstAndBestSolution tmp = res.get(st.question.numero);
            // /reponse/' + btoa('/' + s1 + '/' + (this.questionno + 1) + '/')
            byte[] encodedContent = encoder.encode(("/" + st.sheet.name + "/" + (st.question.numero) + "/").getBytes());
            var s = new String(encodedContent);
            tmp.getBestSolutions().add("/reponse/" + s);
        });

        resworst.forEach(st -> {
            if (!res.containsKey(st.question.numero)) {
                res.put(st.question.numero, new WorstAndBestSolution(st.question.numero));
            }
            WorstAndBestSolution tmp = res.get(st.question.numero);
            // /reponse/' + btoa('/' + s1 + '/' + (this.questionno + 1) + '/')
            byte[] encodedContent = encoder.encode(("/" + st.sheet.name + "/" + (st.question.numero) + "/").getBytes());
            var s = new String(encodedContent);
            tmp.getWorstSolutions().add("/reponse/" + s);
        });

        return Response.ok().entity(res.values()).build();
    }

    @Operation(summary = "Get comments and predictions for a question", description = "Returns the comments and their clustering predictions for a question of the exam.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The answers with predictions",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = AnswersWithPredictionDto.class))
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource"),
            @APIResponse(responseCode = "500", description = "Could not retrieve the predictions")
        }
    )
    @GET
    @Path("/getallcommentsandprediction4qId/{qId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getallcommentsandprediction4qId(@Parameter(description = "ID of the question", required = true) @PathParam("qId") long qId, @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, qId, Question.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        try {
            AnswersWithPredictionDto dto= questionService.getallcommentsandprediction4qId(qId);
            return Response.ok(dto).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
    }
}
