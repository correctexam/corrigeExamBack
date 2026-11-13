package fr.istic.web.rest;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.istic.domain.Course;
import fr.istic.domain.Exam;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.FinalResult;
import fr.istic.domain.Question;
import fr.istic.domain.Scan;
import fr.istic.domain.Student;
import fr.istic.domain.StudentResponse;
import fr.istic.domain.Zone;
import fr.istic.domain.enumeration.GradeType;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;

/**
 * {@link PublicSheetResource}: one sheet read through its UUID, without authentication, and nothing else
 * (ADR 0003, T10c). The page contents come from the cache of the exam and are not checked here, only their bounds.
 */
@QuarkusTest
class PublicSheetResourceTest {

    private String uuid;
    private String otherUuid;

    @BeforeEach
    void fixture() {
        uuid = UUID.randomUUID().toString();
        otherUuid = UUID.randomUUID().toString();
        QuarkusTransaction.requiringNew().run(() -> {
            Course course = new Course();
            course.name = "Algo " + uuid;
            course.persist();

            Scan scan = new Scan();
            scan.name = "scan " + uuid;
            scan.persist();

            Exam exam = new Exam();
            exam.name = "Partiel " + uuid;
            exam.course = course;
            exam.scanfile = scan;
            exam.persist();

            Zone zone = new Zone();
            zone.pageNumber = 2;
            zone.persist();

            Question question = new Question();
            question.numero = 1;
            question.quarterpoint = 8;
            question.step = 4;
            question.gradeType = GradeType.DIRECT;
            question.exam = exam;
            question.zone = zone;
            question.persist();

            ExamSheet sheet = sheet(scan, uuid, 0);
            ExamSheet other = sheet(scan, otherUuid, 2);

            Student student = student("Martin", "Alice", "INE-" + uuid, sheet);
            student("Durand", "Bob", "INE2-" + uuid, other);

            response(question, sheet, 6, false);
            response(question, other, 8, true);

            FinalResult result = new FinalResult();
            result.student = student;
            result.exam = exam;
            result.note = 1250;
            result.persist();
        });
    }

    private static ExamSheet sheet(Scan scan, String name, int pagemin) {
        ExamSheet sheet = new ExamSheet();
        sheet.name = name;
        sheet.scan = scan;
        sheet.pagemin = pagemin;
        sheet.pagemax = pagemin + 1;
        sheet.persist();
        return sheet;
    }

    private static Student student(String name, String firstname, String ine, ExamSheet sheet) {
        Student student = new Student();
        student.name = name;
        student.firstname = firstname;
        student.ine = ine;
        student.mail = firstname.toLowerCase() + "@example.org";
        student.examSheets.add(sheet);
        student.persist();
        sheet.students.add(student);
        return student;
    }

    private static void response(Question question, ExamSheet sheet, int quarternote, boolean star) {
        StudentResponse response = new StudentResponse();
        response.question = question;
        response.sheet = sheet;
        response.quarternote = quarternote;
        response.star = star;
        response.worststar = false;
        response.persist();
    }

    @Test
    void returnsTheSheetOfTheUuidOnly() {
        given()
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/public/copie/{uuid}", uuid)
            .then()
            .statusCode(200)
            .body("uuid", equalTo(uuid))
            .body("pages", equalTo(2))
            .body("exam.name", equalTo("Partiel " + uuid))
            .body("students.name", contains("Martin"))
            .body("finalNote", equalTo(12.5f))
            .body("corrections", equalTo(true))
            .body("questions", hasSize(1))
            .body("responses", hasSize(1))
            .body("responses[0].note", equalTo(1.5f))
            // Nothing that identifies the student beyond the name, nothing of the other sheet
            .body(not(containsString("INE-")))
            .body(not(containsString("@example.org")))
            .body(not(containsString("Durand")));
    }

    @Test
    void unknownUuidIsNotFound() {
        given().when().get("/api/public/copie/{uuid}", UUID.randomUUID().toString()).then().statusCode(404);
        given().when().get("/api/public/copie/{uuid}/page/1", UUID.randomUUID().toString()).then().statusCode(404);
        given().when().get("/api/public/copie/{uuid}/best/1", UUID.randomUUID().toString()).then().statusCode(404);
    }

    @Test
    void onlyThePagesOfTheSheetCanBeRead() {
        given().when().get("/api/public/copie/{uuid}/page/0", uuid).then().statusCode(404);
        given().when().get("/api/public/copie/{uuid}/page/3", uuid).then().statusCode(404);
    }

    @Test
    void countsTheStarredAnswersAndHidesWhatIsNotTheQuestion() {
        given().when().get("/api/public/copie/{uuid}/best/1", uuid).then().statusCode(200).body("count", equalTo(1));
        given().when().get("/api/public/copie/{uuid}/best/2", uuid).then().statusCode(200).body("count", equalTo(0));
        // Page 1 holds no zone of question 1; index 1 does not exist
        given().when().get("/api/public/copie/{uuid}/best/1/0/page/1", uuid).then().statusCode(404);
        given().when().get("/api/public/copie/{uuid}/best/1/1/page/2", uuid).then().statusCode(404);
    }
}
