package fr.istic.web.rest;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.istic.domain.Course;
import fr.istic.domain.Exam;
import fr.istic.domain.ExamSheet;
import fr.istic.domain.FinalResult;
import fr.istic.domain.Scan;
import fr.istic.domain.Student;
import fr.istic.domain.enumeration.StudentAccess;
import fr.istic.service.AltchaService;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

/**
 * Student portal (ADR 0003): captcha, link sent by e-mail, single-use session token, sheets filtered by the student
 * access of their exam, and a session that opens nothing else.
 */
@QuarkusTest
class StudentPortalResourceTest {

    private static final Pattern LINK = Pattern.compile("/my/([A-Za-z0-9_-]{20,})");

    @Inject
    MockMailbox mailbox;

    private String email;
    private long gradedSheet;
    private long sheetOnlySheet;
    private long hiddenSheet;
    private long otherSheet;

    @BeforeEach
    void fixture() {
        mailbox.clear();
        String id = UUID.randomUUID().toString().substring(0, 8);
        email = "alice." + id + "@example.org";
        long[] ids = QuarkusTransaction.requiringNew().call(() -> {
            Course course = new Course();
            course.name = "Algo " + id;
            course.persist();
            Student alice = student("Martin", "Alice", " " + email.toUpperCase() + " ");
            Student bob = student("Durand", "Bob", "bob." + id + "@example.org");
            ExamSheet graded = exam(course, "Partiel", StudentAccess.GRADED, alice, 1250);
            ExamSheet sheetOnly = exam(course, "Examen", StudentAccess.SHEET, alice, 900);
            ExamSheet hidden = exam(course, "Rattrapage", StudentAccess.HIDDEN, alice, 1000);
            ExamSheet other = exam(course, "Partiel Bob", StudentAccess.GRADED, bob, 1500);
            return new long[] { graded.id, sheetOnly.id, hidden.id, other.id };
        });
        gradedSheet = ids[0];
        sheetOnlySheet = ids[1];
        hiddenSheet = ids[2];
        otherSheet = ids[3];
    }

    private static Student student(String name, String firstname, String mail) {
        Student s = new Student();
        s.name = name;
        s.firstname = firstname;
        s.ine = "INE-" + UUID.randomUUID();
        s.mail = mail;
        s.persist();
        return s;
    }

    private static ExamSheet exam(Course course, String name, StudentAccess access, Student student, int note) {
        Scan scan = new Scan();
        scan.name = name;
        scan.persist();
        Exam exam = new Exam();
        exam.name = name;
        exam.course = course;
        exam.scanfile = scan;
        exam.studentAccess = access;
        exam.persist();
        ExamSheet sheet = new ExamSheet();
        sheet.name = UUID.randomUUID().toString();
        sheet.scan = scan;
        sheet.pagemin = 0;
        sheet.pagemax = 1;
        sheet.persist();
        student.examSheets.add(sheet);
        sheet.students.add(student);
        FinalResult result = new FinalResult();
        result.student = student;
        result.exam = exam;
        result.note = note;
        result.persist();
        return sheet;
    }

    /** Solves an ALTCHA challenge of the server, as the widget does. */
    private static String captcha() {
        Map<String, Object> c = given().when().get("/api/public/my/challenge").then().statusCode(200).extract().as(Map.class);
        String salt = (String) c.get("salt");
        String challenge = (String) c.get("challenge");
        int max = ((Number) c.get("maxnumber")).intValue();
        for (int n = 0; n <= max; n++) {
            if (AltchaService.sha256Hex(salt + n).equals(challenge)) {
                String json = "{\"algorithm\":\"SHA-256\",\"challenge\":\"" + challenge + "\",\"number\":" + n + ",\"salt\":\""
                        + salt + "\",\"signature\":\"" + c.get("signature") + "\"}";
                return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
            }
        }
        throw new IllegalStateException("unsolvable challenge");
    }

    private static int askLink(String email, String captcha) {
        return given()
            .contentType(APPLICATION_JSON)
            .body(Map.of("email", email, "altcha", captcha, "lang", "en"))
            .when()
            .post("/api/public/my/link")
            .then()
            .extract()
            .statusCode();
    }

    private List<Mail> mailsTo(String to) throws InterruptedException {
        // The e-mail is sent asynchronously
        for (int i = 0; i < 50 && mailbox.getMailsSentTo(to).isEmpty(); i++) {
            Thread.sleep(100);
        }
        return mailbox.getMailsSentTo(to);
    }

    private String sessionFor(String email) throws InterruptedException {
        assertThat(askLink(email, captcha())).isEqualTo(202);
        List<Mail> mails = mailsTo(email);
        assertThat(mails).hasSize(1);
        Matcher m = LINK.matcher(mails.get(0).getHtml());
        assertThat(m.find()).isTrue();
        return given()
            .contentType(APPLICATION_JSON)
            .body(Map.of("token", m.group(1)))
            .when()
            .post("/api/public/my/session")
            .then()
            .statusCode(200)
            .body("email", equalTo(email))
            .extract()
            .path("idToken");
    }

    @Test
    void sendsALinkToAKnownAddressOnlyWithTheSameAnswer() throws InterruptedException {
        assertThat(askLink(email.toUpperCase(), captcha())).isEqualTo(202);
        assertThat(mailsTo(email)).hasSize(1);
        assertThat(mailsTo(email).get(0).getHtml()).contains("valid for 30 minutes");

        String unknown = "nobody." + UUID.randomUUID() + "@example.org";
        assertThat(askLink(unknown, captcha())).isEqualTo(202);
        Thread.sleep(300);
        assertThat(mailbox.getMailsSentTo(unknown)).isEmpty();
    }

    @Test
    void refusesAMissingWrongOrReusedCaptchaAndAMalformedAddress() {
        assertThat(askLink(email, "")).isEqualTo(400);
        assertThat(askLink(email, "e30=")).isEqualTo(400);
        String solved = captcha();
        assertThat(askLink(email, solved)).isEqualTo(202);
        assertThat(askLink(email, solved)).isEqualTo(400);
        assertThat(askLink("not-an-address", captcha())).isEqualTo(400);
    }

    @Test
    void limitsTheRequestsPerAddress() {
        String address = "carol." + UUID.randomUUID() + "@example.org";
        for (int i = 0; i < 3; i++) {
            assertThat(askLink(address, captcha())).isEqualTo(202);
        }
        assertThat(askLink(address, captcha())).isEqualTo(429);
    }

    @Test
    void theLinkOpensOneSessionOnly() throws InterruptedException {
        assertThat(askLink(email, captcha())).isEqualTo(202);
        Matcher m = LINK.matcher(mailsTo(email).get(0).getHtml());
        assertThat(m.find()).isTrue();
        Map<String, String> body = Map.of("token", m.group(1));
        given().contentType(APPLICATION_JSON).body(body).when().post("/api/public/my/session").then().statusCode(200);
        given().contentType(APPLICATION_JSON).body(body).when().post("/api/public/my/session").then().statusCode(401);
        given().contentType(APPLICATION_JSON).body(Map.of("token", "unknown-token-value-1234")).when()
            .post("/api/public/my/session").then().statusCode(401);
    }

    @Test
    void listsTheSheetsOfTheAddressAsPublished() throws InterruptedException {
        String token = sessionFor(email);
        given()
            .auth().oauth2(token)
            .when()
            .get("/api/student/me/sheets")
            .then()
            .statusCode(200)
            .body("$", hasSize(2))
            .body("find { it.sheetId == " + gradedSheet + " }.access", equalTo("GRADED"))
            .body("find { it.sheetId == " + gradedSheet + " }.finalNote", equalTo(12.5f))
            .body("find { it.sheetId == " + sheetOnlySheet + " }.access", equalTo("SHEET"))
            .body("find { it.sheetId == " + sheetOnlySheet + " }.finalNote", nullValue())
            .body(not(containsString("\"uuid\"")));

        given().auth().oauth2(token).when().get("/api/student/me/sheets/{id}", gradedSheet).then()
            .statusCode(200).body("corrections", equalTo(true)).body("finalNote", equalTo(12.5f)).body("uuid", nullValue());
        given().auth().oauth2(token).when().get("/api/student/me/sheets/{id}", sheetOnlySheet).then()
            .statusCode(200).body("corrections", equalTo(false)).body("finalNote", nullValue()).body("responses", hasSize(0));
        given().auth().oauth2(token).when().get("/api/student/me/sheets/{id}", hiddenSheet).then().statusCode(404);
        given().auth().oauth2(token).when().get("/api/student/me/sheets/{id}", otherSheet).then().statusCode(404);
        given().auth().oauth2(token).when().get("/api/student/me/sheets/{id}/best/1", sheetOnlySheet).then().statusCode(404);
        given().auth().oauth2(token).when().get("/api/student/me/sheets/{id}/page/3", gradedSheet).then().statusCode(404);
    }

    @Test
    void givesThePersonalDataIncludingHiddenExams() throws InterruptedException {
        String token = sessionFor(email);
        given()
            .auth().oauth2(token)
            .when()
            .get("/api/student/me/data")
            .then()
            .statusCode(200)
            .body("email", equalTo(email))
            .body("records", hasSize(1))
            .body("records[0].name", equalTo("Martin"))
            .body("sheets", hasSize(3))
            .body(not(containsString("Durand")));
    }

    @Test
    void aStudentSessionOpensNothingElse() throws InterruptedException {
        String token = sessionFor(email);
        given().auth().oauth2(token).when().put("/api/exams/1/student-access/GRADED").then().statusCode(403);
        given().auth().oauth2(token).when().get("/api/account").then().statusCode(403);
        given().when().get("/api/student/me/sheets").then().statusCode(401);
    }
}
