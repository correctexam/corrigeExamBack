package fr.istic.web.rest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
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
import fr.istic.domain.Question;
import fr.istic.domain.StudentResponse;
import fr.istic.security.AuthoritiesConstants;
import fr.istic.service.SecurityService;
import fr.istic.service.customdto.correctexamstate.MarkingExamStateDTO;
import fr.istic.service.customdto.correctexamstate.QuestionStateDTO;
import fr.istic.service.customdto.correctexamstate.SheetStateDTO;

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
 * Correction progress of an exam, per sheet and per question.
 * <p>
 * Split out of the former {@code ExtendedAPI} class (T3): the REST paths are unchanged.
 */
@Tag(name = "Marking status", description = "Correction progress of an exam, per sheet and per question.")
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class MarkingStatusResource {

    private final Logger log = LoggerFactory.getLogger(MarkingStatusResource.class);

    @Inject
    SecurityService securityService;

    @Operation(summary = "Check if the exam is fully corrected", description = "Returns whether every question of every sheet of the exam has been corrected.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "True if the exam is fully corrected",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.BOOLEAN))
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @GET
    @Path("/getExamStatusFinish/{examId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getExamStatusFinish(@Parameter(description = "ID of the exam", required = true) @PathParam("examId") final long examId, @Context final UriInfo uriInfo,
            @Context final SecurityContext ctx) {

        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access this ressource").build();
        }

        final List<StudentResponse> stdResponses = StudentResponse.getAll4ExamId(examId).list();
        final Map<Long, List<StudentResponse>> byQuestion = stdResponses.stream()
                .collect(Collectors.groupingBy(StudentResponse::getQuestionNumero));

        var nbreSheet = ExamSheet.getAll4ExamId(examId).count();

        var res = byQuestion.keySet().stream().allMatch(e-> byQuestion.get(e).size()==nbreSheet);

        if (nbreSheet ==0 || stdResponses.size()==0){
            res = false;
        }

        return Response.ok(res).build();
    }

    /**
     * Provides summary data of a given exam.
     *
     * @param examId The ID of the exam
     */
    @Operation(summary = "Get the marking status of an exam", description = "Returns a summary of the marking progress per question and per sheet of the exam.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "200",
                description = "The marking state of the exam",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MarkingExamStateDTO.class))
            ),
            @APIResponse(responseCode = "403", description = "Current user cannot access this resource")
        }
    )
    @GET
    @Path("/getExamStatus/{examId}")
    @RolesAllowed({ AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN })
    public Response getExamStatus(@Parameter(description = "ID of the exam", required = true) @PathParam("examId") final long examId, @Context final UriInfo uriInfo,
            @Context final SecurityContext ctx) {

        if (!securityService.canAccess(ctx, examId, Exam.class)) {
            return Response.status(403, "Current user cannot access this ressource").build();
        }

        final MarkingExamStateDTO result = new MarkingExamStateDTO();
        final Exam exam = Exam.findById(examId);
        final List<StudentResponse> stdResponses = StudentResponse.getAll4ExamId(examId).list();
        final List<Question> questionsExam = Question.findQuestionbyExamId(examId).list();
        final Map<Long, List<StudentResponse>> byQuestion = stdResponses.stream()
                .collect(Collectors.groupingBy(StudentResponse::getQuestionNumero));

        result.setNameExam(exam.name);

        // Populate initial questions

        // ExamSheetID
        Map<Long, QuestionStateDTO> q = new LinkedHashMap<>();
        questionsExam.sort(new Comparator<Question>() {

            @Override
            public int compare(Question arg0, Question arg1) {
                return arg0.numero - arg1.numero;
            }

        });

        for (Question quest : questionsExam) {
            // if (!q.containsKey(quest.numero)){
            final var res = new QuestionStateDTO();
            res.setAnsweredSheets(0);
            res.setFirstUnmarkedSheet(0);
            res.setId(quest.id);
            res.setNumero(quest.numero);
            res.setLibelle(quest.libelle);
            if (!q.containsKey(quest.numero.longValue())) {
                q.put(quest.numero.longValue(), res);
            }
            // result.getQuestions().add(res);
            // }
        }

        // Populate initial sheets

        // ExamSheetID
        Map<Long, SheetStateDTO> s = new LinkedHashMap<>();
        List<ExamSheet> sheets = exam.scanfile.sheets.stream().collect(Collectors.toList());
        sheets.sort(new Comparator<ExamSheet>() {

            @Override
            public int compare(ExamSheet arg0, ExamSheet arg1) {
                return arg0.pagemin - arg1.pagemin;
            }

        });

        for (ExamSheet sh : sheets) {
            if (/* sh.students != null && sh.students.size() > 0 && */ sh.pagemin != -1 && sh.pagemax != -1) {
                final var res = new SheetStateDTO();
                res.setAnsweredSheets(0);
                res.setFirstUnmarkedQuestion(1);
                res.setId(sh.id);
                s.put(sh.id, res);
                result.getSheets().add(res);
            } else if (sh.pagemin == -1 || sh.pagemax == -1) {
                // log.error("sheet not linked with a real pdf");
            } else {
                // log.error("sheet without student");

            }
        }

        // The ID of all the sheet. Used to find the first sheet that has a given
        // question not answered yet

        // Filling the questions part of the DTO
        for (Question quest : questionsExam) {
            // The responses for this question
            final List<StudentResponse> _responsesForQ = byQuestion.computeIfAbsent(quest.numero.longValue(),
                    i -> new ArrayList<>());
            final List<StudentResponse> responsesForQ = _responsesForQ.stream()
                    .filter(r -> r.sheet != null && r.sheet.pagemin != -1 && r.sheet.pagemax != -1)
                    .collect(Collectors.toList());
                    /*  log.error(quest.numero + " " + responsesForQ.size());
                    if (quest.numero == 9){
                        responsesForQ.forEach(e-> log.error(""+ e.id +" "+e.sheet.id + " " + e.question.id));
                    }*/
            // Getting the ID of the sheets that have an answer for this question
            responsesForQ.sort(new ComparatorImplementation());
            QuestionStateDTO qs = q.get(quest.numero.longValue());
            if (quest.randomHorizontalCorrection != null){
                qs.setRandomHorizontalCorrection(quest.randomHorizontalCorrection);
            } else {
                qs.setRandomHorizontalCorrection(false);
            }
            List<Integer> uncorrected = new ArrayList<>();
            List<Integer> pageminsCorrected = responsesForQ.stream().map(e-> e.sheet.pagemin).collect(Collectors.toList());
            for (int i=0;i<sheets.size();i++){
                if (!pageminsCorrected.contains(sheets.get(i).pagemin)){
                    uncorrected.add(i);
                }
            }
            qs.setUnmarkedSheetIndex(uncorrected);
            if (responsesForQ.size() > 0 && responsesForQ.get(0).sheet.pagemin == 0) {
                if (responsesForQ.size() == 1) {
                    qs.setFirstUnmarkedSheet(Long.valueOf(responsesForQ.get(0).sheet.pagemax + 1));
                }
                for (int i = 0; i < responsesForQ.size() - 1; i++) {
                    StudentResponse sl1 = responsesForQ.get(i);
                    StudentResponse sl2 = responsesForQ.get(i + 1);
                    qs.setFirstUnmarkedSheet(Long.valueOf(sl1.sheet.pagemax + 1));
                    if (sl1.sheet.pagemax + 1 < sl2.sheet.pagemin) {
                        break;
                    } else if (i == responsesForQ.size() - 2) {
                        qs.setFirstUnmarkedSheet(Long.valueOf(sl2.sheet.pagemax + 1));
                    }
                }
            }
            qs.setAnsweredSheets(responsesForQ.size());
        }

        /*
         * List<QuestionStateDTO> toRemove = q.values().stream().filter(q2 -> {
         * return q.values().stream()
         * .anyMatch(q1 -> q1 != q2 && q1.getNumero() == q2.getNumero()
         * && (q2.getAnsweredSheets() < q1.getAnsweredSheets()
         * || (q2.getAnsweredSheets() <= q1.getAnsweredSheets() && q2.getId() >
         * q1.getId())));
         * }).collect(Collectors.toList());
         *
         * for (QuestionStateDTO tor : toRemove) {
         * q.remove(tor.getId());
         * }
         */
        result.getQuestions().addAll(q.values());
        /*   */

        // Filling the sheet part of the DTO

        /*
         * final Map<Set<Long>, List<StudentResponse>> byStudent = stdResponses
         * .stream()
         * .collect(Collectors.groupingBy(resp -> Set.copyOf(resp.getStudentId())));
         */

        stdResponses.sort(new ComparatorImplementation());

        Map<Long, List<StudentResponse>> byStudent = stdResponses.stream()
                .collect(Collectors.groupingBy(StudentResponse::getSheetId));
        /*
         * var students = new HashMap<Long, List<StudentResponse>>();
         * byStudent.entrySet().stream().forEach(e-> {
         * for (long id : e.getKey()){
         * List<StudentResponse> sts = students.getOrDefault(id, new
         * ArrayList<StudentResponse>());
         * sts.addAll(e.getValue());
         * if (!students.containsKey(id)){
         * students.put(id,sts);
         * }
         * }
         * });
         */
        /*
         * byStudent.values().stream().forEach(std -> {
         * std.sort(new ComparatorImplementation2());
         *
         * });
         * byStudent.entrySet().stream().forEach(ent -> {
         * log.error("pass par la");
         * ent.getKey().stream().forEach(e-> log.error("" +e));
         * });
         */

        // The ID of all the questions. Used to find the first question that has a given
        // sheet not answered yet

        for (Entry<Long, List<StudentResponse>> ent : byStudent.entrySet()) {
            final var res = s.get(ent.getKey());
            if (res != null) {
                List<StudentResponse> l = ent.getValue();
                l.sort(new ComparatorImplementation2());
                res.setAnsweredSheets(Long.valueOf(l.size()));

                if (l.size() == 0) {
                    res.setFirstUnmarkedQuestion(Long.valueOf(1));

                } else if (l.size() == 1 && l.get(0).question.numero == 1) {
                    res.setFirstUnmarkedQuestion(Long.valueOf(2));

                } else if (l.size() > 0 && l.get(0).question.numero != 1) {
                    res.setFirstUnmarkedQuestion(Long.valueOf(1));
                } else {
                    for (int i = 0; i < l.size() - 1; i++) {
                        StudentResponse sl1 = l.get(i);
                        StudentResponse sl2 = l.get(i + 1);
                        if (sl1.question.numero + 1 < sl2.question.numero) {
                            res.setFirstUnmarkedQuestion(Long.valueOf(sl1.question.numero + 1));
                            break;
                        } else if (i == l.size() - 2) {
                            res.setFirstUnmarkedQuestion(Long.valueOf(sl1.question.numero + 2));
                        }
                    }
                }
            }

        }

        return Response.ok().entity(result).build();
    }
    private final class ComparatorImplementation implements Comparator<StudentResponse> {

        @Override
        public int compare(StudentResponse arg0, StudentResponse arg1) {
            return arg0.sheet.pagemin - arg1.sheet.pagemin;
        }
    }

    private final class ComparatorImplementation2 implements Comparator<StudentResponse> {

        @Override
        public int compare(StudentResponse arg0, StudentResponse arg1) {
            return arg0.question.numero - arg1.question.numero;
        }
    }
}
