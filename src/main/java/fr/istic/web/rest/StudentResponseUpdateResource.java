package fr.istic.web.rest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.domain.Answer2HybridGradedComment;
import fr.istic.domain.Exam;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.GradedComment;
import fr.istic.domain.Question;
import fr.istic.domain.Student;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.TextComment;
import fr.istic.domain.User;
import fr.istic.domain.enumeration.GradeType;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.Answer2HybridGradedCommentService;
import fr.istic.service.SecurityService;
import fr.istic.service.StudentResponseService;
import fr.istic.service.customdto.ClusterDTO;
import fr.istic.service.customdto.ZoneSameCommentDTO;
import fr.istic.service.dto.Answer2HybridGradedCommentDTO;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

/**
 * Hybrid comment step values and bulk update of the responses of a cluster.
 * <p>
 * Split out of the former {@code ExtendedAPI} class (T3): the REST paths are unchanged.
 */
@Tag(name = "Student response update", description = "Hybrid comment step values and bulk update of the responses of a cluster.")
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class StudentResponseUpdateResource {

    private final Logger log = LoggerFactory.getLogger(StudentResponseUpdateResource.class);

    @Inject
    StudentResponseService studentResponseService;

    @Inject
    SecurityService securityService;

    @Inject
    Answer2HybridGradedCommentService answer2HybridGradedCommentService;

    @Operation(summary = "Set the step value of a hybrid comment", description = "Sets the step value of a hybrid comment on a student response.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The updated hybrid comment",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Answer2HybridGradedCommentDTO.class))
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @PUT
    @Transactional()
    @Path("/update-2-hybrid-graded-comments-with-stepvalue/{responseId}/{hybridCommentId}/{stepValue}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response updateAnswerHybridCommentWithStepValue(@Parameter(description = "ID of the student response", required = true) @PathParam("responseId") final long responseId,
            @Parameter(description = "ID of the hybrid comment", required = true) @PathParam("hybridCommentId") final long hybridCommentId,
            @Parameter(description = "Step value applied to the hybrid comment", required = true) @PathParam("stepValue") final int stepValue,
            @Context final UriInfo uriInfo,
            @Context final SecurityContext ctx) {
        if (!securityService.canAccess(ctx, responseId, StudentResponse.class)) {
            return Response.status(403, "Current user cannot access this ressource").build();
        }
        this.studentResponseService.updateCorrectedBy(responseId,this.securityService.getCurrentLoggedUser(ctx));
        Answer2HybridGradedCommentDTO result = this.answer2HybridGradedCommentService
                .setStepValueWithResponseIdAndHybridCommentId(responseId, hybridCommentId, stepValue);
        return Response.ok().entity(result).build();

    }

    @Operation(summary = "Increment the step value of a hybrid comment", description = "Increments the step value of a hybrid comment on a student response.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The updated hybrid comment",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Answer2HybridGradedCommentDTO.class))
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @PUT
    @Transactional()
    @Path("/update-2-hybrid-graded-comments/{responseId}/{hybridCommentId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response updateAnswerHybridCommentWith(@Parameter(description = "ID of the student response", required = true) @PathParam("responseId") final long responseId,
            @Parameter(description = "ID of the hybrid comment", required = true) @PathParam("hybridCommentId") final long hybridCommentId,
            @Context final UriInfo uriInfo,
            @Context final SecurityContext ctx) {
        if (!securityService.canAccess(ctx, responseId, StudentResponse.class)) {
            return Response.status(403, "Current user cannot access this ressource").build();
        }
        this.studentResponseService.updateCorrectedBy(responseId,this.securityService.getCurrentLoggedUser(ctx));
        Answer2HybridGradedCommentDTO result = this.answer2HybridGradedCommentService
                .incrementWithResponseIdAndHybridCommentId(responseId, hybridCommentId);
        return Response.ok().entity(result).build();

    }

    @Operation(summary = "Update responses from a cluster", description = "Applies the correction of the template sheet to all copies of the cluster for a question.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "Cluster applied"),
            @APIResponse(responseCode = "204", description = "No response could be updated"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @POST
    @Transactional()
    @Path("/updateStudentResponse4Cluster/{examId}/{qid}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response updateStudentResponse4Cluster(ClusterDTO clusterDto, @Parameter(description = "ID of the exam", required = true) @PathParam("examId") final long examId,
            @Parameter(description = "ID of the question", required = true) @PathParam("qid") final long qid,
            @Context final UriInfo uriInfo,
            @Context final SecurityContext ctx) {

        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access this ressource").build();
        }
        User correctedBy =securityService.getCurrentLoggedUser(ctx);

        Question question = Question.findById(qid);
        // ZoneSameCommentDTO dto = new ZoneSameCommentDTO();
        List<SheetQuestion> answers = new ArrayList<>();
        Map<Long, TextComment> textcomments = new HashMap<>();
        Map<Long, GradedComment> gradedcomments = new HashMap<>();

        int numero = question.numero;
        if (question == null || "QCM".equals(question.type.algoName)) {
            return Response.noContent().build();
        }
        List<ExamSheet> sheets = ExamSheet.getAll4ExamId(examId).list();

        for (ExamSheet sheet : sheets) {
            SheetQuestion answerdto = new SheetQuestion();
            answerdto.sheet = sheet;
            answerdto.question = question;

            List<StudentResponse> r = StudentResponse
                    .getAllStudentResponseWithExamIdNumeroAndSheetId(examId, numero, sheet.id)
                    .list();
            if (r.size() > 0) {
                StudentResponse studentResponse = r.get(0);
                answerdto.studentResponse = studentResponse;
                answerdto.studentResponseId = studentResponse.id;
                if (studentResponse.star != null) {
                    answerdto.star = (studentResponse.star);
                } else {
                    answerdto.star = false;
                }
                if (studentResponse.worststar != null) {
                    answerdto.worststar = studentResponse.worststar;
                } else {
                    answerdto.worststar = false;
                }
                answerdto.quarternote = studentResponse.quarternote;
                answerdto.textComments = studentResponse.textcomments.stream().map(gc -> gc.id)
                        .collect(Collectors.toList());
                answerdto.gradedComments = studentResponse.gradedcomments.stream().map(gc -> gc.id)
                        .collect(Collectors.toList());
                for (TextComment gc : studentResponse.textcomments) {
                    if (!textcomments.containsKey(gc.id)) {
                        textcomments.put(gc.id, gc);
                    }
                }
                for (GradedComment gc : studentResponse.gradedcomments) {
                    if (!gradedcomments.containsKey(gc.id)) {
                        gradedcomments.put(gc.id, gc);
                    }
                }
            } else {
                answerdto.studentResponseId = -1;
                answerdto.star = false;
                answerdto.worststar = false;

            }

            answers.add(answerdto);
        }

        SheetQuestion answerdtotempalate = answers.get(clusterDto.getTemplat());
        if (answerdtotempalate.studentResponseId == -1) {
            return Response.noContent().build();
        }
        List<Answer2HybridGradedComment> answerToCommentTemplate = new ArrayList<>();
        if (question.gradeType == GradeType.HYBRID) {
            answerToCommentTemplate = Answer2HybridGradedComment.findAllAnswerHybridGradedCommentByAnswerId(
                    answerdtotempalate.studentResponseId).list();
        }

        for (int toUpdate : clusterDto.getCopies()) {
            SheetQuestion answerdtoUpdate = answers.get(toUpdate);
            if (answerdtoUpdate.studentResponseId == -1) {
                StudentResponse stToUpdate = new StudentResponse();
                stToUpdate.quarternote = answerdtotempalate.quarternote;
                stToUpdate.star = answerdtotempalate.star;
                stToUpdate.worststar = answerdtotempalate.worststar;
                stToUpdate.question = answerdtoUpdate.question;
                stToUpdate.sheet = answerdtoUpdate.sheet;

                if (question.gradeType == GradeType.DIRECT) {
                    stToUpdate.textcomments.addAll(textcomments.values().stream()
                            .filter(gs2 -> answerdtotempalate.textComments.contains(gs2.id))
                            .collect(Collectors.toList()));
                            stToUpdate.lastModifiedDate = Instant.now();
                            stToUpdate.correctedBy = correctedBy;

                    StudentResponse.persist(stToUpdate);

                } else if (question.gradeType == GradeType.HYBRID) {
                    stToUpdate.lastModifiedDate = Instant.now();
                    stToUpdate.correctedBy = correctedBy;

                    StudentResponse.persist(stToUpdate);
                    Answer2HybridGradedComment.findAllAnswerHybridGradedCommentByAnswerId(
                            answerdtotempalate.studentResponseId).list().forEach(an1 -> {
                                Answer2HybridGradedCommentDTO an = new Answer2HybridGradedCommentDTO();
                                an.hybridcommentsId = an1.hybridcomments.id;
                                an.studentResponseId = stToUpdate.id;
                                an.stepValue = an1.stepValue;
                                this.answer2HybridGradedCommentService.persistOrUpdate(an);

                            });
                }

                else {
                    stToUpdate.gradedcomments.addAll(gradedcomments.values().stream()
                            .filter(gs2 -> answerdtotempalate.gradedComments.contains(gs2.id))
                            .collect(Collectors.toList()));

                            stToUpdate.lastModifiedDate = Instant.now();
                            stToUpdate.correctedBy =correctedBy;
                    StudentResponse.persist(stToUpdate);

                }

            } else {
                StudentResponse stToUpdate = StudentResponse.cleanCommentAndGrade(answerdtoUpdate.studentResponse);
                stToUpdate.quarternote = answerdtotempalate.quarternote;
                stToUpdate.star = answerdtotempalate.star;
                stToUpdate.worststar = answerdtotempalate.worststar;
                if (stToUpdate.question.id != answerdtoUpdate.question.id) {
                    log.error("strange to update a StudentResponse that do not target the same question");
                }
                if (stToUpdate.sheet.id != answerdtoUpdate.sheet.id) {
                    log.error("strange to update a StudentResponse that do not target the same sheet");
                }

                if (question.gradeType == GradeType.DIRECT) {
                    stToUpdate.textcomments.addAll(textcomments.values().stream()
                            .filter(gs2 -> answerdtotempalate.textComments.contains(gs2.id))
                            .collect(Collectors.toList()));
                } else if (question.gradeType == GradeType.HYBRID) {
                    List<Answer2HybridGradedComment> existingComments = Answer2HybridGradedComment
                            .findAllAnswerHybridGradedCommentByAnswerId(stToUpdate.id).list();
                    existingComments.forEach(cc -> {
                        cc.stepValue = 0;
                        cc.persistOrUpdate();
                    });

                    answerToCommentTemplate.forEach(an1 -> {
                        if (existingComments.stream().map(cc -> cc.hybridcomments.id)
                                .anyMatch(ccid -> ccid == an1.hybridcomments.id)) {
                            var commentsToUpdate = existingComments.stream()
                                    .filter(cc -> cc.hybridcomments.id == an1.hybridcomments.id).findFirst();
                            if (commentsToUpdate.isPresent()) {
                                commentsToUpdate.get().stepValue = an1.stepValue;
                                // log.error("should " + commentsToUpdate.get().hybridcomments.text + " "+
                                // commentsToUpdate.get().studentResponse.sheet.pagemin + " "+
                                // commentsToUpdate.get().stepValue);

                                commentsToUpdate.get().persistOrUpdate();
                            } else {
                                log.error("should never go there");
                            }
                        } else {
                            Answer2HybridGradedCommentDTO an = new Answer2HybridGradedCommentDTO();
                            an.hybridcommentsId = an1.hybridcomments.id;
                            an.studentResponseId = stToUpdate.id;
                            an.stepValue = an1.stepValue;
                            this.answer2HybridGradedCommentService.persistOrUpdate(an);
                        }
                    });

                } else {
                    stToUpdate.gradedcomments.addAll(gradedcomments.values().stream()
                            .filter(gs2 -> answerdtotempalate.gradedComments.contains(gs2.id))
                            .collect(Collectors.toList()));
                }
                stToUpdate.persistOrUpdate();

            }

        }

        return Response.ok().build();

    }

    class SheetQuestion {
        ExamSheet sheet;
        Question question;
        long studentResponseId;
        StudentResponse studentResponse;
        int quarternote;
        boolean star = false;
        boolean worststar = false;
        List<Long> textComments = new ArrayList<>();
        List<Long> gradedComments = new ArrayList<>();

    }
}
