package fr.istic.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

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
import fr.istic.domain.QuestionType;
import fr.istic.domain.Scan;
import fr.istic.domain.Student;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.Template;
import fr.istic.domain.TextComment;
import fr.istic.domain.User;
import fr.istic.domain.Zone;
import fr.istic.domain.enumeration.GradeType;
import fr.istic.service.dto.CourseDTO;
import fr.istic.service.mapper.CourseMapper;

/**
 * Imports a course from the JSON format written by {@link CourseExportService}.
 * <p>
 * Split out of the former {@code ImportExportService} (T3).
 */
@ApplicationScoped
public class CourseImportService {

    private final Logger log = LoggerFactory.getLogger(CourseImportService.class);

    @ConfigProperty(name = "correctexam.uses3", defaultValue = "false")
    boolean uses3;

    @Inject
    FichierS3Service fichierS3Service;

    @Inject
    CourseMapper courseMapper;

    public Course importCourse(JsonObject _course, User user, boolean includeStudentData) {
        Map<Long, String> importexamsUID = new HashMap<>();
        //Map<Long, String> _importstudentsUID = new HashMap<>();
        Map<Long, String> importsheetUID = new HashMap<>();
        Map<String, Long> uuidId = new HashMap<>();

        _course.getAsJsonArray("examIdUidMappings").forEach(st -> {
            importexamsUID.put(st.getAsJsonObject().get("left").getAsLong(),
                    st.getAsJsonObject().get("right").getAsString());
        });

        if (includeStudentData) {
           /* if (_course.getAsJsonArray("studentIdUidMappings") != null) {

                _course.getAsJsonArray("studentIdUidMappings").forEach(st -> {
                    importstudentsUID.put(st.getAsJsonObject().get("left").getAsLong(),
                            st.getAsJsonObject().get("right").getAsString());
                });
            }*/
            if (_course.getAsJsonArray("sheetIdUidMappings") != null) {

                _course.getAsJsonArray("sheetIdUidMappings").forEach(st -> {
                    importsheetUID.put(st.getAsJsonObject().get("left").getAsLong(),
                            st.getAsJsonObject().get("right").getAsString());
                });
            }

        }

        // Course
        Course course = new Course();
        JsonObject courseJ = _course.getAsJsonObject("course");
        course.name = courseJ.get("name").getAsString();
        course.persistAndFlush();
        course.profs.add(user);

        if (includeStudentData) {
            if (_course.getAsJsonArray("groups") != null) {
                _course.getAsJsonArray("groups").forEach(gr -> {
                    CourseGroup group = new CourseGroup();
                    group.groupName = gr.getAsJsonObject().get("groupName").getAsString();
                    group.persistAndFlush();
                    uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), group.id);
                });
            }
        }

        if (_course.getAsJsonArray("exams") != null) {
            _course.getAsJsonArray("exams").forEach(gr -> {
                Exam exam = new Exam();
                exam.name = gr.getAsJsonObject().get("name").getAsString();
                if (gr.getAsJsonObject().get("nbgrader") != null){
                    exam.nbgrader = gr.getAsJsonObject().get("nbgrader").getAsBoolean();
                } else {
                    exam.nbgrader = false;
                }
                exam.persistAndFlush();
                uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), exam.id);
            });
        }

        if (includeStudentData) {
            if (_course.getAsJsonArray("students") != null) {

                _course.getAsJsonArray("students").forEach(gr -> {
                    Student student = new Student();
                    if (gr.getAsJsonObject().get("name") != null) {
                        student.name = gr.getAsJsonObject().get("name").getAsString();
                    }
                    if (gr.getAsJsonObject().get("firstname") != null) {
                        student.firstname = gr.getAsJsonObject().get("firstname").getAsString();
                    }
                    if (gr.getAsJsonObject().get("caslogin") != null) {
                        student.caslogin = gr.getAsJsonObject().get("caslogin").getAsString();
                    }
                    if (gr.getAsJsonObject().get("ine") != null) {
                        student.ine = gr.getAsJsonObject().get("ine").getAsString();
                    }
                    if (gr.getAsJsonObject().get("mail") != null) {
                        student.mail = gr.getAsJsonObject().get("mail").getAsString();
                    }

                    student.persistAndFlush();
                    uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), student.id);
                });
            }
        }

        if (_course.getAsJsonArray("templates") != null) {

            _course.getAsJsonArray("templates").forEach(gr -> {
                Template template = new Template();
                if (gr.getAsJsonObject().get("name") != null) {
                    template.name = gr.getAsJsonObject().get("name").getAsString();
                }
                if (gr.getAsJsonObject().get("autoMapStudentCopyToList") != null) {
                    template.autoMapStudentCopyToList = gr.getAsJsonObject().get("autoMapStudentCopyToList")
                            .getAsBoolean();
                }
                if (gr.getAsJsonObject().get("contentContentType") != null) {
                    template.contentContentType = gr.getAsJsonObject().get("contentContentType").getAsString();
                }
                if (gr.getAsJsonObject().get("caseboxname") != null) {
                    template.caseboxname = gr.getAsJsonObject().get("caseboxname").getAsBoolean();
                } else {
                    template.caseboxname = true;
                }

                if (gr.getAsJsonObject().get("mark") != null) {
                    template.mark = gr.getAsJsonObject().get("mark").getAsBoolean();
                }
                if (gr.getAsJsonObject().get("caseboxname") != null) {
                    template.caseboxname = gr.getAsJsonObject().get("caseboxname").getAsBoolean();
                }
                template.persistAndFlush();

                if (gr.getAsJsonObject().get("content") != null) {

                    byte[] bytes = gr.getAsJsonObject().get("content").getAsString().getBytes();

                    Base64.Decoder decoder = Base64.getDecoder();
                    byte[] b64bytes = decoder.decode(bytes);

                    if (this.uses3) {
                        String fileName = "template/" + template.id + ".pdf";
                        try {

                            this.putObject(fileName, b64bytes, template.contentContentType);

                        } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException
                                | IOException e) {
                                                log.error(e.getMessage());

                            e.printStackTrace();
                        }
                    } else {
                        template.content = b64bytes;
                        template.persistAndFlush();
                    }
                }

                uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), template.id);
            });
        }

        if (includeStudentData) {

            if (_course.getAsJsonArray("scans") != null) {

                _course.getAsJsonArray("scans").forEach(gr -> {
                    Scan scan = new Scan();

                    if (gr.getAsJsonObject().get("name") != null) {
                        scan.name = gr.getAsJsonObject().get("name").getAsString();
                    }
                    if (gr.getAsJsonObject().get("contentContentType") != null) {
                        scan.contentContentType = gr.getAsJsonObject().get("contentContentType").getAsString();
                    }
                    scan.persistAndFlush();
                    uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), scan.id);

                    if (gr.getAsJsonObject().get("content") != null) {

                        byte[] bytes = gr.getAsJsonObject().get("content").getAsString().getBytes();

                        Base64.Decoder decoder = Base64.getDecoder();
                        byte[] b64bytes = decoder.decode(bytes);

                        if (this.uses3) {
                            String fileName = "scan/" + scan.id + ".pdf";
                            try {
                                this.putObject(fileName, b64bytes, scan.contentContentType);
                            } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException
                                    | IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            scan.content = b64bytes;
                            scan.persistAndFlush();
                        }
                    }
                });
            }
        }

        if (_course.getAsJsonArray("zones") != null) {

            _course.getAsJsonArray("zones").forEach(gr -> {
                Zone zone = new Zone();
                if (gr.getAsJsonObject().get("height") != null) {
                    zone.height = gr.getAsJsonObject().get("height").getAsInt();

                }
                if (gr.getAsJsonObject().get("width") != null) {
                    zone.width = gr.getAsJsonObject().get("width").getAsInt();
                }
                if (gr.getAsJsonObject().get("xInit") != null) {
                    zone.xInit = gr.getAsJsonObject().get("xInit").getAsInt();
                }
                if (gr.getAsJsonObject().get("yInit") != null) {
                    zone.yInit = gr.getAsJsonObject().get("yInit").getAsInt();
                }
                if (gr.getAsJsonObject().get("pageNumber") != null) {
                    zone.pageNumber = gr.getAsJsonObject().get("pageNumber").getAsInt();
                }
                zone.persistAndFlush();
                uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), zone.id);
            });
        }

        // Questions
        if (_course.getAsJsonArray("questions") != null) {

            _course.getAsJsonArray("questions").forEach(gr -> {
                Question question = new Question();
                if (gr.getAsJsonObject().get("numero") != null) {
                    question.numero = gr.getAsJsonObject().get("numero").getAsInt();
                }
                if (gr.getAsJsonObject().get("quarterpoint") != null) {
                    question.quarterpoint = gr.getAsJsonObject().get("quarterpoint").getAsInt();
                }
                if (gr.getAsJsonObject().get("step") != null) {
                    question.step = gr.getAsJsonObject().get("step").getAsInt();
                }
                if (gr.getAsJsonObject().get("validExpression") != null) {
                    question.validExpression = gr.getAsJsonObject().get("validExpression").getAsString();
                }
                if (gr.getAsJsonObject().get("libelle") != null) {
                    question.libelle = gr.getAsJsonObject().get("libelle").getAsString();
                }
                if (gr.getAsJsonObject().get("defaultpoint") != null) {
                    question.defaultpoint = gr.getAsJsonObject().get("defaultpoint").getAsInt();
                }
                if (gr.getAsJsonObject().get("gradeType") != null) {
                    question.gradeType = GradeType.valueOf(gr.getAsJsonObject().get("gradeType").getAsString());
                }
                if (gr.getAsJsonObject().get("type") != null) {
                    question.type = QuestionType
                            .findQuestionTypebyAlgoName(gr.getAsJsonObject().get("type").getAsString())
                            .firstResult();
                }
                if (gr.getAsJsonObject().get("randomHorizontalCorrection") != null) {
                    question.randomHorizontalCorrection = gr.getAsJsonObject().get("randomHorizontalCorrection").getAsBoolean();
                } else {
                    question.randomHorizontalCorrection =false;
                }
                if (gr.getAsJsonObject().get("canExceedTheMax") != null) {
                    question.canExceedTheMax = gr.getAsJsonObject().get("canExceedTheMax").getAsBoolean();
                } else {
                    question.canExceedTheMax =false;
                }
                if (gr.getAsJsonObject().get("canBeNegative") != null) {
                    question.canBeNegative = gr.getAsJsonObject().get("canBeNegative").getAsBoolean();
                } else {
                    question.canBeNegative =false;
                }
                if (gr.getAsJsonObject().get("mustBeIgnoreInGlobalScale") != null) {
                    question.mustBeIgnoreInGlobalScale = gr.getAsJsonObject().get("mustBeIgnoreInGlobalScale").getAsBoolean();
                } else {
                    question.mustBeIgnoreInGlobalScale =false;
                }

                question.persistAndFlush();
                uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), question.id);
            });
        }

        // TextComments
        if (_course.getAsJsonArray("textcomments") != null) {

            _course.getAsJsonArray("textcomments").forEach(gr -> {
                TextComment textComment = new TextComment();
                if (gr.getAsJsonObject().get("text") != null) {
                    textComment.text = gr.getAsJsonObject().get("text").getAsString();
                }
                if (gr.getAsJsonObject().get("description") != null) {
                    textComment.description = gr.getAsJsonObject().get("description").getAsString();
                }
                if (gr.getAsJsonObject().get("zonegeneratedid") != null) {
                    textComment.zonegeneratedid = gr.getAsJsonObject().get("zonegeneratedid").getAsString();
                }
                textComment.persistAndFlush();
                uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), textComment.id);
            });
        }

        // GradedComments
        if (_course.getAsJsonArray("gradedcomments") != null) {

            _course.getAsJsonArray("gradedcomments").forEach(gr -> {
                GradedComment gradedComment = new GradedComment();
                if (gr.getAsJsonObject().get("text") != null) {
                    gradedComment.text = gr.getAsJsonObject().get("text").getAsString();
                }
                if (gr.getAsJsonObject().get("description") != null) {
                    gradedComment.description = gr.getAsJsonObject().get("description").getAsString();
                }
                if (gr.getAsJsonObject().get("zonegeneratedid") != null) {
                    gradedComment.zonegeneratedid = gr.getAsJsonObject().get("zonegeneratedid").getAsString();
                }
                if (gr.getAsJsonObject().get("gradequarter") != null) {
                    gradedComment.gradequarter = gr.getAsJsonObject().get("gradequarter").getAsInt();
                }
                gradedComment.persistAndFlush();
                uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), gradedComment.id);
            });
        }

        // HybridComments
        if (_course.getAsJsonArray("hybridcomments") != null) {

            _course.getAsJsonArray("hybridcomments").forEach(gr -> {
                HybridGradedComment hybridComment = new HybridGradedComment();
                if (gr.getAsJsonObject().get("text") != null) {
                    hybridComment.text = gr.getAsJsonObject().get("text").getAsString();
                }
                if (gr.getAsJsonObject().get("description") != null) {
                    hybridComment.description = gr.getAsJsonObject().get("description").getAsString();
                }
                if (gr.getAsJsonObject().get("grade") != null) {
                    hybridComment.grade = gr.getAsJsonObject().get("grade").getAsInt();
                }
                if (gr.getAsJsonObject().get("step") != null) {
                    hybridComment.step = gr.getAsJsonObject().get("step").getAsInt();
                }
                if (gr.getAsJsonObject().get("relative") != null) {
                    hybridComment.relative = gr.getAsJsonObject().get("relative").getAsBoolean();
                }
                hybridComment.persistAndFlush();
                uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), hybridComment.id);
            });
        }

        if (includeStudentData) {

            // ExamSheets
            if (_course.getAsJsonArray("examSheets") != null) {

                _course.getAsJsonArray("examSheets").forEach(gr -> {
                    ExamSheet examSheet = new ExamSheet();
                    if (gr.getAsJsonObject().get("name") != null) {
                        examSheet.name = gr.getAsJsonObject().get("name").getAsString();
                    }
                    if (gr.getAsJsonObject().get("pagemax") != null) {
                        examSheet.pagemax = gr.getAsJsonObject().get("pagemax").getAsInt();
                    }
                    if (gr.getAsJsonObject().get("pagemin") != null) {
                        examSheet.pagemin = gr.getAsJsonObject().get("pagemin").getAsInt();
                    }
                    examSheet.persistAndFlush();
                    uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), examSheet.id);
                });
            }

            // StudentResponse

            if (_course.getAsJsonArray("studentResponses") != null) {

                _course.getAsJsonArray("studentResponses").forEach(gr -> {
                    StudentResponse studentResponse = new StudentResponse();
                    if (gr.getAsJsonObject().get("star") != null) {
                        studentResponse.star = gr.getAsJsonObject().get("star").getAsBoolean();
                    }
                    if (gr.getAsJsonObject().get("worststar") != null) {
                        studentResponse.worststar = gr.getAsJsonObject().get("worststar").getAsBoolean();
                    }
                    if (gr.getAsJsonObject().get("manuallyGraded") != null
                            && !gr.getAsJsonObject().get("manuallyGraded").isJsonNull()) {
                        studentResponse.manuallyGraded = gr.getAsJsonObject().get("manuallyGraded").getAsBoolean();
                    }
                    if (gr.getAsJsonObject().get("quarternote") != null) {
                        studentResponse.quarternote = gr.getAsJsonObject().get("quarternote").getAsInt();
                    }
                    studentResponse.persistAndFlush();
                    uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), studentResponse.id);
                });
            }
            if (_course.getAsJsonArray("finalResults") != null) {

                // FinalResult
                _course.getAsJsonArray("finalResults").forEach(gr -> {
                    FinalResult finalResult = new FinalResult();
                    if (gr.getAsJsonObject().get("note") != null) {
                        finalResult.note = gr.getAsJsonObject().get("note").getAsInt();
                    }
                    if (gr.getAsJsonObject().get("frozen") != null) {
                        finalResult.frozen = gr.getAsJsonObject().get("frozen").getAsBoolean();
                    }
                    finalResult.persistAndFlush();
                    uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), finalResult.id);
                });
            }

            if (_course.getAsJsonArray("comments") != null) {

                // Comment
                _course.getAsJsonArray("comments").forEach(gr -> {
                    Comments comment = new Comments();
                    comment.jsonData = gr.getAsJsonObject().get("jsonData").getAsString();
                    comment.zonegeneratedid = gr.getAsJsonObject().get("zonegeneratedid").getAsString();
                    // TODO
                    StringTokenizer t = new StringTokenizer(comment.zonegeneratedid, "_");
                    if (t.countTokens() > 1) {
                        String examId = t.nextToken();
                        String sheetId = t.nextToken();
                        String questiono = t.nextToken();
                        String index = t.nextToken();
                        /*
                         * '' + this.exam!.id + '_' + this.selectionStudents![0].id + '_' +
                         * this.questionno + '_' + index
                         *
                         */
                        comment.zonegeneratedid = "" + uuidId.get(importexamsUID.get(Long.parseLong(examId))) + "_" +
                                uuidId.get(importsheetUID.get(Long.parseLong(sheetId))) + "_" + questiono + "_"
                                + index;
                    }

                    comment.persistAndFlush();
                    uuidId.put(gr.getAsJsonObject().get("uuid").getAsString(), comment.id);
                });
            }

            if (_course.getAsJsonArray("coursegroupsR") != null) {

                _course.getAsJsonArray("coursegroupsR").forEach(gr -> {
                    // String left = gr.getAsJsonObject().get("left").getAsString();
                    String right = gr.getAsJsonObject().get("right").getAsString();
                    Course c = Course.findById(course.id);
                    CourseGroup cg = CourseGroup.findById(uuidId.get(right));
                    cg.course = c;
                    c.groups.add(cg);
                });
            }

            if (_course.getAsJsonArray("groupsstudentR") != null) {

                _course.getAsJsonArray("groupsstudentR").forEach(gr -> {
                    String left = gr.getAsJsonObject().get("left").getAsString();
                    String right = gr.getAsJsonObject().get("right").getAsString();
                    CourseGroup cg = CourseGroup.findById(uuidId.get(left));
                    Student st = Student.findById(uuidId.get(right));
                    cg.students.add(st);
                    st.groups.add(cg);
                });
            }
        }

        if (_course.getAsJsonArray("courseExamR") != null) {

            _course.getAsJsonArray("courseExamR").forEach(gr -> {
                // String left = gr.getAsJsonObject().get("left").getAsString();
                String right = gr.getAsJsonObject().get("right").getAsString();
                Course c = Course.findById(course.id);
                Exam ex = Exam.findById(uuidId.get(right));
                c.exams.add(ex);
                ex.course = c;
            });
        }
        if (_course.getAsJsonArray("examstemplatesR") != null) {

            _course.getAsJsonArray("examstemplatesR").forEach(gr -> {
                String left = gr.getAsJsonObject().get("left").getAsString();
                String right = gr.getAsJsonObject().get("right").getAsString();
                Exam ex = Exam.findById(uuidId.get(left));
                Template st = Template.findById(uuidId.get(right));
                ex.template = st;
                // st.exam = ex;
            });
        }

        if (_course.getAsJsonArray("examsidzoneR") != null) {

            _course.getAsJsonArray("examsidzoneR").forEach(gr -> {
                String left = gr.getAsJsonObject().get("left").getAsString();
                String right = gr.getAsJsonObject().get("right").getAsString();
                Exam ex = Exam.findById(uuidId.get(left));
                Zone st = Zone.findById(uuidId.get(right));
                ex.idzone = st;
            });
        }
        if (_course.getAsJsonArray("examsnamezoneR") != null) {

            _course.getAsJsonArray("examsnamezoneR").forEach(gr -> {
                String left = gr.getAsJsonObject().get("left").getAsString();
                String right = gr.getAsJsonObject().get("right").getAsString();
                Exam ex = Exam.findById(uuidId.get(left));
                Zone st = Zone.findById(uuidId.get(right));
                ex.namezone = st;
            });
        }
        if (_course.getAsJsonArray("examsfirstnamezoneR") != null) {

            _course.getAsJsonArray("examsfirstnamezoneR").forEach(gr -> {
                String left = gr.getAsJsonObject().get("left").getAsString();
                String right = gr.getAsJsonObject().get("right").getAsString();
                Exam ex = Exam.findById(uuidId.get(left));
                Zone st = Zone.findById(uuidId.get(right));
                ex.firstnamezone = st;
            });
        }

        if (_course.getAsJsonArray("examsnotezoneR") != null) {

            _course.getAsJsonArray("examsnotezoneR").forEach(gr -> {
                String left = gr.getAsJsonObject().get("left").getAsString();
                String right = gr.getAsJsonObject().get("right").getAsString();
                Exam ex = Exam.findById(uuidId.get(left));
                Zone st = Zone.findById(uuidId.get(right));
                ex.notezone = st;
            });
        }

        if (_course.getAsJsonArray("examsquestionsR") != null) {

            _course.getAsJsonArray("examsquestionsR").forEach(gr -> {
                String left = gr.getAsJsonObject().get("left").getAsString();
                String right = gr.getAsJsonObject().get("right").getAsString();
                Exam ex = Exam.findById(uuidId.get(left));
                Question st = Question.findById(uuidId.get(right));
                ex.questions.add(st);
                st.exam = ex;
            });
        }

        if (includeStudentData) {

            if (_course.getAsJsonArray("examsscanfileR") != null) {

                _course.getAsJsonArray("examsscanfileR").forEach(gr -> {
                    String left = gr.getAsJsonObject().get("left").getAsString();
                    String right = gr.getAsJsonObject().get("right").getAsString();
                    Exam ex = Exam.findById(uuidId.get(left));
                    Scan st = Scan.findById(uuidId.get(right));
                    ex.scanfile = st;

                });
            }

            if (_course.getAsJsonArray("finalResultStudentR") != null) {

                _course.getAsJsonArray("finalResultStudentR").forEach(gr -> {
                    String left = gr.getAsJsonObject().get("left").getAsString();
                    String right = gr.getAsJsonObject().get("right").getAsString();
                    FinalResult ex = FinalResult.findById(uuidId.get(left));
                    Student st = Student.findById(uuidId.get(right));
                    ex.student = st;
                });
            }

            if (_course.getAsJsonArray("finalResultExamR") != null) {

                _course.getAsJsonArray("finalResultExamR").forEach(gr -> {
                    String left = gr.getAsJsonObject().get("left").getAsString();
                    String right = gr.getAsJsonObject().get("right").getAsString();
                    FinalResult ex = FinalResult.findById(uuidId.get(left));
                    Exam st = Exam.findById(uuidId.get(right));
                    ex.exam = st;
                });
            }

            if (_course.getAsJsonArray("studentResponsesQuestionR") != null) {

                _course.getAsJsonArray("studentResponsesQuestionR").forEach(gr -> {
                    String left = gr.getAsJsonObject().get("left").getAsString();
                    String right = gr.getAsJsonObject().get("right").getAsString();
                    StudentResponse ex = StudentResponse.findById(uuidId.get(left));
                    Question st = Question.findById(uuidId.get(right));
                    ex.question = st;
                });
            }

            if (_course.getAsJsonArray("studentResponsesExamSheetR") != null) {

                _course.getAsJsonArray("studentResponsesExamSheetR").forEach(gr -> {
                    String left = gr.getAsJsonObject().get("left").getAsString();
                    String right = gr.getAsJsonObject().get("right").getAsString();
                    StudentResponse ex = StudentResponse.findById(uuidId.get(left));
                    ExamSheet st = ExamSheet.findById(uuidId.get(right));
                    ex.sheet = st;
                });
            }

            if (_course.getAsJsonArray("studentResponsesCommentsR") != null) {

                _course.getAsJsonArray("studentResponsesCommentsR").forEach(gr -> {
                    String left = gr.getAsJsonObject().get("left").getAsString();
                    String right = gr.getAsJsonObject().get("right").getAsString();
                    StudentResponse ex = StudentResponse.findById(uuidId.get(left));
                    Comments st = Comments.findById(uuidId.get(right));
                    ex.comments.add(st);
                    st.studentResponse = ex;
                });
            }
            if (_course.getAsJsonArray("scanExamSheetsR") != null) {

                _course.getAsJsonArray("scanExamSheetsR").forEach(gr -> {
                    String left = gr.getAsJsonObject().get("left").getAsString();
                    String right = gr.getAsJsonObject().get("right").getAsString();

                    Scan ex = Scan.findById(uuidId.get(left));
                    ExamSheet st = ExamSheet.findById(uuidId.get(right));
                    ex.sheets.add(st);
                    st.scan = ex;
                });
            }
        }

        if (_course.getAsJsonArray("questionTextCommentsR") != null) {

            _course.getAsJsonArray("questionTextCommentsR").forEach(gr -> {
                String left = gr.getAsJsonObject().get("left").getAsString();
                String right = gr.getAsJsonObject().get("right").getAsString();
                Question ex = Question.findById(uuidId.get(left));
                TextComment st = TextComment.findById(uuidId.get(right));
                ex.textcomments.add(st);
                st.question = ex;
            });
        }

        if (_course.getAsJsonArray("questionGradedCommentsR") != null) {

            _course.getAsJsonArray("questionGradedCommentsR").forEach(gr -> {
                String left = gr.getAsJsonObject().get("left").getAsString();
                String right = gr.getAsJsonObject().get("right").getAsString();
                Question ex = Question.findById(uuidId.get(left));
                GradedComment st = GradedComment.findById(uuidId.get(right));
                ex.gradedcomments.add(st);
                st.question = ex;
            });
        }

        if (_course.getAsJsonArray("questionHybridCommentsR") != null) {

            _course.getAsJsonArray("questionHybridCommentsR").forEach(gr -> {
                String left = gr.getAsJsonObject().get("left").getAsString();
                String right = gr.getAsJsonObject().get("right").getAsString();
                Question ex = Question.findById(uuidId.get(left));
                HybridGradedComment st = HybridGradedComment.findById(uuidId.get(right));
                ex.hybridcomments.add(st);
                st.question = ex;
            });
        }

        if (includeStudentData) {

            if (_course.getAsJsonArray("studentResponseTextCommentsR") != null) {

                _course.getAsJsonArray("studentResponseTextCommentsR").forEach(gr -> {
                    String left = gr.getAsJsonObject().get("left").getAsString();
                    String right = gr.getAsJsonObject().get("right").getAsString();
                    StudentResponse ex = StudentResponse.findById(uuidId.get(left));
                    TextComment st = TextComment.findById(uuidId.get(right));
                    ex.textcomments.add(st);
                    st.studentResponses.add(ex);
                });
            }
            if (_course.getAsJsonArray("studentResponseGradedCommentsR") != null) {

                _course.getAsJsonArray("studentResponseGradedCommentsR").forEach(gr -> {
                    String left = gr.getAsJsonObject().get("left").getAsString();
                    String right = gr.getAsJsonObject().get("right").getAsString();
                    StudentResponse ex = StudentResponse.findById(uuidId.get(left));
                    GradedComment st = GradedComment.findById(uuidId.get(right));
                    ex.gradedcomments.add(st);
                    st.studentResponses.add(ex);
                });
            }
            if (_course.getAsJsonArray("studentResponseHybridCommentsR") != null) {

                _course.getAsJsonArray("studentResponseHybridCommentsR").forEach(gr -> {
                    String left = gr.getAsJsonObject().get("left").getAsString();
                    String right = gr.getAsJsonObject().get("right").getAsString();
                    StudentResponse ex = StudentResponse.findById(uuidId.get(left));
                    HybridGradedComment st = HybridGradedComment.findById(uuidId.get(right));
                    Answer2HybridGradedComment an = new Answer2HybridGradedComment();
                    an.studentResponse=ex;
                    ex.hybridcommentsValues.add(an);
                    an.hybridcomments=st;
                    st.valueAnswers.add(an);
                    if (gr.getAsJsonObject().get("stepValue") != null){
                        an.stepValue =  gr.getAsJsonObject().get("stepValue").getAsInt();
                    }
                    an.persistAndFlush();
                });
            }

            if (_course.getAsJsonArray("studentExamSheetR") != null) {

                _course.getAsJsonArray("studentExamSheetR").forEach(gr -> {
                    String left = gr.getAsJsonObject().get("left").getAsString();
                    String right = gr.getAsJsonObject().get("right").getAsString();
                    Student ex = Student.findById(uuidId.get(left));
                    ExamSheet st = ExamSheet.findById(uuidId.get(right));
                    ex.examSheets.add(st);
                });
            }
        }
        if (_course.getAsJsonArray("questionZoneR") != null) {

            _course.getAsJsonArray("questionZoneR").forEach(gr -> {
                String left = gr.getAsJsonObject().get("left").getAsString();
                String right = gr.getAsJsonObject().get("right").getAsString();
                Question ex = Question.findById(uuidId.get(left));
                Zone st = Zone.findById(uuidId.get(right));
                ex.zone = st;
            });
        }

        return course;

    }

    protected void putObject(String name, byte[] bytes, String contenttype)
            throws InvalidKeyException, NoSuchAlgorithmException, IllegalArgumentException, IOException {
        this.fichierS3Service.putObject(name, bytes, contenttype);
    }

    @Transactional
    public CourseDTO importCourse(@RestForm("file") org.jboss.resteasy.reactive.multipart.FileUpload file, User user, boolean includeStudentData) {
    try{

            FileInputStream inputStream = new FileInputStream(file.uploadedFile().toFile());
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
                JsonElement jelement = JsonParser.parseReader(reader);
                Course c = this.importCourse(jelement.getAsJsonObject(), user, includeStudentData);

                return courseMapper.toDto(c);
            } catch (IOException e) {
                e.printStackTrace();
            }
        return null;
    }
}
