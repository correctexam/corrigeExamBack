package fr.istic.web.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.istic.TestUtil;
import fr.istic.domain.Course;
import fr.istic.domain.Exam;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.Question;
import fr.istic.domain.Scan;
import fr.istic.domain.Zone;
import fr.istic.domain.enumeration.GradeType;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;

/**
 * Reads formerly open to anonymous callers (T10c): they now need an instructor session with access to the exam.
 * The student view reads its sheet through /api/public/copie/{uuid} and /api/student/me only.
 */
@QuarkusTest
class PrivateReadsSecurityTest {

    private long courseId;
    private long examId;
    private long scanId;
    private long questionId;
    private long zoneId;
    private long sheetId;
    private String sheetName;

    @BeforeEach
    void fixture() {
        String suffix = UUID.randomUUID().toString();
        sheetName = suffix;
        QuarkusTransaction.requiringNew().run(() -> {
            Course course = new Course();
            course.name = "Course " + suffix;
            course.persist();

            Scan scan = new Scan();
            scan.name = "scan " + suffix;
            scan.persist();

            Exam exam = new Exam();
            exam.name = "Exam " + suffix;
            exam.course = course;
            exam.scanfile = scan;
            exam.persist();

            Zone zone = new Zone();
            zone.pageNumber = 1;
            zone.persist();

            Question question = new Question();
            question.numero = 1;
            question.quarterpoint = 4;
            question.step = 4;
            question.gradeType = GradeType.DIRECT;
            question.exam = exam;
            question.zone = zone;
            // NOT NULL columns without default in the entity
            question.canExceedTheMax = false;
            question.canBeNegative = false;
            question.mustBeIgnoreInGlobalScale = false;
            question.randomHorizontalCorrection = false;
            question.persist();

            ExamSheet sheet = new ExamSheet();
            sheet.name = suffix;
            sheet.scan = scan;
            sheet.pagemin = 0;
            sheet.pagemax = 0;
            sheet.persist();

            courseId = course.id;
            examId = exam.id;
            scanId = scan.id;
            questionId = question.id;
            zoneId = zone.id;
            sheetId = sheet.id;
        });
    }

    private List<String> reads() {
        return List.of(
            "/api/exams/" + examId,
            "/api/exams?scanId=" + scanId,
            "/api/questions?examId=" + examId,
            "/api/questions?zoneId=" + zoneId,
            "/api/zones/" + zoneId,
            "/api/zones/byExam/" + examId,
            "/api/scans/" + scanId,
            "/api/exam-sheets?name=" + sheetName,
            "/api/exam-sheets?examId=" + examId,
            "/api/students?sheetId=" + sheetId,
            "/api/student-responses?sheetId=" + sheetId + "&questionsId=" + questionId,
            "/api/student-responses?questionId=" + questionId,
            "/api/text-comments?questionId=" + questionId,
            "/api/graded-comments?questionId=" + questionId,
            "/api/hybrid-graded-comments?questionId=" + questionId,
            "/api/final-results?examId=" + examId + "&studentId=1",
            "/api/comments?zonegeneratedid=" + examId + "_" + sheetId + "_1_0",
            "/api/getComments/" + examId,
            "/api/getAllEmailProfs4course/" + courseId,
            "/api/getAllStarAnswer/" + examId,
            "/api/getBestAnswer/" + examId + "/1",
            "/api/getCacheAlignPage/" + examId + "/1",
            "/api/getCacheNonAlignPage/" + examId + "/1",
            "/api/getScanPdf/" + scanId,
            "/api/exportpdf4sheet/" + examId + "/" + sheetName
        );
    }

    @Test
    void anonymousCallersAreRejected() {
        for (String url : reads()) {
            given().when().get(url).then().statusCode(401);
        }
    }

    @Test
    void anInstructorWithoutAccessToTheExamIsRejected() {
        String token = TestUtil.getToken("user", "user");
        for (String url : reads()) {
            given().auth().oauth2(token).when().get(url).then().statusCode(403);
        }
    }

    @Test
    void anAdministratorStillReads() {
        String token = TestUtil.getAdminToken();
        given().auth().oauth2(token).when().get("/api/exams/{id}", examId).then().statusCode(200).body("id", equalTo((int) examId));
        given().auth().oauth2(token).when().get("/api/questions?examId={id}", examId).then().statusCode(200);
        given().auth().oauth2(token).when().get("/api/zones/byExam/{id}", examId).then().statusCode(200);
    }
}
