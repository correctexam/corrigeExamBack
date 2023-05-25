package fr.istic.service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import fr.istic.domain.Comments;
import fr.istic.domain.Course;
import fr.istic.domain.CourseGroup;
import fr.istic.domain.Exam;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.FinalResult;
import fr.istic.domain.GradedComment;
import fr.istic.domain.Question;
import fr.istic.domain.Scan;
import fr.istic.domain.Student;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.Template;
import fr.istic.domain.TextComment;
import fr.istic.domain.Zone;

class Relation{
    public String left,right;

    public Relation(String left,String right){
        this.left= left;
        this.right= right;
    }
}

@ApplicationScoped
@Transactional
public class ImportExportService {


    private final Logger log = LoggerFactory.getLogger(ImportExportService.class);



    public ImportExportService() {

    }



    public JsonObject export(long courseId){
        Map<UUID, Long> uuidMap = new HashMap<UUID, Long>();
        JsonObject root = new JsonObject();


        // Course
        Course course = Course.findById(courseId);
        JsonObject courseJ = new JsonObject();
        UUID courseU = UUID.randomUUID();
        uuidMap.put(courseU, course.id);
        courseJ.addProperty("uuid",courseU.toString());
        courseJ.addProperty("name",course.name);
        root.add("course",courseJ);

        JsonArray groups = new JsonArray();
        Map<Long, UUID> groupsUUID =new HashMap<>();
        root.add("groups",groups);


        // CourseGroup
        course.groups.stream().forEach(group -> {
            JsonObject groupJ = new JsonObject();
            UUID groupU = UUID.randomUUID();
            uuidMap.put(groupU, group.id);
            groupJ.addProperty("uuid",groupU.toString());
            groupJ.addProperty("groupName",group.groupName);
            groups.add(groupJ);
            groupsUUID.put(group.id, groupU);
        });

        // Exam
        JsonArray exams = new JsonArray();
        Map<Long, UUID> examsUID =new HashMap<>();
        root.add("exams",exams);
        course.exams.stream().forEach(exam -> {
            JsonObject examJ = new JsonObject();
            UUID examU = UUID.randomUUID();
            uuidMap.put(examU, exam.id);
            examJ.addProperty("uuid",examU.toString());
            examJ.addProperty("name",exam.name);
            exams.add(examJ);
            examsUID.put(exam.id, examU);
        });


        // Student
        JsonArray students = new JsonArray();
        Map<Long, UUID> studentsUID =new HashMap<>();
        root.add("students",students);
        groupsUUID.keySet().stream().forEach( groupid -> {
            CourseGroup group = CourseGroup.findById(groupid);
            group.students.stream().forEach(st -> {
                Student student = Student.findById(st.id);
                JsonObject studentJ = new JsonObject();
                UUID studentU = UUID.randomUUID();
                uuidMap.put(studentU, student.id);
                studentJ.addProperty("uuid",studentU.toString());
                studentJ.addProperty("name",student.name);
                studentJ.addProperty("firstname",student.firstname);
                studentJ.addProperty("caslogin",student.caslogin);
                studentJ.addProperty("ine",student.ine);
                studentJ.addProperty("mail",student.mail);
                students.add(studentJ);
                studentsUID.put(student.id, studentU);
            });
        });



        // Templates
        JsonArray templates = new JsonArray();
        Map<Long, UUID> templatesUID =new HashMap<>();
        root.add("templates",templates);
        examsUID.keySet().stream().forEach( examid -> {
            Exam exam = Exam.findById(examid);
            if (exam.template!=null){

            Template template = Template.findById(exam.template.id);

                JsonObject templateJ = new JsonObject();
                UUID templateU = UUID.randomUUID();
                uuidMap.put(templateU, template.id);
                templateJ.addProperty("uuid",templateU.toString());
                templateJ.addProperty("name",template.name);
                templateJ.addProperty("autoMapStudentCopyToList",template.autoMapStudentCopyToList);
                templateJ.addProperty("contentContentType",template.contentContentType);
                // TODO
                templateJ.addProperty("content","");
                templateJ.addProperty("mark",template.mark);
                templates.add(templateJ);
                templatesUID.put(template.id, templateU);
            }

        });

        // Scans

        JsonArray scans = new JsonArray();
        Map<Long, UUID> scansUID =new HashMap<>();
        root.add("scans",scans);
        examsUID.keySet().stream().forEach( examid -> {
            Exam exam = Exam.findById(examid);
            if (exam.scanfile != null){
            Scan scan = Scan.findById(exam.scanfile.id);
                JsonObject scanJ = new JsonObject();
                UUID scanU = UUID.randomUUID();
                uuidMap.put(scanU, scan.id);
                scanJ.addProperty("uuid",scanU.toString());
                scanJ.addProperty("name",scan.name);
                // TODO
                scanJ.addProperty("content","");
                scanJ.addProperty("contentContentType",scan.contentContentType);
                scans.add(scanJ);
                scansUID.put(scan.id, scanU);
            }
        });

        // Zones

        JsonArray zones = new JsonArray();
        Map<Long, UUID> zonesUID =new HashMap<>();
        root.add("zones",zones);
        examsUID.keySet().stream().forEach( examid -> {
            Exam exam = Exam.findById(examid);
            if (exam.idzone != null){
                Zone zone = Zone.findById(exam.idzone.id);
                createZone(zone, zonesUID, zones, uuidMap);
            }
            if (exam.namezone != null){
                Zone zone = Zone.findById(exam.namezone.id);
            createZone(zone, zonesUID, zones, uuidMap);
            }
            if (exam.firstnamezone != null){
                Zone zone = Zone.findById(exam.firstnamezone.id);
            createZone(zone, zonesUID, zones, uuidMap);
            }
            if (exam.notezone != null){
                Zone zone = Zone.findById(exam.notezone.id);
            createZone(zone, zonesUID, zones, uuidMap);
            }
        });


        // Questions

        JsonArray questions = new JsonArray();
        Map<Long, UUID> questionsUID =new HashMap<>();
        root.add("questions",questions);
        examsUID.keySet().stream().forEach( examid -> {
            Exam exam = Exam.findById(examid);
            exam.questions.stream().forEach(q -> {
                Question question = Question.findById(q.id);
                JsonObject questionJ = new JsonObject();
                UUID questionU = UUID.randomUUID();
                uuidMap.put(questionU, question.id);
                questionJ.addProperty("uuid",questionU.toString());
                questionJ.addProperty("numero",question.numero);
                questionJ.addProperty("quarterpoint",question.quarterpoint);
                questionJ.addProperty("step",question.step);
                questionJ.addProperty("validExpression",question.validExpression);
                questionJ.addProperty("gradeType",question.gradeType.name());
                questionJ.addProperty("type",question.type.algoName);
                if (question.zone != null){

                    Zone zone = Zone.findById(question.zone.id);
                    createZone(zone, zonesUID, zones, uuidMap);
                    }
                questions.add(questionJ);
                questionsUID.put(question.id, questionU);
            });
        });

        // TextComments

        JsonArray textcomments = new JsonArray();
        Map<Long, UUID> textcommentsUID =new HashMap<>();
        root.add("textcomments",textcomments);
        questionsUID.keySet().stream().forEach( questionId -> {
            Question question = Question.findById(questionId);
            question.textcomments.stream().forEach(t -> {
                TextComment textcomment = TextComment.findById(t.id);
                JsonObject textcommentJ = new JsonObject();
                UUID textcommentU = UUID.randomUUID();
                uuidMap.put(textcommentU, textcomment.id);
                textcommentJ.addProperty("uuid",textcommentU.toString());
                textcommentJ.addProperty("text",textcomment.text);
                textcommentJ.addProperty("description",textcomment.description);
                textcommentJ.addProperty("zonegeneratedid",textcomment.zonegeneratedid);
                textcomments.add(textcommentJ);
                textcommentsUID.put(textcomment.id, textcommentU);
            });
        });

        // GradedComments

        JsonArray gradedcomments = new JsonArray();
        Map<Long, UUID> gradedcommentsUID =new HashMap<>();
        root.add("gradedcomments",gradedcomments);
        questionsUID.keySet().stream().forEach( questionId -> {
            Question question = Question.findById(questionId);
            question.gradedcomments.stream().forEach(t -> {
                GradedComment gradedcomment = GradedComment.findById(t.id);
                JsonObject gradedcommentJ = new JsonObject();
                UUID gradedcommentU = UUID.randomUUID();
                uuidMap.put(gradedcommentU, gradedcomment.id);
                gradedcommentJ.addProperty("uuid",gradedcommentU.toString());
                gradedcommentJ.addProperty("text",gradedcomment.text);
                gradedcommentJ.addProperty("description",gradedcomment.description);
                gradedcommentJ.addProperty("gradequarter",gradedcomment.gradequarter);
                gradedcommentJ.addProperty("zonegeneratedid",gradedcomment.zonegeneratedid);
                gradedcomments.add(gradedcommentJ);
                gradedcommentsUID.put(gradedcomment.id, gradedcommentU);
            });
        });


        // ExamSheets

        JsonArray examSheets = new JsonArray();
        Map<Long, UUID> examSheetsUID =new HashMap<>();
        root.add("examSheets",examSheets);
        scansUID.keySet().stream().forEach( scanid -> {
            List<ExamSheet> examSheets_ = ExamSheet.findExamSheetByScan(scanid).list();
            examSheets_.forEach(examSheet -> {
                JsonObject examSheetJ = new JsonObject();
                UUID examSheetU = UUID.randomUUID();
                uuidMap.put(examSheetU, examSheet.id);
                examSheetJ.addProperty("uuid",examSheetU.toString());
                examSheetJ.addProperty("name",examSheet.name);
                examSheetJ.addProperty("pagemax",examSheet.pagemax);
                examSheetJ.addProperty("pagemin",examSheet.pagemin);
                examSheets.add(examSheetJ);
                examSheetsUID.put(examSheet.id, examSheetU);

            });

        });


        // StudentResponse

        JsonArray studentResponses = new JsonArray();
        Map<Long, UUID> studentResponsesUID =new HashMap<>();
        root.add("studentResponses",studentResponses);
        examsUID.keySet().stream().forEach( examId -> {
            List<StudentResponse> studentResponses_ = StudentResponse.getAll4ExamId(examId).list();
            studentResponses_.forEach(studentResponse -> {
                JsonObject studentResponseJ = new JsonObject();
                UUID studentResponseU = UUID.randomUUID();
                uuidMap.put(studentResponseU, studentResponse.id);
                studentResponseJ.addProperty("uuid",studentResponseU.toString());
                studentResponseJ.addProperty("star",studentResponse.star);
                studentResponseJ.addProperty("worststar",studentResponse.worststar);
                studentResponseJ.addProperty("quarternote",studentResponse.quarternote);
                studentResponses.add(studentResponseJ);
                studentResponsesUID.put(studentResponse.id, studentResponseU);

            });
        });

        // Comment

        JsonArray comments = new JsonArray();
        Map<Long, UUID> commentsUID =new HashMap<>();
        root.add("comments",comments);
        examsUID.keySet().stream().forEach( examId -> {
            List<Comments> comments_ = Comments.findCommentByExamId(""+examId).list();
            comments_.forEach(comment -> {
                JsonObject commentJ = new JsonObject();
                UUID commentU = UUID.randomUUID();
                uuidMap.put(commentU, comment.id);
                commentJ.addProperty("uuid",commentU.toString());
                commentJ.addProperty("jsonData",comment.jsonData);
                // TODO
                /*
                 *           '' + this.exam!.id + '_' + this.selectionStudents![0].id + '_' + this.questionno + '_' + index

                 */
                commentJ.addProperty("zonegeneratedid",comment.zonegeneratedid);
                comments.add(commentJ);
                commentsUID.put(comment.id, commentU);

            });
        });


        // FinalResult

        JsonArray finalResults = new JsonArray();
        Map<Long, UUID> finalResultsUID =new HashMap<>();
        root.add("finalResults",finalResults);
        examsUID.keySet().stream().forEach( examId -> {
            List<FinalResult> finalResults_ = FinalResult.getAll4ExamId(examId).list();
            finalResults_.forEach(finalResult -> {
                JsonObject finalResultJ = new JsonObject();
                UUID finalResultU = UUID.randomUUID();
                uuidMap.put(finalResultU, finalResult.id);
                finalResultJ.addProperty("uuid",finalResultU.toString());
                finalResultJ.addProperty("note",finalResult.note);
                finalResults.add(finalResultJ);
                finalResultsUID.put(finalResult.id, finalResultU);

            });
        });



        JsonArray coursegroupsR = new JsonArray();
        root.add("coursegroupsR",coursegroupsR);
        course.groups.forEach(gr -> {
            JsonObject ob= new JsonObject();
            ob.addProperty("left",courseU.toString());
            ob.addProperty("right",groupsUUID.get(gr.id).toString());
            coursegroupsR.add(ob );
        });

        JsonArray groupsstudentR = new JsonArray();
        root.add("groupsstudentR",groupsstudentR);
        groupsUUID.keySet().forEach(groupId -> {
            CourseGroup gr = CourseGroup.findById(groupId);
            gr.students.forEach(st -> {
                JsonObject ob= new JsonObject();
                ob.addProperty("left",groupsUUID.get(gr.id).toString()  );
                ob.addProperty("right",studentsUID.get(st.id).toString());
                groupsstudentR.add(ob );
            });
        });


        JsonArray courseExamR = new JsonArray();
        root.add("courseExamR",courseExamR);
        course.exams.forEach(ex -> {
            JsonObject ob= new JsonObject();
            ob.addProperty("left",courseU.toString());
            ob.addProperty("right",examsUID.get(ex.id).toString());
            courseExamR.add(ob );
        });


        JsonArray examstemplatesR = new JsonArray();
        root.add("examstemplatesR",examstemplatesR);
        examsUID.keySet().forEach(exid -> {
            Exam ex = Exam.findById(exid);
                JsonObject ob= new JsonObject();
                ob.addProperty("left",examsUID.get(exid).toString()  );
                if (ex.template != null){
                    ob.addProperty("right",templatesUID.get(ex.template.id).toString());
                    examstemplatesR.add(ob );
                 }

        });

        JsonArray examsidzoneR = new JsonArray();
        root.add("examsidzoneR",examsidzoneR);
        examsUID.keySet().forEach(exid -> {
            Exam ex = Exam.findById(exid);
                JsonObject ob= new JsonObject();
                ob.addProperty("left",examsUID.get(exid).toString()  );
                if (ex.idzone != null){
                    ob.addProperty("right",zonesUID.get(ex.idzone.id).toString());
                    examsidzoneR.add(ob );
             }
                       });

        JsonArray examsnamezoneR = new JsonArray();
        root.add("examsnamezoneR",examsnamezoneR);
        examsUID.keySet().forEach(exid -> {
            Exam ex = Exam.findById(exid);
                JsonObject ob= new JsonObject();
                ob.addProperty("left",examsUID.get(exid).toString()  );

                if (ex.namezone != null){
                    ob.addProperty("right",zonesUID.get(ex.namezone.id).toString());
                    examsnamezoneR.add(ob );
                        }
        });

        JsonArray examsfirstnamezoneR = new JsonArray();
        root.add("examsfirstnamezoneR",examsfirstnamezoneR);
        examsUID.keySet().forEach(exid -> {
            Exam ex = Exam.findById(exid);
                JsonObject ob= new JsonObject();
                ob.addProperty("left",examsUID.get(exid).toString()  );
                if (ex.firstnamezone != null){
                    ob.addProperty("right",zonesUID.get(ex.firstnamezone.id).toString());
                    examsfirstnamezoneR.add(ob );
                    }
        });

        JsonArray examsnotezoneR = new JsonArray();
        root.add("examsnotezoneR",examsnotezoneR);
        examsUID.keySet().forEach(exid -> {
            Exam ex = Exam.findById(exid);
                JsonObject ob= new JsonObject();
                ob.addProperty("left",examsUID.get(exid).toString()  );
                if (ex.notezone != null){
                    ob.addProperty("right",zonesUID.get(ex.notezone.id).toString());
                    examsnotezoneR.add(ob );
                }
        });

        JsonArray examsscanfileR = new JsonArray();
        root.add("examsscanfileR",examsscanfileR);
        examsUID.keySet().forEach(exid -> {
            Exam ex = Exam.findById(exid);
                JsonObject ob= new JsonObject();
                ob.addProperty("left",examsUID.get(exid).toString()  );

                if (ex.scanfile != null){
                    ob.addProperty("right",scansUID.get(ex.scanfile.id).toString());
                    examsscanfileR.add(ob );
                    }
        });

        JsonArray examsquestionsR = new JsonArray();
        root.add("examsquestionsR",examsquestionsR);
        examsUID.keySet().forEach(exid -> {
            Exam ex = Exam.findById(exid);
            ex.questions.forEach(q-> {
                JsonObject ob= new JsonObject();
                ob.addProperty("left",examsUID.get(exid).toString()  );
                ob.addProperty("right",questionsUID.get(q.id).toString());
                examsscanfileR.add(ob );

            });
        });

        JsonArray finalResultStudentR = new JsonArray();
        root.add("finalResultStudentR",finalResultStudentR);
        finalResultsUID.keySet().forEach(frid -> {
            FinalResult fr = FinalResult.findById(frid);
                JsonObject ob= new JsonObject();
                ob.addProperty("left",finalResultsUID.get(frid).toString()  );
                if (fr.student != null && studentsUID.get(fr.student.id)!=null)  {
                    log.error(""+fr.student.id);
                ob.addProperty("right",studentsUID.get(fr.student.id).toString());
                finalResultStudentR.add(ob );
                }
        });


        JsonArray finalResultExamR = new JsonArray();
        root.add("finalResultExamR",finalResultExamR);
        finalResultsUID.keySet().forEach(frid -> {
            FinalResult fr = FinalResult.findById(frid);
                JsonObject ob= new JsonObject();
                ob.addProperty("left",finalResultsUID.get(frid).toString()  );
                if (fr.exam!= null){

                ob.addProperty("right",examsUID.get(fr.exam.id).toString());
                finalResultStudentR.add(ob );
            }

        });


        JsonArray studentResponsesQuestionR = new JsonArray();
        root.add("studentResponsesQuestionR",studentResponsesQuestionR);
        studentResponsesUID.keySet().forEach(srid -> {
            StudentResponse sr = StudentResponse.findById(srid);
                JsonObject ob= new JsonObject();
                ob.addProperty("left",studentResponsesUID.get(srid).toString()  );
                if (sr.question != null){
                    ob.addProperty("right",questionsUID.get(sr.question.id).toString());
                    studentResponsesQuestionR.add(ob );

                }
        });

        JsonArray studentResponsesExamSheetR = new JsonArray();
        root.add("studentResponsesExamSheetR",studentResponsesExamSheetR);
        studentResponsesUID.keySet().forEach(srid -> {
            StudentResponse sr = StudentResponse.findById(srid);
                JsonObject ob= new JsonObject();
                ob.addProperty("left",studentResponsesUID.get(srid).toString()  );
                if (sr.sheet != null){
                    ob.addProperty("right",examSheetsUID.get(sr.sheet.id).toString());
                studentResponsesExamSheetR.add(ob );
                }
        });

        JsonArray studentResponsesCommentsR = new JsonArray();
        root.add("studentResponsesCommentsR",studentResponsesCommentsR);
        studentResponsesUID.keySet().forEach(srid -> {
            StudentResponse sr = StudentResponse.findById(srid);
            sr.comments.forEach(comment -> {
                JsonObject ob= new JsonObject();
                ob.addProperty("left",studentResponsesUID.get(srid).toString()  );
                ob.addProperty("right",commentsUID.get(comment.id).toString());
                studentResponsesCommentsR.add(ob );
            });
        });

        JsonArray scanExamSheetsR = new JsonArray();
        root.add("scanExamSheetsR",scanExamSheetsR);
        scansUID.keySet().forEach(sid -> {
            Scan s = Scan.findById(sid);
            s.sheets.forEach(sheet -> {
                JsonObject ob= new JsonObject();
                ob.addProperty("left",scansUID.get(sid).toString()  );
                ob.addProperty("right",examSheetsUID.get(sheet.id).toString());
                scanExamSheetsR.add(ob );
            });
        });

        JsonArray questionTextCommentsR = new JsonArray();
        root.add("questionTextCommentsR",questionTextCommentsR);
        questionsUID.keySet().forEach(sid -> {
            Question s = Question.findById(sid);
            s.textcomments.forEach(tc -> {
                JsonObject ob= new JsonObject();
                ob.addProperty("left",questionsUID.get(sid).toString()  );
                ob.addProperty("right",textcommentsUID.get(tc.id).toString());
                questionTextCommentsR.add(ob );
            });
        });

        JsonArray questionGradedCommentsR = new JsonArray();
        root.add("questionGradedCommentsR",questionGradedCommentsR);
        questionsUID.keySet().forEach(sid -> {
            Question s = Question.findById(sid);
            s.gradedcomments.forEach(gc -> {
                JsonObject ob= new JsonObject();
                ob.addProperty("left",questionsUID.get(sid).toString()  );
                ob.addProperty("right",gradedcommentsUID.get(gc.id).toString());
                questionGradedCommentsR.add(ob );
            });
        });

        JsonArray studentResponseTextCommentsR = new JsonArray();
        root.add("studentResponseTextCommentsR",studentResponseTextCommentsR);
        studentResponsesUID.keySet().forEach(sid -> {
            StudentResponse s = StudentResponse.findById(sid);
            s.textcomments.forEach(tc -> {
                JsonObject ob= new JsonObject();
                ob.addProperty("left",studentResponsesUID.get(sid).toString()  );
                ob.addProperty("right",textcommentsUID.get(tc.id).toString());
                studentResponseTextCommentsR.add(ob );
            });
        });

        JsonArray studentResponseGradedCommentsR = new JsonArray();
        root.add("studentResponseGradedCommentsR",studentResponseGradedCommentsR);
        studentResponsesUID.keySet().forEach(sid -> {
            StudentResponse s = StudentResponse.findById(sid);
            s.gradedcomments.forEach(gc -> {
                JsonObject ob= new JsonObject();
                ob.addProperty("left",studentResponsesUID.get(sid).toString()  );
                ob.addProperty("right",gradedcommentsUID.get(gc.id).toString());
                studentResponseGradedCommentsR.add(ob );
            });
        });


        JsonArray studentExamSheetR = new JsonArray();
        root.add("studentExamSheetR",studentExamSheetR);
        studentsUID.keySet().forEach(sid -> {
            Student s = Student.findById(sid);
            s.examSheets.forEach(gc -> {
                JsonObject ob= new JsonObject();
                ob.addProperty("left",studentsUID.get(sid).toString()  );
                ob.addProperty("right",examSheetsUID.get(gc.id).toString());
                studentExamSheetR.add(ob );
            });
        });

        return root;


    }

    private void createZone(Zone zone, Map<Long, UUID> zonesUID,JsonArray zones,Map<UUID, Long> uuidMap){
        JsonObject zoneJ = new JsonObject();
        UUID zoneU = UUID.randomUUID();
        uuidMap.put(zoneU, zone.id);
        zoneJ.addProperty("uuid",zoneU.toString());
        zoneJ.addProperty("height",zone.height);
        zoneJ.addProperty("width",zone.width);
        zoneJ.addProperty("xInit",zone.xInit);
        zoneJ.addProperty("yInit",zone.yInit);
        zoneJ.addProperty("pageNumber",zone.pageNumber);
        zones.add(zoneJ);
        zonesUID.put(zone.id, zoneU);
    }
}
