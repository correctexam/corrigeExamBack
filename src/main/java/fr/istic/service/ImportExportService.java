package fr.istic.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import fr.istic.domain.Comments;
import fr.istic.domain.Course;
import fr.istic.domain.CourseGroup;
import fr.istic.domain.Exam;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.FinalResult;
import fr.istic.domain.GradedComment;
import fr.istic.domain.Question;
import fr.istic.domain.QuestionType;
import fr.istic.domain.Scan;
import fr.istic.domain.Student;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.Template;
import fr.istic.domain.TextComment;
import fr.istic.domain.Zone;
import fr.istic.domain.enumeration.GradeType;
import fr.istic.service.dto.CourseDTO;
import fr.istic.service.mapper.CourseMapper;

class Relation {
    public String left, right;

    public Relation(String left, String right) {
        this.left = left;
        this.right = right;
    }
}

@ApplicationScoped
@Transactional
public class ImportExportService {

    private final Logger log = LoggerFactory.getLogger(ImportExportService.class);

    @ConfigProperty(name = "correctexam.uses3", defaultValue = "false")
    boolean uses3;

    @Inject
    FichierS3Service fichierS3Service;

    @Inject
    CourseMapper courseMapper;


    public JsonObject export(long courseId) {
        Map<UUID, Long> uuidMap = new HashMap<UUID, Long>();
        JsonObject root = new JsonObject();

        // Course
        Course course = Course.findById(courseId);
        JsonObject courseJ = new JsonObject();
        UUID courseU = UUID.randomUUID();
        uuidMap.put(courseU, course.id);
        courseJ.addProperty("uuid", courseU.toString());
        courseJ.addProperty("name", course.name);
        root.add("course", courseJ);

        JsonArray groups = new JsonArray();
        Map<Long, UUID> groupsUUID = new HashMap<>();
        root.add("groups", groups);

        // CourseGroup
        course.groups.stream().forEach(group -> {
            JsonObject groupJ = new JsonObject();
            UUID groupU = UUID.randomUUID();
            uuidMap.put(groupU, group.id);
            groupJ.addProperty("uuid", groupU.toString());
            groupJ.addProperty("groupName", group.groupName);
            groups.add(groupJ);
            groupsUUID.put(group.id, groupU);
        });

        // Exam
        JsonArray exams = new JsonArray();
        Map<Long, UUID> examsUID = new HashMap<>();
        root.add("exams", exams);
        course.exams.stream().forEach(exam -> {
            JsonObject examJ = new JsonObject();
            UUID examU = UUID.randomUUID();
            uuidMap.put(examU, exam.id);
            examJ.addProperty("uuid", examU.toString());
            examJ.addProperty("name", exam.name);
            exams.add(examJ);
            examsUID.put(exam.id, examU);
        });

        // Student
        JsonArray students = new JsonArray();
        Map<Long, UUID> studentsUID = new HashMap<>();
        root.add("students", students);
        groupsUUID.keySet().stream().forEach(groupid -> {
            CourseGroup group = CourseGroup.findById(groupid);
            group.students.stream().forEach(st -> {
                Student student = Student.findById(st.id);
                JsonObject studentJ = new JsonObject();
                UUID studentU = UUID.randomUUID();
                uuidMap.put(studentU, student.id);
                studentJ.addProperty("uuid", studentU.toString());
                studentJ.addProperty("name", student.name);
                studentJ.addProperty("firstname", student.firstname);
                studentJ.addProperty("caslogin", student.caslogin);
                studentJ.addProperty("ine", student.ine);
                studentJ.addProperty("mail", student.mail);
                students.add(studentJ);
                studentsUID.put(student.id, studentU);
            });
        });

        // Templates
        JsonArray templates = new JsonArray();
        Map<Long, UUID> templatesUID = new HashMap<>();
        root.add("templates", templates);
        examsUID.keySet().stream().forEach(examid -> {
            Exam exam = Exam.findById(examid);
            if (exam.template != null) {

                Template template = Template.findById(exam.template.id);

                JsonObject templateJ = new JsonObject();
                UUID templateU = UUID.randomUUID();
                uuidMap.put(templateU, template.id);
                templateJ.addProperty("uuid", templateU.toString());
                templateJ.addProperty("name", template.name);
                templateJ.addProperty("autoMapStudentCopyToList", template.autoMapStudentCopyToList);
                templateJ.addProperty("contentContentType", template.contentContentType);
                if (this.uses3) {
                    String fileName = "template/" + +template.id + ".pdf";
                    try {
                        Base64.Encoder encoder = Base64.getEncoder();
                        byte[] encodedContent = encoder
                                .encode(this.fichierS3Service.getObject(fileName).readAllBytes());
                        templateJ.addProperty("content", new String(encodedContent));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Base64.Encoder encoder = Base64.getEncoder();
                    byte[] encodedContent = encoder
                            .encode(template.content);

                    templateJ.addProperty("content", new String(encodedContent));

                }
                templateJ.addProperty("mark", template.mark);
                templates.add(templateJ);
                templatesUID.put(template.id, templateU);
            }

        });

        // Scans

        JsonArray scans = new JsonArray();
        Map<Long, UUID> scansUID = new HashMap<>();
        root.add("scans", scans);
        examsUID.keySet().stream().forEach(examid -> {
            Exam exam = Exam.findById(examid);
            if (exam.scanfile != null) {
                Scan scan = Scan.findById(exam.scanfile.id);
                JsonObject scanJ = new JsonObject();
                UUID scanU = UUID.randomUUID();
                uuidMap.put(scanU, scan.id);
                scanJ.addProperty("uuid", scanU.toString());
                scanJ.addProperty("name", scan.name);
                if (this.uses3) {
                    String fileName = "scan/" + +scan.id + ".pdf";
                    try {
                        Base64.Encoder encoder = Base64.getEncoder();
                        byte[] encodedContent = encoder
                                .encode(this.fichierS3Service.getObject(fileName).readAllBytes());
                        scanJ.addProperty("content", new String(encodedContent));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Base64.Encoder encoder = Base64.getEncoder();
                    byte[] encodedContent = encoder
                            .encode(scan.content);
                    scanJ.addProperty("content", new String(encodedContent));

                }

                scanJ.addProperty("contentContentType", scan.contentContentType);
                scans.add(scanJ);
                scansUID.put(scan.id, scanU);
            }
        });

        // Zones

        JsonArray zones = new JsonArray();
        Map<Long, UUID> zonesUID = new HashMap<>();
        root.add("zones", zones);
        examsUID.keySet().stream().forEach(examid -> {
            Exam exam = Exam.findById(examid);
            if (exam.idzone != null) {
                Zone zone = Zone.findById(exam.idzone.id);
                createZone("idzone", zone, zonesUID, zones, uuidMap);
            }
            if (exam.namezone != null) {
                Zone zone = Zone.findById(exam.namezone.id);
                createZone("namezone", zone, zonesUID, zones, uuidMap);
            }
            if (exam.firstnamezone != null) {
                Zone zone = Zone.findById(exam.firstnamezone.id);
                createZone("firstnamezone", zone, zonesUID, zones, uuidMap);
            }
            if (exam.notezone != null) {
                Zone zone = Zone.findById(exam.notezone.id);
                createZone("notezone", zone, zonesUID, zones, uuidMap);
            }
        });

        // Questions

        JsonArray questions = new JsonArray();
        Map<Long, UUID> questionsUID = new HashMap<>();
        root.add("questions", questions);
        examsUID.keySet().stream().forEach(examid -> {
            Exam exam = Exam.findById(examid);
            exam.questions.stream().forEach(q -> {
                Question question = Question.findById(q.id);
                JsonObject questionJ = new JsonObject();
                UUID questionU = UUID.randomUUID();
                uuidMap.put(questionU, question.id);
                questionJ.addProperty("uuid", questionU.toString());
                questionJ.addProperty("numero", question.numero);
                questionJ.addProperty("quarterpoint", question.quarterpoint);
                questionJ.addProperty("step", question.step);
                questionJ.addProperty("validExpression", question.validExpression);
                questionJ.addProperty("gradeType", question.gradeType.name());
                questionJ.addProperty("type", question.type.algoName);
                if (question.zone != null) {

                    Zone zone = Zone.findById(question.zone.id);
                    createZone("question", zone, zonesUID, zones, uuidMap);
                }
                questions.add(questionJ);
                questionsUID.put(question.id, questionU);
            });
        });

        // TextComments

        JsonArray textcomments = new JsonArray();
        Map<Long, UUID> textcommentsUID = new HashMap<>();
        root.add("textcomments", textcomments);
        questionsUID.keySet().stream().forEach(questionId -> {
            Question question = Question.findById(questionId);
            question.textcomments.stream().forEach(t -> {
                TextComment textcomment = TextComment.findById(t.id);
                JsonObject textcommentJ = new JsonObject();
                UUID textcommentU = UUID.randomUUID();
                uuidMap.put(textcommentU, textcomment.id);
                textcommentJ.addProperty("uuid", textcommentU.toString());
                textcommentJ.addProperty("text", textcomment.text);
                textcommentJ.addProperty("description", textcomment.description);
                textcommentJ.addProperty("zonegeneratedid", textcomment.zonegeneratedid);
                textcomments.add(textcommentJ);
                textcommentsUID.put(textcomment.id, textcommentU);
            });
        });

        // GradedComments

        JsonArray gradedcomments = new JsonArray();
        Map<Long, UUID> gradedcommentsUID = new HashMap<>();
        root.add("gradedcomments", gradedcomments);
        questionsUID.keySet().stream().forEach(questionId -> {
            Question question = Question.findById(questionId);
            question.gradedcomments.stream().forEach(t -> {
                GradedComment gradedcomment = GradedComment.findById(t.id);
                JsonObject gradedcommentJ = new JsonObject();
                UUID gradedcommentU = UUID.randomUUID();
                uuidMap.put(gradedcommentU, gradedcomment.id);
                gradedcommentJ.addProperty("uuid", gradedcommentU.toString());
                gradedcommentJ.addProperty("text", gradedcomment.text);
                gradedcommentJ.addProperty("description", gradedcomment.description);
                gradedcommentJ.addProperty("gradequarter", gradedcomment.gradequarter);
                gradedcommentJ.addProperty("zonegeneratedid", gradedcomment.zonegeneratedid);
                gradedcomments.add(gradedcommentJ);
                gradedcommentsUID.put(gradedcomment.id, gradedcommentU);
            });
        });

        // ExamSheets

        JsonArray examSheets = new JsonArray();
        Map<Long, UUID> examSheetsUID = new HashMap<>();
        root.add("examSheets", examSheets);
        scansUID.keySet().stream().forEach(scanid -> {
            List<ExamSheet> examSheets_ = ExamSheet.findExamSheetByScan(scanid).list();
            examSheets_.forEach(examSheet -> {
                JsonObject examSheetJ = new JsonObject();
                UUID examSheetU = UUID.randomUUID();
                uuidMap.put(examSheetU, examSheet.id);
                examSheetJ.addProperty("uuid", examSheetU.toString());
                examSheetJ.addProperty("name", examSheet.name);
                examSheetJ.addProperty("pagemax", examSheet.pagemax);
                examSheetJ.addProperty("pagemin", examSheet.pagemin);
                examSheets.add(examSheetJ);
                examSheetsUID.put(examSheet.id, examSheetU);

            });

        });

        // StudentResponse

        JsonArray studentResponses = new JsonArray();
        Map<Long, UUID> studentResponsesUID = new HashMap<>();
        root.add("studentResponses", studentResponses);
        examsUID.keySet().stream().forEach(examId -> {
            List<StudentResponse> studentResponses_ = StudentResponse.getAll4ExamId(examId).list();
            studentResponses_.forEach(studentResponse -> {
                JsonObject studentResponseJ = new JsonObject();
                UUID studentResponseU = UUID.randomUUID();
                uuidMap.put(studentResponseU, studentResponse.id);
                studentResponseJ.addProperty("uuid", studentResponseU.toString());
                studentResponseJ.addProperty("star", studentResponse.star);
                studentResponseJ.addProperty("worststar", studentResponse.worststar);
                studentResponseJ.addProperty("quarternote", studentResponse.quarternote);
                studentResponses.add(studentResponseJ);
                studentResponsesUID.put(studentResponse.id, studentResponseU);

            });
        });

        // Comment

        JsonArray comments = new JsonArray();
        Map<Long, UUID> commentsUID = new HashMap<>();
        root.add("comments", comments);
        examsUID.keySet().stream().forEach(examId -> {
            List<Comments> comments_ = Comments.findCommentByExamId("" + examId).list();
            comments_.forEach(comment -> {
                JsonObject commentJ = new JsonObject();
                UUID commentU = UUID.randomUUID();
                uuidMap.put(commentU, comment.id);
                commentJ.addProperty("uuid", commentU.toString());
                commentJ.addProperty("jsonData", comment.jsonData);
                commentJ.addProperty("zonegeneratedid", comment.zonegeneratedid);
                comments.add(commentJ);
                commentsUID.put(comment.id, commentU);

            });
        });

        // FinalResult

        JsonArray finalResults = new JsonArray();
        Map<Long, UUID> finalResultsUID = new HashMap<>();
        root.add("finalResults", finalResults);
        examsUID.keySet().stream().forEach(examId -> {
            List<FinalResult> finalResults_ = FinalResult.getAll4ExamId(examId).list();
            finalResults_.forEach(finalResult -> {
                JsonObject finalResultJ = new JsonObject();
                UUID finalResultU = UUID.randomUUID();
                uuidMap.put(finalResultU, finalResult.id);
                finalResultJ.addProperty("uuid", finalResultU.toString());
                finalResultJ.addProperty("note", finalResult.note);
                finalResults.add(finalResultJ);
                finalResultsUID.put(finalResult.id, finalResultU);

            });
        });

        JsonArray coursegroupsR = new JsonArray();
        root.add("coursegroupsR", coursegroupsR);
        course.groups.forEach(gr -> {
            JsonObject ob = new JsonObject();
            ob.addProperty("left", courseU.toString());
            ob.addProperty("right", groupsUUID.get(gr.id).toString());
            coursegroupsR.add(ob);
        });

        JsonArray groupsstudentR = new JsonArray();
        root.add("groupsstudentR", groupsstudentR);
        groupsUUID.keySet().forEach(groupId -> {
            CourseGroup gr = CourseGroup.findById(groupId);
            gr.students.forEach(st -> {
                JsonObject ob = new JsonObject();
                ob.addProperty("left", groupsUUID.get(gr.id).toString());
                ob.addProperty("right", studentsUID.get(st.id).toString());
                groupsstudentR.add(ob);
            });
        });

        JsonArray courseExamR = new JsonArray();
        root.add("courseExamR", courseExamR);
        course.exams.forEach(ex -> {
            JsonObject ob = new JsonObject();
            ob.addProperty("left", courseU.toString());
            ob.addProperty("right", examsUID.get(ex.id).toString());
            courseExamR.add(ob);
        });

        JsonArray examstemplatesR = new JsonArray();
        root.add("examstemplatesR", examstemplatesR);
        examsUID.keySet().forEach(exid -> {
            Exam ex = Exam.findById(exid);
            JsonObject ob = new JsonObject();
            ob.addProperty("left", examsUID.get(exid).toString());
            if (ex.template != null) {
                ob.addProperty("right", templatesUID.get(ex.template.id).toString());
                examstemplatesR.add(ob);
            }

        });

        JsonArray examsidzoneR = new JsonArray();
        root.add("examsidzoneR", examsidzoneR);
        examsUID.keySet().forEach(exid -> {
            Exam ex = Exam.findById(exid);
            JsonObject ob = new JsonObject();
            ob.addProperty("left", examsUID.get(exid).toString());
            if (ex.idzone != null) {
                ob.addProperty("right", zonesUID.get(ex.idzone.id).toString());
                examsidzoneR.add(ob);
            }
        });

        JsonArray examsnamezoneR = new JsonArray();
        root.add("examsnamezoneR", examsnamezoneR);
        examsUID.keySet().forEach(exid -> {
            Exam ex = Exam.findById(exid);
            JsonObject ob = new JsonObject();
            ob.addProperty("left", examsUID.get(exid).toString());

            if (ex.namezone != null) {
                ob.addProperty("right", zonesUID.get(ex.namezone.id).toString());
                examsnamezoneR.add(ob);
            }
        });

        JsonArray examsfirstnamezoneR = new JsonArray();
        root.add("examsfirstnamezoneR", examsfirstnamezoneR);
        examsUID.keySet().forEach(exid -> {
            Exam ex = Exam.findById(exid);
            JsonObject ob = new JsonObject();
            ob.addProperty("left", examsUID.get(exid).toString());
            if (ex.firstnamezone != null) {
                ob.addProperty("right", zonesUID.get(ex.firstnamezone.id).toString());
                examsfirstnamezoneR.add(ob);
            }
        });

        JsonArray examsnotezoneR = new JsonArray();
        root.add("examsnotezoneR", examsnotezoneR);
        examsUID.keySet().forEach(exid -> {
            Exam ex = Exam.findById(exid);
            JsonObject ob = new JsonObject();
            ob.addProperty("left", examsUID.get(exid).toString());
            if (ex.notezone != null) {
                ob.addProperty("right", zonesUID.get(ex.notezone.id).toString());
                examsnotezoneR.add(ob);
            }
        });

        JsonArray examsscanfileR = new JsonArray();
        root.add("examsscanfileR", examsscanfileR);
        examsUID.keySet().forEach(exid -> {
            Exam ex = Exam.findById(exid);
            JsonObject ob = new JsonObject();
            ob.addProperty("left", examsUID.get(exid).toString());

            if (ex.scanfile != null) {
                ob.addProperty("right", scansUID.get(ex.scanfile.id).toString());
                examsscanfileR.add(ob);
            }
        });

        JsonArray examsquestionsR = new JsonArray();
        root.add("examsquestionsR", examsquestionsR);
        examsUID.keySet().forEach(exid -> {
            Exam ex = Exam.findById(exid);
            ex.questions.forEach(q -> {
                JsonObject ob = new JsonObject();
                ob.addProperty("left", examsUID.get(exid).toString());
                ob.addProperty("right", questionsUID.get(q.id).toString());
                examsscanfileR.add(ob);

            });
        });

        JsonArray finalResultStudentR = new JsonArray();
        root.add("finalResultStudentR", finalResultStudentR);
        finalResultsUID.keySet().forEach(frid -> {
            FinalResult fr = FinalResult.findById(frid);
            JsonObject ob = new JsonObject();
            ob.addProperty("left", finalResultsUID.get(frid).toString());
            if (fr.student != null && studentsUID.get(fr.student.id) != null) {
                ob.addProperty("right", studentsUID.get(fr.student.id).toString());
                finalResultStudentR.add(ob);
            }
        });

        JsonArray finalResultExamR = new JsonArray();
        root.add("finalResultExamR", finalResultExamR);
        finalResultsUID.keySet().forEach(frid -> {
            FinalResult fr = FinalResult.findById(frid);
            JsonObject ob = new JsonObject();
            ob.addProperty("left", finalResultsUID.get(frid).toString());
            if (fr.exam != null) {

                ob.addProperty("right", examsUID.get(fr.exam.id).toString());
                finalResultStudentR.add(ob);
            }

        });

        JsonArray studentResponsesQuestionR = new JsonArray();
        root.add("studentResponsesQuestionR", studentResponsesQuestionR);
        studentResponsesUID.keySet().forEach(srid -> {
            StudentResponse sr = StudentResponse.findById(srid);
            JsonObject ob = new JsonObject();
            ob.addProperty("left", studentResponsesUID.get(srid).toString());
            if (sr.question != null) {
                ob.addProperty("right", questionsUID.get(sr.question.id).toString());
                studentResponsesQuestionR.add(ob);

            }
        });

        JsonArray studentResponsesExamSheetR = new JsonArray();
        root.add("studentResponsesExamSheetR", studentResponsesExamSheetR);
        studentResponsesUID.keySet().forEach(srid -> {
            StudentResponse sr = StudentResponse.findById(srid);
            JsonObject ob = new JsonObject();
            ob.addProperty("left", studentResponsesUID.get(srid).toString());
            if (sr.sheet != null) {
                ob.addProperty("right", examSheetsUID.get(sr.sheet.id).toString());
                studentResponsesExamSheetR.add(ob);
            }
        });

        JsonArray studentResponsesCommentsR = new JsonArray();
        root.add("studentResponsesCommentsR", studentResponsesCommentsR);
        studentResponsesUID.keySet().forEach(srid -> {
            StudentResponse sr = StudentResponse.findById(srid);
            sr.comments.forEach(comment -> {
                JsonObject ob = new JsonObject();
                ob.addProperty("left", studentResponsesUID.get(srid).toString());
                ob.addProperty("right", commentsUID.get(comment.id).toString());
                studentResponsesCommentsR.add(ob);
            });
        });

        JsonArray scanExamSheetsR = new JsonArray();
        root.add("scanExamSheetsR", scanExamSheetsR);
        scansUID.keySet().forEach(sid -> {
            Scan s = Scan.findById(sid);
            s.sheets.forEach(sheet -> {
                JsonObject ob = new JsonObject();
                ob.addProperty("left", scansUID.get(sid).toString());
                ob.addProperty("right", examSheetsUID.get(sheet.id).toString());
                scanExamSheetsR.add(ob);
            });
        });

        JsonArray questionZoneR = new JsonArray();
        root.add("questionZoneR", questionZoneR);
        questionsUID.keySet().forEach(sid -> {
            Question s = Question.findById(sid);
            if (s.zone != null) {
                JsonObject ob = new JsonObject();
                ob.addProperty("left", questionsUID.get(sid).toString());
                ob.addProperty("right", zonesUID.get(s.zone.id).toString());
                questionZoneR.add(ob);

            }
        });

        JsonArray questionTextCommentsR = new JsonArray();
        root.add("questionTextCommentsR", questionTextCommentsR);
        questionsUID.keySet().forEach(sid -> {
            Question s = Question.findById(sid);
            s.textcomments.forEach(tc -> {
                JsonObject ob = new JsonObject();
                ob.addProperty("left", questionsUID.get(sid).toString());
                ob.addProperty("right", textcommentsUID.get(tc.id).toString());
                questionTextCommentsR.add(ob);
            });
        });

        JsonArray questionGradedCommentsR = new JsonArray();
        root.add("questionGradedCommentsR", questionGradedCommentsR);
        questionsUID.keySet().forEach(sid -> {
            Question s = Question.findById(sid);
            s.gradedcomments.forEach(gc -> {
                JsonObject ob = new JsonObject();
                ob.addProperty("left", questionsUID.get(sid).toString());
                ob.addProperty("right", gradedcommentsUID.get(gc.id).toString());
                questionGradedCommentsR.add(ob);
            });
        });

        JsonArray studentResponseTextCommentsR = new JsonArray();
        root.add("studentResponseTextCommentsR", studentResponseTextCommentsR);
        studentResponsesUID.keySet().forEach(sid -> {
            StudentResponse s = StudentResponse.findById(sid);
            s.textcomments.forEach(tc -> {
                JsonObject ob = new JsonObject();
                ob.addProperty("left", studentResponsesUID.get(sid).toString());
                ob.addProperty("right", textcommentsUID.get(tc.id).toString());
                studentResponseTextCommentsR.add(ob);
            });
        });

        JsonArray studentResponseGradedCommentsR = new JsonArray();
        root.add("studentResponseGradedCommentsR", studentResponseGradedCommentsR);
        studentResponsesUID.keySet().forEach(sid -> {
            StudentResponse s = StudentResponse.findById(sid);
            s.gradedcomments.forEach(gc -> {
                JsonObject ob = new JsonObject();
                ob.addProperty("left", studentResponsesUID.get(sid).toString());
                ob.addProperty("right", gradedcommentsUID.get(gc.id).toString());
                studentResponseGradedCommentsR.add(ob);
            });
        });

        JsonArray studentExamSheetR = new JsonArray();
        root.add("studentExamSheetR", studentExamSheetR);
        studentsUID.keySet().forEach(sid -> {
            Student s = Student.findById(sid);
            s.examSheets.forEach(gc -> {
                JsonObject ob = new JsonObject();
                ob.addProperty("left", studentsUID.get(sid).toString());
                ob.addProperty("right", examSheetsUID.get(gc.id).toString());
                studentExamSheetR.add(ob);
            });
        });

        JsonArray studentIdUidMappings = new JsonArray();
        root.add("studentIdUidMappings", studentIdUidMappings);
        studentsUID.entrySet().forEach(sid -> {
            JsonObject ob = new JsonObject();
            ob.addProperty("left", sid.getKey());
            ob.addProperty("right", sid.getValue().toString());
            studentIdUidMappings.add(ob);
        });

        JsonArray examIdUidMappings = new JsonArray();
        root.add("examIdUidMappings", examIdUidMappings);
        examsUID.entrySet().forEach(sid -> {
            JsonObject ob = new JsonObject();
            ob.addProperty("left", sid.getKey());
            ob.addProperty("right", sid.getValue().toString());
            examIdUidMappings.add(ob);
        });

        return root;

    }

    private void createZone(String idzone, Zone zone, Map<Long, UUID> zonesUID, JsonArray zones,
            Map<UUID, Long> uuidMap) {
        JsonObject zoneJ = new JsonObject();
        UUID zoneU = UUID.randomUUID();
        uuidMap.put(zoneU, zone.id);
        zoneJ.addProperty("uuid", zoneU.toString());
        zoneJ.addProperty("height", zone.height);
        zoneJ.addProperty("width", zone.width);
        zoneJ.addProperty("xInit", zone.xInit);
        zoneJ.addProperty("yInit", zone.yInit);
        zoneJ.addProperty("pageNumber", zone.pageNumber);
        zoneJ.addProperty("idzone", idzone);
        zones.add(zoneJ);
        zonesUID.put(zone.id, zoneU);
    }

    public Course importCourse(JsonObject _course) {
        Map<Long, String> importstudentsUID = new HashMap<>();
        Map<Long, String> importexamsUID = new HashMap<>();
        _course.getAsJsonArray("studentIdUidMappings").forEach(st -> {
            importstudentsUID.put(st.getAsJsonObject().get("left").getAsLong(),
                    st.getAsJsonObject().get("right").getAsString());
        });

        _course.getAsJsonArray("examIdUidMappings").forEach(st -> {
            importexamsUID.put(st.getAsJsonObject().get("left").getAsLong(),
                    st.getAsJsonObject().get("right").getAsString());
        });

        // Course
        Course course = new Course();
        JsonObject courseJ = _course.getAsJsonObject("course");
        course.name = courseJ.get("name").getAsString();
        course.persistAndFlush();

        Map<String, Long> uuidId = new HashMap<>();
        _course.getAsJsonArray("groups").forEach(gr -> {
            CourseGroup group = new CourseGroup();
            group.groupName = gr.getAsJsonObject().get("groupName").getAsString();
            group.persistAndFlush();
            uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), group.id);
        });

        _course.getAsJsonArray("exams").forEach(gr -> {
            Exam exam = new Exam();
            exam.name = gr.getAsJsonObject().get("name").getAsString();
            exam.persistAndFlush();
            uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), exam.id);
        });

        _course.getAsJsonArray("students").forEach(gr -> {
            Student student = new Student();
            student.name = gr.getAsJsonObject().get("name").getAsString();
            student.firstname = gr.getAsJsonObject().get("firstname").getAsString();
            student.caslogin = gr.getAsJsonObject().get("caslogin").getAsString();
            student.ine = gr.getAsJsonObject().get("ine").getAsString();
            student.mail = gr.getAsJsonObject().get("mail").getAsString();
            student.persistAndFlush();
            uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), student.id);
        });

        _course.getAsJsonArray("templates").forEach(gr -> {
            Template template = new Template();
            template.name = gr.getAsJsonObject().get("name").getAsString();
            template.autoMapStudentCopyToList = gr.getAsJsonObject().get("autoMapStudentCopyToList").getAsBoolean();
            template.contentContentType = gr.getAsJsonObject().get("contentContentType").getAsString();
            template.mark = gr.getAsJsonObject().get("mark").getAsBoolean();
            template.persistAndFlush();

            byte[] bytes = gr.getAsJsonObject().get("content").getAsString().getBytes();

            Base64.Decoder decoder = Base64.getDecoder();
            byte[] b64bytes = decoder.decode(bytes);

            if (this.uses3) {
                String fileName = "template/" + template.id + ".pdf";
                try {
                    this.putObject(fileName, b64bytes, template.contentContentType);
                } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException | IOException e) {
                    e.printStackTrace();
                }
            } else {
                template.content = b64bytes;
                template.persistAndFlush();
            }

            uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), template.id);
        });

        _course.getAsJsonArray("templates").forEach(gr -> {
            Scan scan = new Scan();
            scan.name = gr.getAsJsonObject().get("name").getAsString();
            scan.contentContentType = gr.getAsJsonObject().get("contentContentType").getAsString();
            scan.persistAndFlush();
            byte[] bytes = gr.getAsJsonObject().get("content").getAsString().getBytes();

            Base64.Decoder decoder = Base64.getDecoder();
            byte[] b64bytes = decoder.decode(bytes);

            if (this.uses3) {
                String fileName = "scan/" + scan.id + ".pdf";
                try {
                    this.putObject(fileName, b64bytes, scan.contentContentType);
                } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException | IOException e) {
                    e.printStackTrace();
                }
            } else {
                scan.content = b64bytes;
                scan.persistAndFlush();
            }
            uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), scan.id);
        });

        _course.getAsJsonArray("zones").forEach(gr -> {
            Zone zone = new Zone();
            zone.height = gr.getAsJsonObject().get("height").getAsInt();
            zone.width = gr.getAsJsonObject().get("width").getAsInt();
            zone.xInit = gr.getAsJsonObject().get("xInit").getAsInt();
            zone.yInit = gr.getAsJsonObject().get("yInit").getAsInt();
            zone.pageNumber = gr.getAsJsonObject().get("pageNumber").getAsInt();
            zone.persistAndFlush();
            uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), zone.id);
        });

        // Questions
        _course.getAsJsonArray("questions").forEach(gr -> {
            Question question = new Question();
            question.numero = gr.getAsJsonObject().get("numero").getAsInt();
            question.quarterpoint = gr.getAsJsonObject().get("quarterpoint").getAsInt();
            question.step = gr.getAsJsonObject().get("step").getAsInt();
            question.validExpression = gr.getAsJsonObject().get("validExpression").getAsString();
            question.gradeType = GradeType.valueOf(gr.getAsJsonObject().get("gradeType").getAsString());
            question.type = QuestionType.findQuestionTypebyAlgoName(gr.getAsJsonObject().get("type").getAsString())
                    .firstResult();
            question.persistAndFlush();
            uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), question.id);
        });

        // TextComments
        _course.getAsJsonArray("questions").forEach(gr -> {
            TextComment textComment = new TextComment();
            textComment.text = gr.getAsJsonObject().get("text").getAsString();
            textComment.description = gr.getAsJsonObject().get("description").getAsString();
            textComment.zonegeneratedid = gr.getAsJsonObject().get("zonegeneratedid").getAsString();
            textComment.persistAndFlush();
            uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), textComment.id);
        });

        // GradedComments
        _course.getAsJsonArray("questions").forEach(gr -> {
            GradedComment gradedComment = new GradedComment();
            gradedComment.text = gr.getAsJsonObject().get("text").getAsString();
            gradedComment.description = gr.getAsJsonObject().get("description").getAsString();
            gradedComment.zonegeneratedid = gr.getAsJsonObject().get("zonegeneratedid").getAsString();
            gradedComment.gradequarter = gr.getAsJsonObject().get("gradequarter").getAsInt();
            gradedComment.persistAndFlush();
            uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), gradedComment.id);
        });

        // ExamSheets
        _course.getAsJsonArray("examSheets").forEach(gr -> {
            ExamSheet examSheet = new ExamSheet();
            examSheet.name = gr.getAsJsonObject().get("name").getAsString();
            examSheet.pagemax = gr.getAsJsonObject().get("pagemax").getAsInt();
            examSheet.pagemin = gr.getAsJsonObject().get("pagemin").getAsInt();
            examSheet.persistAndFlush();
            uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), examSheet.id);
        });

        // StudentResponse

        _course.getAsJsonArray("studentResponses").forEach(gr -> {
            StudentResponse studentResponse = new StudentResponse();
            studentResponse.star = gr.getAsJsonObject().get("star").getAsBoolean();
            studentResponse.worststar = gr.getAsJsonObject().get("worststar").getAsBoolean();
            studentResponse.quarternote = gr.getAsJsonObject().get("quarternote").getAsInt();
            studentResponse.persistAndFlush();
            uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), studentResponse.id);
        });

        // FinalResult
        _course.getAsJsonArray("finalResults").forEach(gr -> {
            FinalResult finalResult = new FinalResult();
            finalResult.note = gr.getAsJsonObject().get("note").getAsInt();
            finalResult.persistAndFlush();
            uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), finalResult.id);
        });

        // Comment
        _course.getAsJsonArray("comments").forEach(gr -> {
            Comments comment = new Comments();
            comment.jsonData = gr.getAsJsonObject().get("jsonData").getAsString();
            comment.zonegeneratedid = gr.getAsJsonObject().get("zonegeneratedid").getAsString();
            // TODO
            StringTokenizer t = new StringTokenizer(comment.zonegeneratedid, "_");
            if (t.countTokens() > 1) {
                String examId = t.nextToken();
                String studentId = t.nextToken();
                String questiono = t.nextToken();
                String index = t.nextToken();
                /*
                 * '' + this.exam!.id + '_' + this.selectionStudents![0].id + '_' +
                 * this.questionno + '_' + index
                 *
                 */
                comment.zonegeneratedid = "" + uuidId.get(importexamsUID.get(Long.parseLong(examId))) + "_" +
                        uuidId.get(importstudentsUID.get(Long.parseLong(studentId))) + "_" + questiono + "_" + index;
            }

            comment.persistAndFlush();
            uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), comment.id);
        });

        _course.getAsJsonArray("coursegroupsR").forEach(gr -> {
            // String left = gr.getAsJsonObject().get("left").getAsString();
            String right = gr.getAsJsonObject().get("right").getAsString();
            Course c = Course.findById(course.id);
            CourseGroup cg = CourseGroup.findById(uuidId.get(right));
            cg.course = c;
            c.groups.add(cg);
        });

        _course.getAsJsonArray("groupsstudentR").forEach(gr -> {
            String left = gr.getAsJsonObject().get("left").getAsString();
            String right = gr.getAsJsonObject().get("right").getAsString();
            CourseGroup cg = CourseGroup.findById(uuidId.get(left));
            Student st = Student.findById(uuidId.get(right));
            cg.students.add(st);
            st.groups.add(cg);
        });

        _course.getAsJsonArray("courseExamR").forEach(gr -> {
            // String left = gr.getAsJsonObject().get("left").getAsString();
            String right = gr.getAsJsonObject().get("right").getAsString();
            Course c = Course.findById(course.id);
            Exam ex = Exam.findById(uuidId.get(right));
            c.exams.add(ex);
            ex.course = c;
        });

        _course.getAsJsonArray("examstemplatesR").forEach(gr -> {
            String left = gr.getAsJsonObject().get("left").getAsString();
            String right = gr.getAsJsonObject().get("right").getAsString();
            Exam ex = Exam.findById(uuidId.get(left));
            Template st = Template.findById(uuidId.get(right));
            ex.template = st;
            st.exam = ex;
        });

        _course.getAsJsonArray("examsidzoneR").forEach(gr -> {
            String left = gr.getAsJsonObject().get("left").getAsString();
            String right = gr.getAsJsonObject().get("right").getAsString();
            Exam ex = Exam.findById(uuidId.get(left));
            Zone st = Zone.findById(uuidId.get(right));
            ex.idzone = st;
        });

        _course.getAsJsonArray("examsnamezoneR").forEach(gr -> {
            String left = gr.getAsJsonObject().get("left").getAsString();
            String right = gr.getAsJsonObject().get("right").getAsString();
            Exam ex = Exam.findById(uuidId.get(left));
            Zone st = Zone.findById(uuidId.get(right));
            ex.namezone = st;
        });

        _course.getAsJsonArray("examsfirstnamezoneR").forEach(gr -> {
            String left = gr.getAsJsonObject().get("left").getAsString();
            String right = gr.getAsJsonObject().get("right").getAsString();
            Exam ex = Exam.findById(uuidId.get(left));
            Zone st = Zone.findById(uuidId.get(right));
            ex.firstnamezone = st;
        });

        _course.getAsJsonArray("examsnotezoneR").forEach(gr -> {
            String left = gr.getAsJsonObject().get("left").getAsString();
            String right = gr.getAsJsonObject().get("right").getAsString();
            Exam ex = Exam.findById(uuidId.get(left));
            Zone st = Zone.findById(uuidId.get(right));
            ex.notezone = st;
        });

        _course.getAsJsonArray("examsscanfileR").forEach(gr -> {
            String left = gr.getAsJsonObject().get("left").getAsString();
            String right = gr.getAsJsonObject().get("right").getAsString();
            Exam ex = Exam.findById(uuidId.get(left));
            Scan st = Scan.findById(uuidId.get(right));
            ex.scanfile = st;
        });

        _course.getAsJsonArray("examsquestionsR").forEach(gr -> {
            String left = gr.getAsJsonObject().get("left").getAsString();
            String right = gr.getAsJsonObject().get("right").getAsString();
            Exam ex = Exam.findById(uuidId.get(left));
            Question st = Question.findById(uuidId.get(right));
            ex.questions.add(st);
            st.exam = ex;
        });

        _course.getAsJsonArray("finalResultStudentR").forEach(gr -> {
            String left = gr.getAsJsonObject().get("left").getAsString();
            String right = gr.getAsJsonObject().get("right").getAsString();
            FinalResult ex = FinalResult.findById(uuidId.get(left));
            Student st = Student.findById(uuidId.get(right));
            ex.student = st;
        });

        _course.getAsJsonArray("finalResultExamR").forEach(gr -> {
            String left = gr.getAsJsonObject().get("left").getAsString();
            String right = gr.getAsJsonObject().get("right").getAsString();
            FinalResult ex = FinalResult.findById(uuidId.get(left));
            Exam st = Exam.findById(uuidId.get(right));
            ex.exam = st;
        });

        _course.getAsJsonArray("studentResponsesQuestionR").forEach(gr -> {
            String left = gr.getAsJsonObject().get("left").getAsString();
            String right = gr.getAsJsonObject().get("right").getAsString();
            StudentResponse ex = StudentResponse.findById(uuidId.get(left));
            Question st = Question.findById(uuidId.get(right));
            ex.question = st;
        });

        _course.getAsJsonArray("studentResponsesExamSheetR").forEach(gr -> {
            String left = gr.getAsJsonObject().get("left").getAsString();
            String right = gr.getAsJsonObject().get("right").getAsString();
            StudentResponse ex = StudentResponse.findById(uuidId.get(left));
            ExamSheet st = ExamSheet.findById(uuidId.get(right));
            ex.sheet = st;
        });

        _course.getAsJsonArray("studentResponsesCommentsR").forEach(gr -> {
            String left = gr.getAsJsonObject().get("left").getAsString();
            String right = gr.getAsJsonObject().get("right").getAsString();
            StudentResponse ex = StudentResponse.findById(uuidId.get(left));
            Comments st = Comments.findById(uuidId.get(right));
            ex.comments.add(st);
            st.studentResponse = ex;
        });

        _course.getAsJsonArray("scanExamSheetsR").forEach(gr -> {
            String left = gr.getAsJsonObject().get("left").getAsString();
            String right = gr.getAsJsonObject().get("right").getAsString();
            Scan ex = Scan.findById(uuidId.get(left));
            ExamSheet st = ExamSheet.findById(uuidId.get(right));
            ex.sheets.add(st);
            st.scan = ex;
        });

        _course.getAsJsonArray("questionTextCommentsR").forEach(gr -> {
            String left = gr.getAsJsonObject().get("left").getAsString();
            String right = gr.getAsJsonObject().get("right").getAsString();
            Question ex = Question.findById(uuidId.get(left));
            TextComment st = TextComment.findById(uuidId.get(right));
            ex.textcomments.add(st);
            st.question = ex;
        });

        _course.getAsJsonArray("questionGradedCommentsR").forEach(gr -> {
            String left = gr.getAsJsonObject().get("left").getAsString();
            String right = gr.getAsJsonObject().get("right").getAsString();
            Question ex = Question.findById(uuidId.get(left));
            GradedComment st = GradedComment.findById(uuidId.get(right));
            ex.gradedcomments.add(st);
            st.question = ex;
        });

        _course.getAsJsonArray("studentResponseTextCommentsR").forEach(gr -> {
            String left = gr.getAsJsonObject().get("left").getAsString();
            String right = gr.getAsJsonObject().get("right").getAsString();
            StudentResponse ex = StudentResponse.findById(uuidId.get(left));
            TextComment st = TextComment.findById(uuidId.get(right));
            ex.textcomments.add(st);
            st.studentResponses.add(ex);
        });

        _course.getAsJsonArray("studentResponseGradedCommentsR").forEach(gr -> {
            String left = gr.getAsJsonObject().get("left").getAsString();
            String right = gr.getAsJsonObject().get("right").getAsString();
            StudentResponse ex = StudentResponse.findById(uuidId.get(left));
            GradedComment st = GradedComment.findById(uuidId.get(right));
            ex.gradedcomments.add(st);
            st.studentResponses.add(ex);
        });

        _course.getAsJsonArray("studentExamSheetR").forEach(gr -> {
            String left = gr.getAsJsonObject().get("left").getAsString();
            String right = gr.getAsJsonObject().get("right").getAsString();
            Student ex = Student.findById(uuidId.get(left));
            ExamSheet st = ExamSheet.findById(uuidId.get(right));
            ex.examSheets.add(st);
        });

        _course.getAsJsonArray("questionZoneR").forEach(gr -> {
            String left = gr.getAsJsonObject().get("left").getAsString();
            String right = gr.getAsJsonObject().get("right").getAsString();
            Question ex = Question.findById(uuidId.get(left));
            Zone st = Zone.findById(uuidId.get(right));
            ex.zone = st;
        });

        return course;

    }

    protected void putObject(String name, byte[] bytes, String contenttype)
            throws InvalidKeyException, NoSuchAlgorithmException, IllegalArgumentException, IOException {
        this.fichierS3Service.putObject(name, bytes, contenttype);
    }

    public CourseDTO importCourse(MultipartFormDataInput input) {
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

        List<String> fileNames = new ArrayList<>();
        List<InputPart> inputParts = uploadForm.get("file");
        String fileName = null;
        for (InputPart inputPart : inputParts) {
                MultivaluedMap<String, String> header = inputPart.getHeaders();
                fileName = getFileName(header);
                fileNames.add(fileName);
                InputStream inputStream;
                try {
                    inputStream = inputPart.getBody(InputStream.class, null);
                    JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
                    JsonElement jelement = JsonParser.parseReader(reader);
                    Course c  = this.importCourse(jelement.getAsJsonObject());
                    return courseMapper.toDto(c);
                    } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return null;
    }

    protected String getFileName(MultivaluedMap<String, String> header) {
        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                String[] name = filename.split("=");
                String finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }
        return "";
    }

}
