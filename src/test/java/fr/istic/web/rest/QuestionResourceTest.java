package fr.istic.web.rest;

import static io.restassured.RestAssured.given;
import static io.restassured.config.ObjectMapperConfig.objectMapperConfig;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

import fr.istic.TestUtil;
import fr.istic.domain.enumeration.GradeType;
import fr.istic.service.dto.QuestionDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import liquibase.Liquibase;
import io.quarkus.liquibase.LiquibaseFactory;
import org.junit.jupiter.api.*;

import jakarta.inject.Inject;

import java.util.List;

@QuarkusTest
public class QuestionResourceTest {

    private static final TypeRef<QuestionDTO> ENTITY_TYPE = new TypeRef<>() {
    };

    private static final TypeRef<List<QuestionDTO>> LIST_OF_ENTITY_TYPE = new TypeRef<>() {
    };

    private static final Integer DEFAULT_NUMERO = 1;
    private static final Integer UPDATED_NUMERO = 2;

    private static final Double DEFAULT_POINT = 1.0;
    private static final Double UPDATED_POINT = 2.0;

    private static final Integer DEFAULT_STEP = 1;
    private static final Integer UPDATED_STEP = 2;

    private static final GradeType DEFAULT_GRADE_TYPE = GradeType.DIRECT;
    private static final GradeType UPDATED_GRADE_TYPE = GradeType.POSITIVE;



    String adminToken;

    QuestionDTO questionDTO;

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
    public static QuestionDTO createEntity() {
        var questionDTO = new QuestionDTO();
        questionDTO.numero = DEFAULT_NUMERO;
        questionDTO.point = DEFAULT_POINT;
        questionDTO.step = DEFAULT_STEP;
        questionDTO.gradeType = DEFAULT_GRADE_TYPE;
        return questionDTO;
    }

    @BeforeEach
    public void initTest() {
        questionDTO = createEntity();
    }

    @Test
    public void createQuestion() {
        var databaseSizeBeforeCreate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/questions")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Create the Question
        questionDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(questionDTO)
            .when()
            .post("/api/questions")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        // Validate the Question in the database
        var questionDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/questions")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(questionDTOList).hasSize(databaseSizeBeforeCreate + 1);
        var testQuestionDTO = questionDTOList.stream().filter(it -> questionDTO.id.equals(it.id)).findFirst().get();
        assertThat(testQuestionDTO.numero).isEqualTo(DEFAULT_NUMERO);
        assertThat(testQuestionDTO.point).isEqualTo(DEFAULT_POINT);
        assertThat(testQuestionDTO.step).isEqualTo(DEFAULT_STEP);
        assertThat(testQuestionDTO.gradeType).isEqualTo(DEFAULT_GRADE_TYPE);
    }

    @Test
    public void createQuestionWithExistingId() {
        var databaseSizeBeforeCreate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/questions")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Create the Question with an existing ID
        questionDTO.id = 1L;

        // An entity with an existing ID cannot be created, so this API call must fail
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(questionDTO)
            .when()
            .post("/api/questions")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());

        // Validate the Question in the database
        var questionDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/questions")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(questionDTOList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    public void checkNumeroIsRequired() throws Exception {
        var databaseSizeBeforeTest = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/questions")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // set the field null
        questionDTO.numero = null;

        // Create the Question, which fails.
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(questionDTO)
            .when()
            .post("/api/questions")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());

        // Validate the Question in the database
        var questionDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/questions")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(questionDTOList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void updateQuestion() {
        // Initialize the database
        questionDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(questionDTO)
            .when()
            .post("/api/questions")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var databaseSizeBeforeUpdate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/questions")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Get the question
        var updatedQuestionDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/questions/{id}", questionDTO.id)
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().body().as(ENTITY_TYPE);

        // Update the question
        updatedQuestionDTO.numero = UPDATED_NUMERO;
        updatedQuestionDTO.point = UPDATED_POINT;
        updatedQuestionDTO.step = UPDATED_STEP;
        updatedQuestionDTO.gradeType = UPDATED_GRADE_TYPE;

        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(updatedQuestionDTO)
            .when()
            .put("/api/questions")
            .then()
            .statusCode(OK.getStatusCode());

        // Validate the Question in the database
        var questionDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/questions")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(questionDTOList).hasSize(databaseSizeBeforeUpdate);
        var testQuestionDTO = questionDTOList.stream().filter(it -> updatedQuestionDTO.id.equals(it.id)).findFirst().get();
        assertThat(testQuestionDTO.numero).isEqualTo(UPDATED_NUMERO);
        assertThat(testQuestionDTO.point).isEqualTo(UPDATED_POINT);
        assertThat(testQuestionDTO.step).isEqualTo(UPDATED_STEP);
        assertThat(testQuestionDTO.gradeType).isEqualTo(UPDATED_GRADE_TYPE);
    }

    @Test
    public void updateNonExistingQuestion() {
        var databaseSizeBeforeUpdate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/questions")
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
            .body(questionDTO)
            .when()
            .put("/api/questions")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());

        // Validate the Question in the database
        var questionDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/questions")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(questionDTOList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    public void deleteQuestion() {
        // Initialize the database
        questionDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(questionDTO)
            .when()
            .post("/api/questions")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var databaseSizeBeforeDelete = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/questions")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Delete the question
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .delete("/api/questions/{id}", questionDTO.id)
            .then()
            .statusCode(NO_CONTENT.getStatusCode());

        // Validate the database contains one less item
        var questionDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/questions")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(questionDTOList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void getAllQuestions() {
        // Initialize the database
        questionDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(questionDTO)
            .when()
            .post("/api/questions")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        // Get all the questionList
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/questions?sort=id,desc")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .body("id", hasItem(questionDTO.id.intValue()))
            .body("numero", hasItem(DEFAULT_NUMERO.intValue()))
            .body("point", hasItem(DEFAULT_POINT.floatValue()))
            .body("step", hasItem(DEFAULT_STEP.intValue()))
            .body("gradeType", hasItem(DEFAULT_GRADE_TYPE.toString()));
    }

    @Test
    public void getQuestion() {
        // Initialize the database
        questionDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(questionDTO)
            .when()
            .post("/api/questions")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var response = // Get the question
            given()
                .auth()
                .preemptive()
                .oauth2(adminToken)
                .accept(APPLICATION_JSON)
                .when()
                .get("/api/questions/{id}", questionDTO.id)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ENTITY_TYPE);

        // Get the question
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/questions/{id}", questionDTO.id)
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .body("id", is(questionDTO.id.intValue()))

                .body("numero", is(DEFAULT_NUMERO.intValue()))
                .body("point", is(DEFAULT_POINT.floatValue()))
                .body("step", is(DEFAULT_STEP.intValue()))
                .body("gradeType", is(DEFAULT_GRADE_TYPE.toString()));
    }

    @Test
    public void getNonExistingQuestion() {
        // Get the question
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/questions/{id}", Long.MAX_VALUE)
            .then()
            .statusCode(NOT_FOUND.getStatusCode());
    }
}
