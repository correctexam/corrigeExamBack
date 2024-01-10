package fr.istic.web.rest;

import static io.restassured.RestAssured.given;
import static io.restassured.config.ObjectMapperConfig.objectMapperConfig;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

import fr.istic.TestUtil;
import fr.istic.service.dto.ExamSheetDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import liquibase.Liquibase;
import io.quarkus.liquibase.LiquibaseFactory;
import org.junit.jupiter.api.*;

import jakarta.inject.Inject;

import java.util.List;

@QuarkusTest
public class ExamSheetResourceTest {

    private static final TypeRef<ExamSheetDTO> ENTITY_TYPE = new TypeRef<>() {
    };

    private static final TypeRef<List<ExamSheetDTO>> LIST_OF_ENTITY_TYPE = new TypeRef<>() {
    };

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Integer DEFAULT_PAGEMIN = 1;
    private static final Integer UPDATED_PAGEMIN = 2;

    private static final Integer DEFAULT_PAGEMAX = 1;
    private static final Integer UPDATED_PAGEMAX = 2;



    String adminToken;

    ExamSheetDTO examSheetDTO;

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
    public static ExamSheetDTO createEntity() {
        var examSheetDTO = new ExamSheetDTO();
        examSheetDTO.name = DEFAULT_NAME;
        examSheetDTO.pagemin = DEFAULT_PAGEMIN;
        examSheetDTO.pagemax = DEFAULT_PAGEMAX;
        return examSheetDTO;
    }

    @BeforeEach
    public void initTest() {
        examSheetDTO = createEntity();
    }

    @Test
    public void createExamSheet() {
        var databaseSizeBeforeCreate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/exam-sheets")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Create the ExamSheet
        examSheetDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(examSheetDTO)
            .when()
            .post("/api/exam-sheets")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        // Validate the ExamSheet in the database
        var examSheetDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/exam-sheets")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(examSheetDTOList).hasSize(databaseSizeBeforeCreate + 1);
        var testExamSheetDTO = examSheetDTOList.stream().filter(it -> examSheetDTO.id.equals(it.id)).findFirst().get();
        assertThat(testExamSheetDTO.name).isEqualTo(DEFAULT_NAME);
        assertThat(testExamSheetDTO.pagemin).isEqualTo(DEFAULT_PAGEMIN);
        assertThat(testExamSheetDTO.pagemax).isEqualTo(DEFAULT_PAGEMAX);
    }

    @Test
    public void createExamSheetWithExistingId() {
        var databaseSizeBeforeCreate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/exam-sheets")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Create the ExamSheet with an existing ID
        examSheetDTO.id = 1L;

        // An entity with an existing ID cannot be created, so this API call must fail
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(examSheetDTO)
            .when()
            .post("/api/exam-sheets")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());

        // Validate the ExamSheet in the database
        var examSheetDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/exam-sheets")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(examSheetDTOList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    public void checkNameIsRequired() throws Exception {
        var databaseSizeBeforeTest = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/exam-sheets")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // set the field null
        examSheetDTO.name = null;

        // Create the ExamSheet, which fails.
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(examSheetDTO)
            .when()
            .post("/api/exam-sheets")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());

        // Validate the ExamSheet in the database
        var examSheetDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/exam-sheets")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(examSheetDTOList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void updateExamSheet() {
        // Initialize the database
        examSheetDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(examSheetDTO)
            .when()
            .post("/api/exam-sheets")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var databaseSizeBeforeUpdate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/exam-sheets")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Get the examSheet
        var updatedExamSheetDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/exam-sheets/{id}", examSheetDTO.id)
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().body().as(ENTITY_TYPE);

        // Update the examSheet
        updatedExamSheetDTO.name = UPDATED_NAME;
        updatedExamSheetDTO.pagemin = UPDATED_PAGEMIN;
        updatedExamSheetDTO.pagemax = UPDATED_PAGEMAX;

        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(updatedExamSheetDTO)
            .when()
            .put("/api/exam-sheets")
            .then()
            .statusCode(OK.getStatusCode());

        // Validate the ExamSheet in the database
        var examSheetDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/exam-sheets")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(examSheetDTOList).hasSize(databaseSizeBeforeUpdate);
        var testExamSheetDTO = examSheetDTOList.stream().filter(it -> updatedExamSheetDTO.id.equals(it.id)).findFirst().get();
        assertThat(testExamSheetDTO.name).isEqualTo(UPDATED_NAME);
        assertThat(testExamSheetDTO.pagemin).isEqualTo(UPDATED_PAGEMIN);
        assertThat(testExamSheetDTO.pagemax).isEqualTo(UPDATED_PAGEMAX);
    }

    @Test
    public void updateNonExistingExamSheet() {
        var databaseSizeBeforeUpdate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/exam-sheets")
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
            .body(examSheetDTO)
            .when()
            .put("/api/exam-sheets")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());

        // Validate the ExamSheet in the database
        var examSheetDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/exam-sheets")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(examSheetDTOList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    public void deleteExamSheet() {
        // Initialize the database
        examSheetDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(examSheetDTO)
            .when()
            .post("/api/exam-sheets")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var databaseSizeBeforeDelete = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/exam-sheets")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Delete the examSheet
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .delete("/api/exam-sheets/{id}", examSheetDTO.id)
            .then()
            .statusCode(NO_CONTENT.getStatusCode());

        // Validate the database contains one less item
        var examSheetDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/exam-sheets")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(examSheetDTOList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void getAllExamSheets() {
        // Initialize the database
        examSheetDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(examSheetDTO)
            .when()
            .post("/api/exam-sheets")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        // Get all the examSheetList
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/exam-sheets?sort=id,desc")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .body("id", hasItem(examSheetDTO.id.intValue()))
            .body("name", hasItem(DEFAULT_NAME))            .body("pagemin", hasItem(DEFAULT_PAGEMIN.intValue()))            .body("pagemax", hasItem(DEFAULT_PAGEMAX.intValue()));
    }

    @Test
    public void getExamSheet() {
        // Initialize the database
        examSheetDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(examSheetDTO)
            .when()
            .post("/api/exam-sheets")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var response = // Get the examSheet
            given()
                .auth()
                .preemptive()
                .oauth2(adminToken)
                .accept(APPLICATION_JSON)
                .when()
                .get("/api/exam-sheets/{id}", examSheetDTO.id)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ENTITY_TYPE);

        // Get the examSheet
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/exam-sheets/{id}", examSheetDTO.id)
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .body("id", is(examSheetDTO.id.intValue()))

                .body("name", is(DEFAULT_NAME))
                .body("pagemin", is(DEFAULT_PAGEMIN.intValue()))
                .body("pagemax", is(DEFAULT_PAGEMAX.intValue()));
    }

    @Test
    public void getNonExistingExamSheet() {
        // Get the examSheet
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/exam-sheets/{id}", Long.MAX_VALUE)
            .then()
            .statusCode(NOT_FOUND.getStatusCode());
    }
}
