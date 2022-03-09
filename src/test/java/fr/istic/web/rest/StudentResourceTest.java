package fr.istic.web.rest;

import static io.restassured.RestAssured.given;
import static io.restassured.config.ObjectMapperConfig.objectMapperConfig;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

import fr.istic.TestUtil;
import fr.istic.service.dto.StudentDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import liquibase.Liquibase;
import io.quarkus.liquibase.LiquibaseFactory;
import org.junit.jupiter.api.*;

import javax.inject.Inject;

import java.util.List;
    import java.util.ArrayList;

@QuarkusTest
public class StudentResourceTest {

    private static final TypeRef<StudentDTO> ENTITY_TYPE = new TypeRef<>() {
    };

    private static final TypeRef<List<StudentDTO>> LIST_OF_ENTITY_TYPE = new TypeRef<>() {
    };

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_FIRSTNAME = "AAAAAAAAAA";
    private static final String UPDATED_FIRSTNAME = "BBBBBBBBBB";

    private static final String DEFAULT_INE = "AAAAAAAAAA";
    private static final String UPDATED_INE = "BBBBBBBBBB";

    private static final String DEFAULT_CASLOGIN = "AAAAAAAAAA";
    private static final String UPDATED_CASLOGIN = "BBBBBBBBBB";

    private static final String DEFAULT_MAIL = "AAAAAAAAAA";
    private static final String UPDATED_MAIL = "BBBBBBBBBB";



    String adminToken;

    StudentDTO studentDTO;

    @Inject
    LiquibaseFactory liquibaseFactory;

    @BeforeAll
    static void jsonMapper() {
        RestAssured.config =
            RestAssured.config().objectMapperConfig(objectMapperConfig().defaultObjectMapper(TestUtil.jsonbObjectMapper()));
    }

    @BeforeEach
    public void authenticateAdmin() {
        this.adminToken = TestUtil.getAdminToken();
    }

    @BeforeEach
    public void databaseFixture() {
        try (Liquibase liquibase = liquibaseFactory.createLiquibase()) {
            liquibase.dropAll();
            liquibase.validate();
            liquibase.update(liquibaseFactory.createContexts(), liquibaseFactory.createLabels());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static StudentDTO createEntity() {
        var studentDTO = new StudentDTO();
        studentDTO.name = DEFAULT_NAME;
        studentDTO.firstname = DEFAULT_FIRSTNAME;
        studentDTO.ine = DEFAULT_INE;
        studentDTO.caslogin = DEFAULT_CASLOGIN;
        studentDTO.mail = DEFAULT_MAIL;
        return studentDTO;
    }

    @BeforeEach
    public void initTest() {
        studentDTO = createEntity();
    }

    @Test
    public void createStudent() {
        var databaseSizeBeforeCreate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/students")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Create the Student
        studentDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(studentDTO)
            .when()
            .post("/api/students")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        // Validate the Student in the database
        var studentDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/students")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(studentDTOList).hasSize(databaseSizeBeforeCreate + 1);
        var testStudentDTO = studentDTOList.stream().filter(it -> studentDTO.id.equals(it.id)).findFirst().get();
        assertThat(testStudentDTO.name).isEqualTo(DEFAULT_NAME);
        assertThat(testStudentDTO.firstname).isEqualTo(DEFAULT_FIRSTNAME);
        assertThat(testStudentDTO.ine).isEqualTo(DEFAULT_INE);
        assertThat(testStudentDTO.caslogin).isEqualTo(DEFAULT_CASLOGIN);
        assertThat(testStudentDTO.mail).isEqualTo(DEFAULT_MAIL);
    }

    @Test
    public void createStudentWithExistingId() {
        var databaseSizeBeforeCreate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/students")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Create the Student with an existing ID
        studentDTO.id = 1L;

        // An entity with an existing ID cannot be created, so this API call must fail
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(studentDTO)
            .when()
            .post("/api/students")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());

        // Validate the Student in the database
        var studentDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/students")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(studentDTOList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    public void checkNameIsRequired() throws Exception {
        var databaseSizeBeforeTest = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/students")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // set the field null
        studentDTO.name = null;

        // Create the Student, which fails.
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(studentDTO)
            .when()
            .post("/api/students")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());

        // Validate the Student in the database
        var studentDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/students")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(studentDTOList).hasSize(databaseSizeBeforeTest);
    }
    @Test
    public void checkIneIsRequired() throws Exception {
        var databaseSizeBeforeTest = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/students")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // set the field null
        studentDTO.ine = null;

        // Create the Student, which fails.
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(studentDTO)
            .when()
            .post("/api/students")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());

        // Validate the Student in the database
        var studentDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/students")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(studentDTOList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void updateStudent() {
        // Initialize the database
        studentDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(studentDTO)
            .when()
            .post("/api/students")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var databaseSizeBeforeUpdate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/students")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Get the student
        var updatedStudentDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/students/{id}", studentDTO.id)
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().body().as(ENTITY_TYPE);

        // Update the student
        updatedStudentDTO.name = UPDATED_NAME;
        updatedStudentDTO.firstname = UPDATED_FIRSTNAME;
        updatedStudentDTO.ine = UPDATED_INE;
        updatedStudentDTO.caslogin = UPDATED_CASLOGIN;
        updatedStudentDTO.mail = UPDATED_MAIL;

        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(updatedStudentDTO)
            .when()
            .put("/api/students")
            .then()
            .statusCode(OK.getStatusCode());

        // Validate the Student in the database
        var studentDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/students")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(studentDTOList).hasSize(databaseSizeBeforeUpdate);
        var testStudentDTO = studentDTOList.stream().filter(it -> updatedStudentDTO.id.equals(it.id)).findFirst().get();
        assertThat(testStudentDTO.name).isEqualTo(UPDATED_NAME);
        assertThat(testStudentDTO.firstname).isEqualTo(UPDATED_FIRSTNAME);
        assertThat(testStudentDTO.ine).isEqualTo(UPDATED_INE);
        assertThat(testStudentDTO.caslogin).isEqualTo(UPDATED_CASLOGIN);
        assertThat(testStudentDTO.mail).isEqualTo(UPDATED_MAIL);
    }

    @Test
    public void updateNonExistingStudent() {
        var databaseSizeBeforeUpdate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/students")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(studentDTO)
            .when()
            .put("/api/students")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());

        // Validate the Student in the database
        var studentDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/students")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(studentDTOList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    public void deleteStudent() {
        // Initialize the database
        studentDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(studentDTO)
            .when()
            .post("/api/students")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var databaseSizeBeforeDelete = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/students")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Delete the student
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .delete("/api/students/{id}", studentDTO.id)
            .then()
            .statusCode(NO_CONTENT.getStatusCode());

        // Validate the database contains one less item
        var studentDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/students")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(studentDTOList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void getAllStudents() {
        // Initialize the database
        studentDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(studentDTO)
            .when()
            .post("/api/students")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        // Get all the studentList
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/students?sort=id,desc")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .body("id", hasItem(studentDTO.id.intValue()))
            .body("name", hasItem(DEFAULT_NAME))            .body("firstname", hasItem(DEFAULT_FIRSTNAME))            .body("ine", hasItem(DEFAULT_INE))            .body("caslogin", hasItem(DEFAULT_CASLOGIN))            .body("mail", hasItem(DEFAULT_MAIL));
    }

    @Test
    public void getStudent() {
        // Initialize the database
        studentDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(studentDTO)
            .when()
            .post("/api/students")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var response = // Get the student
            given()
                .auth()
                .preemptive()
                .oauth2(adminToken)
                .accept(APPLICATION_JSON)
                .when()
                .get("/api/students/{id}", studentDTO.id)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ENTITY_TYPE);

        // Get the student
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/students/{id}", studentDTO.id)
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .body("id", is(studentDTO.id.intValue()))
            
                .body("name", is(DEFAULT_NAME))
                .body("firstname", is(DEFAULT_FIRSTNAME))
                .body("ine", is(DEFAULT_INE))
                .body("caslogin", is(DEFAULT_CASLOGIN))
                .body("mail", is(DEFAULT_MAIL));
    }

    @Test
    public void getNonExistingStudent() {
        // Get the student
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/students/{id}", Long.MAX_VALUE)
            .then()
            .statusCode(NOT_FOUND.getStatusCode());
    }
}
