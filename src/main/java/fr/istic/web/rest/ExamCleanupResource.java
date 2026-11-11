package fr.istic.web.rest;

import static jakarta.ws.rs.core.UriBuilder.fromPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.domain.Exam;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.GradedComment;
import fr.istic.domain.Question;
import fr.istic.domain.Student;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.TextComment;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.Answer2HybridGradedCommentService;
import fr.istic.service.ExamService;
import fr.istic.service.ExamSheetService;
import fr.istic.service.QuestionService;
import fr.istic.service.SecurityService;
import fr.istic.service.StudentService;
import fr.istic.service.ZoneService;
import fr.istic.service.dto.ExamDTO;
import fr.istic.service.dto.QuestionDTO;
import fr.istic.service.mapper.ExamMapper;
import fr.istic.web.util.HeaderUtil;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

/**
 * Removal of corrections, comments, orphan sheets and question zones of an exam.
 * <p>
 * Split out of the former {@code ExtendedAPI} class (T3): the REST paths are unchanged.
 */
@Tag(name = "Exam cleanup", description = "Removal of corrections, comments, orphan sheets and question zones of an exam.")
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ExamCleanupResource {

    private final Logger log = LoggerFactory.getLogger(ExamCleanupResource.class);

    @ConfigProperty(name = "application.name")
    String applicationName;

    @Inject
    StudentService studentService;

    @Inject
    SecurityService securityService;

    @Inject
    QuestionService questionService;

    @Inject
    ZoneService zoneService;

    @Inject
    ExamMapper examMapper;

    @Inject
    ExamService examService;

    @Inject
    ExamSheetService examSheetService;

    @Inject
    Answer2HybridGradedCommentService answer2HybridGradedCommentService;

    @Operation(summary = "Clean a question correction", description = "Removes the corrections and comments of a question and returns the cleaned question.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "201",
                description = "The cleaned question",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = QuestionDTO.class))
            ),
            @APIResponse(responseCode = "400", description = "Invalid question data")
        }
    )
    @POST()
    @Path("/cleanResponse")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response cleanAllCorrectionAndComment(@Valid QuestionDTO questionDTO, @Context UriInfo uriInfo) {
        log.debug("REST request to clean Question : {}", questionDTO);
        var result = questionService.cleanAllCorrectionAndComment(questionDTO);
        var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build()).entity(result);
        HeaderUtil.createEntityCreationAlert(applicationName, true, "question", result.id.toString())
                .forEach(response::header);
        return response.build();
    }

    @Transactional
    void deleteOphanExamSheet(long examId) {
        List<ExamSheet> list = ExamSheet.getAllOrphan4ExamId(examId).list();
        if (list.size() > 0) {
            for (ExamSheet examSheet : list) {
                if (StudentResponse.findStudentResponsesbysheetId(examSheet.id).count() == 0) {
                    if (ExamSheet.getAllDouble4Same(examSheet.id).count() > 0) {
                        examSheetService.delete(examSheet.id);
                    }
                    if (examSheet.pagemin == -1 && examSheet.pagemax == -1) {
                        if (examSheet.students.size() > 0) {
                            List<Student> students = new ArrayList<Student>(examSheet.students);
                            for (Student student : students) {
                                student.examSheets.remove(examSheet);
                                examSheet.students.remove(student);
                                studentService.persistOrUpdate(student);
                            }
                        }
                        examSheetService.delete(examSheet.id);
                    }
                }
                ;
            }

        }
    }

    @Operation(summary = "Clean orphan exam sheets", description = "Deletes the orphan exam sheets of the exam.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "204", description = "Orphan sheets deleted"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @DELETE()
    @Path("/cleanExamSheet/{examId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response cleanAllFalseSheet(@Parameter(description = "ID of the exam", required = true) @PathParam("examId") long examId, @Context UriInfo uriInfo,
            @Context SecurityContext ctx) {
        log.debug("REST request to clean all Questions and zone : {}");
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        this.deleteOphanExamSheet(examId);

        ExamSheet.getEntityManager().clear();
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, "exam", "-1")
                .forEach(response::header);
        return response.build();
    }

    @Operation(summary = "Clean an exam", description = "Deletes all questions, comments and zones of the exam and returns the cleaned exam.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "201",
                description = "The cleaned exam",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExamDTO.class))
            ),
            @APIResponse(responseCode = "204", description = "The exam does not exist"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @DELETE()
    @Path("/cleanExam/{examId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response cleanAllQuestionZone4Exam(@Parameter(description = "ID of the exam", required = true) @PathParam("examId") long examId, @Context UriInfo uriInfo,
            @Context SecurityContext ctx) {
        log.debug("REST request to clean all Questions and zone : {}");
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }

        Optional<Exam> optEx = Exam.findByIdOptional(examId);

        if (optEx.isPresent()) {
            examService.deleteQuestionCommentAndZone(examId);
            /*
             * List<Zone> zones = Zone.getQuestionZone4ExamId(examId).list();
             * zones.forEach(z -> {
             * this.zoneService.delete(z.id);
             * });
             *
             * Exam ex = optEx.get();
             * if (ex.idzone != null){
             * this.zoneService.delete(ex.idzone.id);
             * }
             * if (ex.namezone != null){
             *
             * this.zoneService.delete(ex.namezone.id);
             * }
             * if (ex.firstnamezone != null){
             * this.zoneService.delete(ex.firstnamezone.id);
             * }
             * if (ex.notezone != null){
             * this.zoneService.delete(ex.notezone.id);
             * }
             */
            ExamDTO result = this.examMapper.toDto(optEx.get());

            var response = Response.created(fromPath(uriInfo.getPath()).path(result.id.toString()).build())
                    .entity(result);
            HeaderUtil.createEntityCreationAlert(applicationName, true, "exam", result.id.toString())
                    .forEach(response::header);
            return response.build();

        } else {
            var response = Response.noContent();
            HeaderUtil.createEntityDeletionAlert(applicationName, true, "exam", "-1")
                    .forEach(response::header);
            return response.build();
        }

    }

    @Operation(summary = "Delete an answer and its comments", description = "Deletes a student response and unsets all the comments associated with it.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "204", description = "Answer deleted"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @DELETE
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Path("/deleteAnswerAndUnsetComment/{studentResponseId}")
    @Transactional
    public Response deleteAnswerAndUnsetComment(@Parameter(description = "ID of the student response", required = true) @PathParam("studentResponseId") long studentResponseId,
            @Context UriInfo uriInfo,
            @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, studentResponseId, StudentResponse.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        Optional<StudentResponse> sr = StudentResponse.findByIdOptional(studentResponseId);
        if (sr.isPresent()) {
            sr.get().gradedcomments.forEach(gc -> {

                gc.studentResponses.remove(sr.get());
                gc.persistOrUpdate();
            });
            sr.get().textcomments.forEach(tc -> {

                tc.studentResponses.remove(sr.get());
                tc.persistOrUpdate();
            });
            sr.get().clearComments();
            this.answer2HybridGradedCommentService.deleteAllAnswerHybridGradedCommentByAnswerId(sr.get().id);
            sr.get().delete();
        }
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, "studentResponse", "-1")
                .forEach(response::header);
        return response.build();
    }

    @Operation(summary = "Delete all answers and comments of an exam", description = "Deletes every student response, answer and comment of all questions of the exam.")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "204", description = "Answers and comments deleted"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @DELETE
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    @Path("/deleteAllAnswerAndComment/{examId}")
    @Transactional
    public Response deleteAllAnswerAndComment(@Parameter(description = "ID of the exam", required = true) @PathParam("examId") long examId, @Context UriInfo uriInfo,
            @Context SecurityContext ctx) {
        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access to this ressource").build();
        }
        Optional<Exam> ex = Exam.findByIdOptional(examId);
        if (ex.isPresent()) {
            List<Question> qs = Question.findQuestionbyExamId(examId).list();

            for (Question question : qs) {

                List<GradedComment> gradeComment = new ArrayList<GradedComment>();
                List<TextComment> textComments = new ArrayList<TextComment>();
                questionService.updateCorrectionAndAnswer(question, gradeComment, textComments);
                List<Long> gradeCommentids = gradeComment.stream().map(gc -> gc.id).collect(Collectors.toList());
                List<Long> textCommentsids = textComments.stream().map(gc -> gc.id).collect(Collectors.toList());
                questionService.deleteComments(gradeCommentids, textCommentsids);
                List<StudentResponse> srs = StudentResponse.findAllByQuestionId(question.id).list();
                for (StudentResponse studentResponse : srs) {
                    studentResponse.delete();
                }
            }

        }
        var response = Response.noContent();
        HeaderUtil.createEntityDeletionAlert(applicationName, true, "studentResponse", "-1")
                .forEach(response::header);
        return response.build();
    }
}
