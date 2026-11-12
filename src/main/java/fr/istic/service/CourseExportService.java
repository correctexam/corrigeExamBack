package fr.istic.service;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fr.istic.domain.Answer2HybridGradedComment;
import fr.istic.domain.Comments;
import fr.istic.domain.Course;
import fr.istic.domain.CourseGroup;
import fr.istic.domain.Exam;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.FinalResult;
import fr.istic.domain.GradedComment;
import fr.istic.domain.HybridGradedComment;
import fr.istic.domain.Question;
import fr.istic.domain.Scan;
import fr.istic.domain.Student;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.Template;
import fr.istic.domain.TextComment;
import fr.istic.domain.Zone;

/**
 * Exports a course, or one of its exams, to the JSON format read by {@link CourseImportService}.
 * <p>
 * Split out of the former {@code ImportExportService} (T3).
 */
@ApplicationScoped
public class CourseExportService {

    private final Logger log = LoggerFactory.getLogger(CourseExportService.class);

    @ConfigProperty(name = "correctexam.uses3", defaultValue = "false")
    boolean uses3;

    @Inject
    FichierS3Service fichierS3Service;

    public JsonObject export(long courseId, boolean includeStudentData, long eid) {
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

        Map<Long, UUID> groupsUUID = new HashMap<>();
        Map<Long, UUID> studentResponsesUID = new HashMap<>();
        Map<Long, UUID> studentsUID = new HashMap<>();
        Map<Long, UUID> finalResultsUID = new HashMap<>();
        Map<Long, UUID> scansUID = new HashMap<>();

        Map<Long, UUID> commentsUID = new HashMap<>();
        Map<Long, UUID> examSheetsUID = new HashMap<>();

        if (includeStudentData) {

            JsonArray groups = new JsonArray();
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

            // Student
            JsonArray students = new JsonArray();
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
        }

        // Exam
        JsonArray exams = new JsonArray();
        Map<Long, UUID> examsUID = new HashMap<>();
        root.add("exams", exams);
        Set<Exam> _exams = course.exams;
        if (eid != -1){
           _exams= _exams.stream().filter(ex-> ex.id == eid).collect(Collectors.toSet());
        }

        _exams.stream().forEach(exam -> {
            JsonObject examJ = new JsonObject();
            UUID examU = UUID.randomUUID();
            uuidMap.put(examU, exam.id);
            examJ.addProperty("uuid", examU.toString());
            examJ.addProperty("name", exam.name);
            examJ.addProperty("nbgrader", exam.nbgrader);
            exams.add(examJ);
            examsUID.put(exam.id, examU);
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
                templateJ.addProperty("caseboxname", template.caseboxname);
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
                templateJ.addProperty("caseboxname", template.caseboxname);
                templates.add(templateJ);
                templatesUID.put(template.id, templateU);
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
                createZone(zone, zonesUID, zones, uuidMap);
            }
            if (exam.namezone != null) {
                Zone zone = Zone.findById(exam.namezone.id);
                createZone(zone, zonesUID, zones, uuidMap);
            }
            if (exam.firstnamezone != null) {
                Zone zone = Zone.findById(exam.firstnamezone.id);
                createZone(zone, zonesUID, zones, uuidMap);
            }
            if (exam.notezone != null) {
                Zone zone = Zone.findById(exam.notezone.id);
                createZone(zone, zonesUID, zones, uuidMap);
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
                questionJ.addProperty("defaultpoint", question.defaultpoint);
                questionJ.addProperty("libelle", question.libelle);
                questionJ.addProperty("gradeType", question.gradeType.name());
                questionJ.addProperty("type", question.type.algoName);
                questionJ.addProperty("randomHorizontalCorrection", question.randomHorizontalCorrection);
                questionJ.addProperty("canExceedTheMax", question.canExceedTheMax);
                questionJ.addProperty("canBeNegative", question.canBeNegative);
                questionJ.addProperty("mustBeIgnoreInGlobalScale", question.mustBeIgnoreInGlobalScale);

                if (question.zone != null) {

                    Zone zone = Zone.findById(question.zone.id);
                    createZone(zone, zonesUID, zones, uuidMap);
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

        // HybridComments

        JsonArray hybridcomments = new JsonArray();
        Map<Long, UUID> hybridcommentsUID = new HashMap<>();
        root.add("hybridcomments", hybridcomments);
        questionsUID.keySet().stream().forEach(questionId -> {
            Question question = Question.findById(questionId);
            question.hybridcomments.stream().forEach(t -> {
                HybridGradedComment hybridcomment = HybridGradedComment.findById(t.id);
                JsonObject hybridcommentJ = new JsonObject();
                UUID hybridcommentU = UUID.randomUUID();
                uuidMap.put(hybridcommentU, hybridcomment.id);
                hybridcommentJ.addProperty("uuid", hybridcommentU.toString());
                hybridcommentJ.addProperty("text", hybridcomment.text);
                hybridcommentJ.addProperty("description", hybridcomment.description);
                hybridcommentJ.addProperty("relative", hybridcomment.relative);
                hybridcommentJ.addProperty("grade", hybridcomment.grade);
                hybridcommentJ.addProperty("step", hybridcomment.step);
                hybridcomments.add(hybridcommentJ);
                hybridcommentsUID.put(hybridcomment.id, hybridcommentU);
            });
        });

        if (includeStudentData) {

            // Scans
            JsonArray scans = new JsonArray();
            root.add("scans", scans);
            examsUID.keySet().stream().forEach(examid -> {
                Exam exam = Exam.findById(examid);
                if (exam.scanfile != null) {
                    Scan scan = Scan.findById(exam.scanfile.id);
                    JsonObject scanJ = new JsonObject();
                    UUID scanU = UUID.randomUUID();
                    scansUID.put(scan.id, scanU);

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
                }
            });

            // ExamSheets

            JsonArray examSheets = new JsonArray();
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
            root.add("finalResults", finalResults);
            examsUID.keySet().stream().forEach(examId -> {
                List<FinalResult> finalResults_ = FinalResult.getAll4ExamId(examId).list();
                finalResults_.forEach(finalResult -> {
                    JsonObject finalResultJ = new JsonObject();
                    UUID finalResultU = UUID.randomUUID();
                    uuidMap.put(finalResultU, finalResult.id);
                    finalResultJ.addProperty("uuid", finalResultU.toString());
                    finalResultJ.addProperty("note", finalResult.note);
                    finalResultJ.addProperty("frozen", finalResult.frozen);
                    finalResults.add(finalResultJ);
                    finalResultsUID.put(finalResult.id, finalResultU);

                });
            });

        }
        if (includeStudentData) {

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
        }
        JsonArray courseExamR = new JsonArray();
        root.add("courseExamR", courseExamR);
        _exams.forEach(ex -> {
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

        JsonArray examsquestionsR = new JsonArray();
        root.add("examsquestionsR", examsquestionsR);
        examsUID.keySet().forEach(exid -> {
            Exam ex = Exam.findById(exid);
            ex.questions.forEach(q -> {
                JsonObject ob = new JsonObject();
                ob.addProperty("left", examsUID.get(exid).toString());
                ob.addProperty("right", questionsUID.get(q.id).toString());
                examsquestionsR.add(ob);

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
            } else {
                log.error("question without zone");
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
        JsonArray questionHybridCommentsR = new JsonArray();
        root.add("questionHybridCommentsR", questionHybridCommentsR);
        questionsUID.keySet().forEach(sid -> {
            Question s = Question.findById(sid);
            s.hybridcomments.forEach(hc -> {
                JsonObject ob = new JsonObject();
                ob.addProperty("left", questionsUID.get(sid).toString());
                ob.addProperty("right", hybridcommentsUID.get(hc.id).toString());
                questionHybridCommentsR.add(ob);
            });
        });

        if (includeStudentData) {

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

            JsonArray studentResponseHybridCommentsR = new JsonArray();
            root.add("studentResponseHybridCommentsR", studentResponseHybridCommentsR);
            studentResponsesUID.keySet().forEach(sid -> {
                List<Answer2HybridGradedComment> ans = Answer2HybridGradedComment.findAllAnswerHybridGradedCommentByAnswerId(sid).list();
//                StudentResponse s = StudentResponse.findById(sid);
                ans.forEach(an -> {
                    JsonObject ob = new JsonObject();
                    ob.addProperty("left", studentResponsesUID.get(an.studentResponse.id).toString());
                    ob.addProperty("right", hybridcommentsUID.get(an.hybridcomments.id).toString());
                    ob.addProperty("stepValue",an.stepValue);
                    studentResponseHybridCommentsR.add(ob);
                });
            });

            JsonArray studentExamSheetR = new JsonArray();
            root.add("studentExamSheetR", studentExamSheetR);
            studentsUID.keySet().forEach(sid -> {
                Student s = Student.findById(sid);
                s.examSheets.forEach(gc -> {
                    if (examSheetsUID.containsKey(gc.id)){
                        JsonObject ob = new JsonObject();
                        ob.addProperty("left", studentsUID.get(sid).toString());
                        ob.addProperty("right", examSheetsUID.get(gc.id).toString());
                        studentExamSheetR.add(ob);
                    }
                });
            });

            JsonArray sheetIdUidMappings = new JsonArray();
            root.add("sheetIdUidMappings", sheetIdUidMappings);
            examSheetsUID.entrySet().forEach(sid -> {
                JsonObject ob = new JsonObject();
                ob.addProperty("left", sid.getKey());
                ob.addProperty("right", sid.getValue().toString());
                sheetIdUidMappings.add(ob);
            });
        }

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

    private void createZone(Zone zone, Map<Long, UUID> zonesUID, JsonArray zones,
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
        zones.add(zoneJ);
        zonesUID.put(zone.id, zoneU);
    }
}
