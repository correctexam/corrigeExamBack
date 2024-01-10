package fr.istic.web.rest;

import static io.restassured.RestAssured.given;
import static io.restassured.config.ObjectMapperConfig.objectMapperConfig;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

import fr.istic.TestUtil;
import fr.istic.service.dto.QuestionTypeDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import liquibase.Liquibase;
import io.quarkus.liquibase.LiquibaseFactory;
import org.junit.jupiter.api.*;

import jakarta.inject.Inject;

import java.util.List;

@QuarkusTest
public class QuestionTypeResourceTest {

    private static final TypeRef<QuestionTypeDTO> ENTITY_TYPE = new TypeRef<>() {
    };

    private static final TypeRef<List<QuestionTypeDTO>> LIST_OF_ENTITY_TYPE = new TypeRef<>() {
    };

    private static final String DEFAULT_ALGO_NAME = "AAAAAAAAAA";
    private static final String UPDATED_ALGO_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_ENDPOINT = "AAAAAAAAAA";
    private static final String UPDATED_ENDPOINT = "BBBBBBBBBB";

    private static final String DEFAULT_JS_FUNCTION = "AAAAAAAAAA";
    private static final String UPDATED_JS_FUNCTION = "BBBBBBBBBB";



    String adminToken;

    QuestionTypeDTO questionTypeDTO;

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
    public static QuestionTypeDTO createEntity() {
        var questionTypeDTO = new QuestionTypeDTO();
        questionTypeDTO.algoName = DEFAULT_ALGO_NAME;
        questionTypeDTO.endpoint = DEFAULT_ENDPOINT;
        questionTypeDTO.jsFunction = DEFAULT_JS_FUNCTION;
        return questionTypeDTO;
    }

    @BeforeEach
    public void initTest() {
        questionTypeDTO = createEntity();
    }

    @Test
    public void createQuestionType() {
        var databaseSizeBeforeCreate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/question-types")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Create the QuestionType
        questionTypeDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(questionTypeDTO)
            .when()
            .post("/api/question-types")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        // Validate the QuestionType in the database
        var questionTypeDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/question-types")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(questionTypeDTOList).hasSize(databaseSizeBeforeCreate + 1);
        var testQuestionTypeDTO = questionTypeDTOList.stream().filter(it -> questionTypeDTO.id.equals(it.id)).findFirst().get();
        assertThat(testQuestionTypeDTO.algoName).isEqualTo(DEFAULT_ALGO_NAME);
        assertThat(testQuestionTypeDTO.endpoint).isEqualTo(DEFAULT_ENDPOINT);
        assertThat(testQuestionTypeDTO.jsFunction).isEqualTo(DEFAULT_JS_FUNCTION);
    }

    @Test
    public void createQuestionTypeWithExistingId() {
        var databaseSizeBeforeCreate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/question-types")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Create the QuestionType with an existing ID
        questionTypeDTO.id = 1L;

        // An entity with an existing ID cannot be created, so this API call must fail
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(questionTypeDTO)
            .when()
            .post("/api/question-types")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());

        // Validate the QuestionType in the database
        var questionTypeDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/question-types")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(questionTypeDTOList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    public void checkAlgoNameIsRequired() throws Exception {
        var databaseSizeBeforeTest = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/question-types")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // set the field null
        questionTypeDTO.algoName = null;

        // Create the QuestionType, which fails.
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(questionTypeDTO)
            .when()
            .post("/api/question-types")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());

        // Validate the QuestionType in the database
        var questionTypeDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/question-types")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(questionTypeDTOList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void updateQuestionType() {
        // Initialize the database
        questionTypeDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(questionTypeDTO)
            .when()
            .post("/api/question-types")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var databaseSizeBeforeUpdate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/question-types")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Get the questionType
        var updatedQuestionTypeDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/question-types/{id}", questionTypeDTO.id)
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().body().as(ENTITY_TYPE);

        // Update the questionType
        updatedQuestionTypeDTO.algoName = UPDATED_ALGO_NAME;
        updatedQuestionTypeDTO.endpoint = UPDATED_ENDPOINT;
        updatedQuestionTypeDTO.jsFunction = UPDATED_JS_FUNCTION;

        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(updatedQuestionTypeDTO)
            .when()
            .put("/api/question-types")
            .then()
            .statusCode(OK.getStatusCode());

        // Validate the QuestionType in the database
        var questionTypeDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/question-types")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(questionTypeDTOList).hasSize(databaseSizeBeforeUpdate);
        var testQuestionTypeDTO = questionTypeDTOList.stream().filter(it -> updatedQuestionTypeDTO.id.equals(it.id)).findFirst().get();
        assertThat(testQuestionTypeDTO.algoName).isEqualTo(UPDATED_ALGO_NAME);
        assertThat(testQuestionTypeDTO.endpoint).isEqualTo(UPDATED_ENDPOINT);
        assertThat(testQuestionTypeDTO.jsFunction).isEqualTo(UPDATED_JS_FUNCTION);
    }

    @Test
    public void updateNonExistingQuestionType() {
        var databaseSizeBeforeUpdate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/question-types")
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
            .body(questionTypeDTO)
            .when()
            .put("/api/question-types")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());

        // Validate the QuestionType in the database
        var questionTypeDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/question-types")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(questionTypeDTOList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    public void deleteQuestionType() {
        // Initialize the database
        questionTypeDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(questionTypeDTO)
            .when()
            .post("/api/question-types")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var databaseSizeBeforeDelete = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/question-types")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Delete the questionType
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .delete("/api/question-types/{id}", questionTypeDTO.id)
            .then()
            .statusCode(NO_CONTENT.getStatusCode());

        // Validate the database contains one less item
        var questionTypeDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/question-types")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(questionTypeDTOList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void getAllQuestionTypes() {
        // Initialize the database
        questionTypeDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(questionTypeDTO)
            .when()
            .post("/api/question-types")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        // Get all the questionTypeList
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/question-types?sort=id,desc")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .body("id", hasItem(questionTypeDTO.id.intValue()))
            .body("algoName", hasItem(DEFAULT_ALGO_NAME))            .body("endpoint", hasItem(DEFAULT_ENDPOINT))            .body("jsFunction", hasItem(DEFAULT_JS_FUNCTION));
    }

    @Test
    public void getQuestionType() {
        // Initialize the database
        questionTypeDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(questionTypeDTO)
            .when()
            .post("/api/question-types")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var response = // Get the questionType
            given()
                .auth()
                .preemptive()
                .oauth2(adminToken)
                .accept(APPLICATION_JSON)
                .when()
                .get("/api/question-types/{id}", questionTypeDTO.id)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ENTITY_TYPE);

        // Get the questionType
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/question-types/{id}", questionTypeDTO.id)
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .body("id", is(questionTypeDTO.id.intValue()))

                .body("algoName", is(DEFAULT_ALGO_NAME))
                .body("endpoint", is(DEFAULT_ENDPOINT))
                .body("jsFunction", is(DEFAULT_JS_FUNCTION));
    }

    @Test
    public void getNonExistingQuestionType() {
        // Get the questionType
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/question-types/{id}", Long.MAX_VALUE)
            .then()
            .statusCode(NOT_FOUND.getStatusCode());
    }
}
