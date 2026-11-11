package fr.istic.web.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import fr.istic.domain.Answer2HybridGradedComment;
import fr.istic.domain.Exam;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.GradedComment;
import fr.istic.domain.HybridGradedComment;
import fr.istic.domain.Question;
import fr.istic.domain.Student;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.TextComment;
import fr.istic.domain.enumeration.GradeType;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.FinalNoteService;
import fr.istic.service.SecurityService;
import fr.istic.service.customdto.Answer4QuestionDTO;
import fr.istic.service.customdto.ZoneSameCommentDTO;
import fr.istic.service.dto.GradedCommentDTO;
import fr.istic.service.dto.HybridGradedCommentDTO;
import fr.istic.service.dto.TextCommentDTO;
import fr.istic.service.mapper.CommentsMapper;
import fr.istic.service.mapper.GradedCommentMapper;
import fr.istic.service.mapper.HybridGradedCommentMapper;
import fr.istic.service.mapper.TextCommentMapper;
import fr.istic.service.mapper.ZoneMapper;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

/**
 * Answer zones that share a comment, a mark or a question, to review corrections side by side.
 * <p>
 * Split out of the former {@code ExtendedAPI} class (T3): the REST paths are unchanged.
 */
@Tag(name = "Same comment zones", description = "Answer zones that share a comment, a mark or a question, to review corrections side by side.")
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class SameCommentZoneResource {

    @Inject
    SecurityService securityService;

    @Inject
    ZoneMapper zoneMapper;

    @Inject
    CommentsMapper commentsMapper;

    @Inject
    GradedCommentMapper gradedCommentMapper;

    @Inject
    TextCommentMapper textCommentMapper;

    @Inject
    HybridGradedCommentMapper hybridCommentMapper;

    @Inject
    FinalNoteService finalNoteService;

    @Operation(summary = "Get the zone and answers of a graded comment", description = "Returns the zone, step and all answers sharing the given graded comment.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The zone information and answers",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ZoneSameCommentDTO.class))
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @GET
    @Path("/getZone4GradedComment/{examId}/{gradedCommentId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getZone4GradedComment(@Parameter(description = "ID of the exam", required = true) @PathParam("examId") final long examId,
            @Parameter(description = "ID of the graded comment", required = true) @PathParam("gradedCommentId") final long gradedCommentId, @Context final UriInfo uriInfo,
            @Context final SecurityContext ctx) {

        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access this ressource").build();
        }
        List<StudentResponse> r = StudentResponse.getAllStudentResponse4examIdGradedCommentId(examId, gradedCommentId)
                .list();
        ZoneSameCommentDTO dto = new ZoneSameCommentDTO();
        List<Answer4QuestionDTO> answers = new ArrayList<>();
        // Map<Long,TextCommentDTO> textComments = new HashMap<>();
        Map<Long, GradedCommentDTO> comments = new HashMap<>();

        if (r.size() > 0 && r.get(0).question != null) {
            int numero = r.get(0).question.numero;
            dto.setNumero(numero);
            List<Question> questions = Question.findQuestionbyExamIdandnumero(examId, numero).list();
            if (questions.size() > 0) {
                dto.setZones(zoneMapper.toDto(questions.stream().map(q -> q.zone).collect(Collectors.toList())));
                dto.setGradeType(questions.get(0).gradeType);
                dto.setPoint(Integer.valueOf(questions.get(0).quarterpoint).doubleValue() / 4);
                dto.setStep(questions.get(0).step);
                dto.setValidExpression(questions.get(0).validExpression);
                dto.setAlgoName(questions.get(0).type.algoName);

            }
        }

        for (StudentResponse studentResponse : r) {
            Answer4QuestionDTO answerdto = new Answer4QuestionDTO();
            answerdto.setPagemin(studentResponse.sheet.pagemin);
            answerdto.setPagemax(studentResponse.sheet.pagemax);

            if (studentResponse.star != null) {
                answerdto.setStar(studentResponse.star);
            } else {
                answerdto.setStar(false);
            }
            if (studentResponse.worststar != null) {
                answerdto.setWorststar(studentResponse.worststar);
            } else {
                answerdto.setWorststar(false);
            }
            Set<Student> students = studentResponse.sheet.students;
            String studentName = "";
            long nbeStudent = students.size();
            long i = 0;
            for (Student student : students) {
                i = i + 1;
                studentName = studentName + student.firstname + " " + student.name;
                if (i != nbeStudent) {
                    studentName = studentName + ", ";
                }

            }
            answerdto.setStudentName(studentName);
            answerdto.setNote(Integer.valueOf(studentResponse.quarternote).doubleValue() / 4);
            answerdto.setComments(commentsMapper.toDto(new ArrayList<>(studentResponse.comments)));
            answerdto.setGradedComments(
                    studentResponse.gradedcomments.stream().map(gc -> gc.id).collect(Collectors.toList()));
            for (GradedComment gc : studentResponse.gradedcomments) {
                if (!comments.containsKey(gc.id)) {
                    comments.put(gc.id, gradedCommentMapper.toDto(gc));
                }
            }
            answers.add(answerdto);
        }

        dto.setAnswers(answers);
        dto.setGradedComments(new ArrayList(comments.values()));
        return Response.ok().entity(dto).build();

    }

    @Operation(summary = "Get the zone and answers of a hybrid comment", description = "Returns the zone, step and all answers sharing the given hybrid comment for the step value.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The zone information and answers",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ZoneSameCommentDTO.class))
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @GET
    @Path("/getZone4HybridComment/{examId}/{hybridCommentId}/{stepValue}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getZone4HybridComment(@Parameter(description = "ID of the exam", required = true) @PathParam("examId") final long examId,
            @Parameter(description = "ID of the hybrid comment", required = true) @PathParam("hybridCommentId") final long hybridCommentId, @Parameter(description = "Step value applied to the hybrid comment", required = true) @PathParam("stepValue") final int stepValue,
            @Context final UriInfo uriInfo,
            @Context final SecurityContext ctx) {

        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access this ressource").build();
        }
        List<Answer2HybridGradedComment> _r = StudentResponse
                .getAllStudentResponse4examIdHybridCommentId(examId, hybridCommentId, stepValue)
                .list();
        List<Answer2HybridGradedComment> r = new ArrayList<>();
        r.addAll(_r);

        if (stepValue == 0) {
            Optional<HybridGradedComment> s = HybridGradedComment.findByIdOptional(hybridCommentId);
            if (s.isPresent()) {
                long qId = s.get().question.id;
                List<Long> answerIds = Answer2HybridGradedComment
                        .findAllAnswerHybridGradedCommentByCommentId(hybridCommentId).list().stream()
                        .map(hc -> hc.studentResponse.id).collect(Collectors.toList());
                List<StudentResponse> resps = StudentResponse
                        .getAllStudentResponse4QidNotInResponseIdLists(qId, answerIds).list();
                for (StudentResponse resp : resps) {
                    Answer2HybridGradedComment an = new Answer2HybridGradedComment();
                    an.hybridcomments = s.get();
                    an.stepValue = 0;
                    an.studentResponse = resp;
                    r.add(an);
                }
            }
        }
        ZoneSameCommentDTO dto = new ZoneSameCommentDTO();
        List<Answer4QuestionDTO> answers = new ArrayList<>();
        // Map<Long,TextCommentDTO> textComments = new HashMap<>();
        Map<Long, HybridGradedCommentDTO> comments = new HashMap<>();

        if (r.size() > 0 && r.get(0).studentResponse.question != null) {
            int numero = r.get(0).studentResponse.question.numero;
            dto.setNumero(numero);
            List<Question> questions = Question.findQuestionbyExamIdandnumero(examId, numero).list();
            if (questions.size() > 0) {
                dto.setZones(zoneMapper.toDto(questions.stream().map(q -> q.zone).collect(Collectors.toList())));
                dto.setGradeType(questions.get(0).gradeType);
                dto.setPoint(Integer.valueOf(questions.get(0).quarterpoint).doubleValue() / 4);
                dto.setStep(questions.get(0).step);
                dto.setValidExpression(questions.get(0).validExpression);
                dto.setAlgoName(questions.get(0).type.algoName);

            }
        }
        Set<StudentResponse> processSt = new HashSet<>();
        for (Answer2HybridGradedComment an : r) {
            StudentResponse studentResponse = an.studentResponse;
            finalNoteService.computeNote4Hybrid(studentResponse);

            if (!processSt.contains(studentResponse)) {
                Answer4QuestionDTO answerdto = new Answer4QuestionDTO();
                answerdto.setPagemin(studentResponse.sheet.pagemin);
                answerdto.setPagemax(studentResponse.sheet.pagemax);

                if (studentResponse.star != null) {
                    answerdto.setStar(studentResponse.star);
                } else {
                    answerdto.setStar(false);
                }
                if (studentResponse.worststar != null) {
                    answerdto.setWorststar(studentResponse.worststar);
                } else {
                    answerdto.setWorststar(false);
                }
                Set<Student> students = studentResponse.sheet.students;
                String studentName = "";
                long nbeStudent = students.size();
                long i = 0;
                for (Student student : students) {
                    i = i + 1;
                    studentName = studentName + student.firstname + " " + student.name;
                    if (i != nbeStudent) {
                        studentName = studentName + ", ";
                    }
                }
                answerdto.setStudentName(studentName);
                answerdto.setNote(Integer.valueOf(studentResponse.quarternote).doubleValue() / 4);
                answerdto.setComments(commentsMapper.toDto(new ArrayList<>(studentResponse.comments)));
                Map<Long, Integer> hybridC = new HashMap<>();
                r.stream().filter(an1 -> an1.studentResponse.id == studentResponse.id && an1.stepValue > 0)
                        .forEach(anh -> hybridC.put(anh.hybridcomments.id, anh.stepValue));
                answerdto.setHybridgradedComments(hybridC);
                processSt.add(studentResponse);
                answers.add(answerdto);

            }
            if (!comments.containsKey(an.hybridcomments.id)) {
                comments.put(an.hybridcomments.id, hybridCommentMapper.toDto(an.hybridcomments));
            }

        }

        dto.setAnswers(answers);
        dto.setHybridComments(new ArrayList(comments.values()));
        return Response.ok().entity(dto).build();

    }

    @Operation(summary = "Get the zone and answers of a text comment", description = "Returns the zone, step and all answers sharing the given text comment.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The zone information and answers",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ZoneSameCommentDTO.class))
            ),
            @APIResponse(responseCode = "204", description = "No answer found for this comment"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @GET
    @Path("/getZone4TextComment/{examId}/{textCommentId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getZone4TextComment(@Parameter(description = "ID of the exam", required = true) @PathParam("examId") final long examId,
            @Parameter(description = "ID of the text comment", required = true) @PathParam("textCommentId") final long textCommentId, @Context final UriInfo uriInfo,
            @Context final SecurityContext ctx) {

        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access this ressource").build();
        }
        List<StudentResponse> r = StudentResponse.getAllStudentResponse4examIdTextCommentId(examId, textCommentId)
                .list();
        ZoneSameCommentDTO dto = new ZoneSameCommentDTO();
        List<Answer4QuestionDTO> answers = new ArrayList<>();
        Map<Long, TextCommentDTO> comments = new HashMap<>();
        // Map<Long,GradedCommentDTO> comments = new HashMap<>();

        if (r.size() > 0 && r.get(0).question != null) {
            int numero = r.get(0).question.numero;
            dto.setNumero(numero);
            List<Question> questions = Question.findQuestionbyExamIdandnumero(examId, numero).list();
            if (questions.size() > 0) {
                dto.setZones(zoneMapper.toDto(questions.stream().map(q -> q.zone).collect(Collectors.toList())));
                dto.setGradeType(questions.get(0).gradeType);
                dto.setPoint(Integer.valueOf(questions.get(0).quarterpoint).doubleValue() / 4);
                dto.setStep(questions.get(0).step);
                dto.setValidExpression(questions.get(0).validExpression);
                dto.setAlgoName(questions.get(0).type.algoName);

            } else {
                return Response.noContent().build();
            }
        } else {
            return Response.noContent().build();
        }

        for (StudentResponse studentResponse : r) {
            Answer4QuestionDTO answerdto = new Answer4QuestionDTO();
            answerdto.setPagemin(studentResponse.sheet.pagemin);
            answerdto.setPagemax(studentResponse.sheet.pagemax);

            if (studentResponse.star != null) {
                answerdto.setStar(studentResponse.star);
            } else {
                answerdto.setStar(false);
            }
            if (studentResponse.worststar != null) {
                answerdto.setWorststar(studentResponse.worststar);
            } else {
                answerdto.setWorststar(false);
            }
            Set<Student> students = studentResponse.sheet.students;
            String studentName = "";
            long nbeStudent = students.size();
            long i = 0;
            for (Student student : students) {
                i = i + 1;
                studentName = studentName + student.firstname + " " + student.name;
                if (i != nbeStudent) {
                    studentName = studentName + ", ";
                }

            }
            answerdto.setStudentName(studentName);
            answerdto.setNote(Integer.valueOf(studentResponse.quarternote).doubleValue() / 4);
            answerdto.setComments(commentsMapper.toDto(new ArrayList<>(studentResponse.comments)));
            answerdto.setTextComments(
                    studentResponse.textcomments.stream().map(gc -> gc.id).collect(Collectors.toList()));
            for (TextComment gc : studentResponse.textcomments) {
                if (!comments.containsKey(gc.id)) {
                    comments.put(gc.id, textCommentMapper.toDto(gc));
                }
            }
            answers.add(answerdto);
        }

        dto.setAnswers(answers);
        dto.setTextComments(new ArrayList(comments.values()));
        return Response.ok().entity(dto).build();

    }

    @Operation(summary = "Get the zone and answers with the same mark", description = "Returns the zone, step and all answers with the same grade as the given response.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The zone information and answers",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ZoneSameCommentDTO.class))
            ),
            @APIResponse(responseCode = "204", description = "No answer found for this response"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @GET
    @Path("/getZone4Mark/{examId}/{respid}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getZone4Mark(@Parameter(description = "ID of the exam", required = true) @PathParam("examId") final long examId, @Parameter(description = "ID of the student response", required = true) @PathParam("respid") final long respid,
            @Context final UriInfo uriInfo,
            @Context final SecurityContext ctx) {

        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access this ressource").build();
        }
        List<StudentResponse> r = StudentResponse.getAllStudentResponseWithSameGrade4examIdRespId(examId, respid)
                .list();
        ZoneSameCommentDTO dto = new ZoneSameCommentDTO();
        List<Answer4QuestionDTO> answers = new ArrayList<>();
        Map<Long, TextCommentDTO> textcomments = new HashMap<>();
        Map<Long, GradedCommentDTO> gradedcomments = new HashMap<>();
        Map<Long, HybridGradedCommentDTO> hybridcomments = new HashMap<>();

        if (r.size() > 0 && r.get(0).question != null) {
            int numero = r.get(0).question.numero;
            List<HybridGradedComment> hc = HybridGradedComment.findByQuestionId(r.get(0).question.id).list();
            hc.forEach(hc1 -> hybridcomments.put(hc1.id, this.hybridCommentMapper.toDto(hc1)));

            dto.setNumero(numero);
            List<Question> questions = Question.findQuestionbyExamIdandnumero(examId, numero).list();
            if (questions.size() > 0) {
                dto.setZones(zoneMapper.toDto(questions.stream().map(q -> q.zone).collect(Collectors.toList())));
                dto.setGradeType(questions.get(0).gradeType);
                dto.setPoint(Integer.valueOf(questions.get(0).quarterpoint).doubleValue() / 4);
                dto.setStep(questions.get(0).step);
                dto.setValidExpression(questions.get(0).validExpression);
                dto.setAlgoName(questions.get(0).type.algoName);

            } else {
                return Response.noContent().build();
            }
        } else {
            return Response.noContent().build();
        }

        for (StudentResponse studentResponse : r) {
            Answer4QuestionDTO answerdto = new Answer4QuestionDTO();
            answerdto.setPagemin(studentResponse.sheet.pagemin);
            answerdto.setPagemax(studentResponse.sheet.pagemax);

            if (studentResponse.star != null) {
                answerdto.setStar(studentResponse.star);
            } else {
                answerdto.setStar(false);
            }
            if (studentResponse.worststar != null) {
                answerdto.setWorststar(studentResponse.worststar);
            } else {
                answerdto.setWorststar(false);
            }
            Set<Student> students = studentResponse.sheet.students;
            String studentName = "";
            long nbeStudent = students.size();
            long i = 0;
            for (Student student : students) {
                i = i + 1;
                studentName = studentName + student.firstname + " " + student.name;
                if (i != nbeStudent) {
                    studentName = studentName + ", ";
                }

            }
            answerdto.setStudentName(studentName);
            if (studentResponse.question.gradeType == GradeType.HYBRID
                    && !"QCM".equals(studentResponse.question.type.algoName)) {
                finalNoteService.computeNote4Hybrid(studentResponse);
            }
            answerdto.setNote(Integer.valueOf(studentResponse.quarternote).doubleValue() / 4);

            answerdto.setComments(commentsMapper.toDto(new ArrayList<>(studentResponse.comments)));
            answerdto.setTextComments(
                    studentResponse.textcomments.stream().map(gc -> gc.id).collect(Collectors.toList()));
            answerdto.setGradedComments(
                    studentResponse.gradedcomments.stream().map(gc -> gc.id).collect(Collectors.toList()));

            for (TextComment gc : studentResponse.textcomments) {
                if (!textcomments.containsKey(gc.id)) {
                    textcomments.put(gc.id, textCommentMapper.toDto(gc));
                }
            }
            for (GradedComment gc : studentResponse.gradedcomments) {
                if (!gradedcomments.containsKey(gc.id)) {
                    gradedcomments.put(gc.id, gradedCommentMapper.toDto(gc));
                }
            }
            List<Answer2HybridGradedComment> r1 = Answer2HybridGradedComment
                    .findAllAnswerHybridGradedCommentByAnswerId(studentResponse.id).list();
            Map<Long, Integer> hybridC = new HashMap<>();
            r1.stream().filter(an1 -> an1.studentResponse.id == studentResponse.id && an1.stepValue > 0)
                    .forEach(anh -> hybridC.put(anh.hybridcomments.id, anh.stepValue));
            answerdto.setHybridgradedComments(hybridC);
            answers.add(answerdto);
        }

        dto.setAnswers(answers);
        dto.setTextComments(new ArrayList(textcomments.values()));
        dto.setGradedComments(new ArrayList(gradedcomments.values()));
        dto.setHybridComments(new ArrayList(hybridcomments.values()));

        return Response.ok().entity(dto).build();

    }

    @Operation(summary = "Get the zone and answers of a question", description = "Returns the zone, step and all answers of every sheet for the given question.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The zone information and answers",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ZoneSameCommentDTO.class))
            ),
            @APIResponse(responseCode = "204", description = "No question found"),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @GET
    @Path("/getZone4Numero/{examId}/{qid}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getZone4Question(@Parameter(description = "ID of the exam", required = true) @PathParam("examId") final long examId, @Parameter(description = "ID of the question", required = true) @PathParam("qid") final long qid,
            @Context final UriInfo uriInfo,
            @Context final SecurityContext ctx) {

        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access this ressource").build();
        }

        Question question = Question.findById(qid);
        ZoneSameCommentDTO dto = new ZoneSameCommentDTO();
        List<Answer4QuestionDTO> answers = new ArrayList<>();
        Map<Long, TextCommentDTO> textcomments = new HashMap<>();
        Map<Long, GradedCommentDTO> gradedcomments = new HashMap<>();
        Map<Long, HybridGradedCommentDTO> hybridcomments = new HashMap<>();

        int numero = question.numero;
        dto.setNumero(numero);
        List<Question> questions = Question.findQuestionbyExamIdandnumero(examId, numero).list();
        if (questions.size() > 0) {
            List<HybridGradedComment> hc = HybridGradedComment.findByQuestionId(questions.get(0).id).list();
            hc.forEach(hc1 -> hybridcomments.put(hc1.id, this.hybridCommentMapper.toDto(hc1)));

            dto.setZones(zoneMapper.toDto(questions.stream().map(q -> q.zone).collect(Collectors.toList())));
            dto.setGradeType(questions.get(0).gradeType);
            dto.setPoint(Integer.valueOf(questions.get(0).quarterpoint).doubleValue() / 4);
            dto.setStep(questions.get(0).step);
            dto.setValidExpression(questions.get(0).validExpression);
            dto.setAlgoName(questions.get(0).type.algoName);

        } else {
            return Response.noContent().build();
        }
        List<ExamSheet> sheets = ExamSheet.getAll4ExamId(examId).list();

        for (ExamSheet sheet : sheets) {
            Answer4QuestionDTO answerdto = new Answer4QuestionDTO();
            answerdto.setPagemin(sheet.pagemin);
            answerdto.setPagemax(sheet.pagemax);
            Set<Student> students = sheet.students;
            String studentName = "";
            long nbeStudent = students.size();
            long i = 0;
            for (Student student : students) {
                i = i + 1;
                studentName = studentName + student.firstname + " " + student.name;
                if (i != nbeStudent) {
                    studentName = studentName + ", ";
                }

            }
            answerdto.setStudentName(studentName);

            List<StudentResponse> r = StudentResponse
                    .getAllStudentResponseWithExamIdNumeroAndSheetId(examId, numero, sheet.id)
                    .list();
            if (r.size() > 0) {
                StudentResponse studentResponse = r.get(0);
                if (studentResponse.star != null) {
                    answerdto.setStar(studentResponse.star);
                } else {
                    answerdto.setStar(false);
                }
                if (studentResponse.worststar != null) {
                    answerdto.setWorststar(studentResponse.worststar);
                } else {
                    answerdto.setWorststar(false);
                }
                answerdto.setNote(Integer.valueOf(studentResponse.quarternote).doubleValue() / 4);
                answerdto.setComments(commentsMapper.toDto(new ArrayList<>(studentResponse.comments)));
                answerdto.setTextComments(
                        studentResponse.textcomments.stream().map(gc -> gc.id).collect(Collectors.toList()));
                answerdto.setGradedComments(
                        studentResponse.gradedcomments.stream().map(gc -> gc.id).collect(Collectors.toList()));
                for (TextComment gc : studentResponse.textcomments) {
                    if (!textcomments.containsKey(gc.id)) {
                        textcomments.put(gc.id, textCommentMapper.toDto(gc));
                    }
                }
                for (GradedComment gc : studentResponse.gradedcomments) {
                    if (!gradedcomments.containsKey(gc.id)) {
                        gradedcomments.put(gc.id, gradedCommentMapper.toDto(gc));
                    }
                }
                List<Answer2HybridGradedComment> r1 = Answer2HybridGradedComment
                        .findAllAnswerHybridGradedCommentByAnswerId(studentResponse.id).list();
                Map<Long, Integer> hybridC = new HashMap<>();
                r1.stream().filter(an1 -> an1.studentResponse.id == studentResponse.id && an1.stepValue > 0)
                        .forEach(anh -> hybridC.put(anh.hybridcomments.id, anh.stepValue));
                answerdto.setHybridgradedComments(hybridC);
            }
            answers.add(answerdto);
        }

        dto.setAnswers(answers);
        dto.setTextComments(new ArrayList(textcomments.values()));
        dto.setGradedComments(new ArrayList(gradedcomments.values()));
        dto.setHybridComments(new ArrayList(hybridcomments.values()));

        return Response.ok().entity(dto).build();

    }
}
