package fr.istic.web.rest;

import static io.restassured.RestAssured.given;
import static io.restassured.config.ObjectMapperConfig.objectMapperConfig;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

import fr.istic.TestUtil;
import fr.istic.service.dto.GradedCommentDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import liquibase.Liquibase;
import io.quarkus.liquibase.LiquibaseFactory;
import org.junit.jupiter.api.*;

import jakarta.inject.Inject;

import java.util.List;

@QuarkusTest
public class GradedCommentResourceTest {

    private static final TypeRef<GradedCommentDTO> ENTITY_TYPE = new TypeRef<>() {
    };

    private static final TypeRef<List<GradedCommentDTO>> LIST_OF_ENTITY_TYPE = new TypeRef<>() {
    };

    private static final String DEFAULT_ZONEGENERATEDID = "AAAAAAAAAA";
    private static final String UPDATED_ZONEGENERATEDID = "BBBBBBBBBB";

    private static final String DEFAULT_TEXT = "AAAAAAAAAA";
    private static final String UPDATED_TEXT = "BBBBBBBBBB";

    private static final Double DEFAULT_GRADE = 1.0;
    private static final Double UPDATED_GRADE = 2.0;



    String adminToken;

    GradedCommentDTO gradedCommentDTO;

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
    public static GradedCommentDTO createEntity() {
        var gradedCommentDTO = new GradedCommentDTO();
        gradedCommentDTO.zonegeneratedid = DEFAULT_ZONEGENERATEDID;
        gradedCommentDTO.text = DEFAULT_TEXT;
        gradedCommentDTO.grade = DEFAULT_GRADE;
        return gradedCommentDTO;
    }

    @BeforeEach
    public void initTest() {
        gradedCommentDTO = createEntity();
    }

    @Test
    public void createGradedComment() {
        var databaseSizeBeforeCreate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/graded-comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Create the GradedComment
        gradedCommentDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(gradedCommentDTO)
            .when()
            .post("/api/graded-comments")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        // Validate the GradedComment in the database
        var gradedCommentDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/graded-comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(gradedCommentDTOList).hasSize(databaseSizeBeforeCreate + 1);
        var testGradedCommentDTO = gradedCommentDTOList.stream().filter(it -> gradedCommentDTO.id.equals(it.id)).findFirst().get();
        assertThat(testGradedCommentDTO.zonegeneratedid).isEqualTo(DEFAULT_ZONEGENERATEDID);
        assertThat(testGradedCommentDTO.text).isEqualTo(DEFAULT_TEXT);
        assertThat(testGradedCommentDTO.grade).isEqualTo(DEFAULT_GRADE);
    }

    @Test
    public void createGradedCommentWithExistingId() {
        var databaseSizeBeforeCreate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/graded-comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Create the GradedComment with an existing ID
        gradedCommentDTO.id = 1L;

        // An entity with an existing ID cannot be created, so this API call must fail
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(gradedCommentDTO)
            .when()
            .post("/api/graded-comments")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());

        // Validate the GradedComment in the database
        var gradedCommentDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/graded-comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(gradedCommentDTOList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    public void updateGradedComment() {
        // Initialize the database
        gradedCommentDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(gradedCommentDTO)
            .when()
            .post("/api/graded-comments")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var databaseSizeBeforeUpdate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/graded-comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Get the gradedComment
        var updatedGradedCommentDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/graded-comments/{id}", gradedCommentDTO.id)
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().body().as(ENTITY_TYPE);

        // Update the gradedComment
        updatedGradedCommentDTO.zonegeneratedid = UPDATED_ZONEGENERATEDID;
        updatedGradedCommentDTO.text = UPDATED_TEXT;
        updatedGradedCommentDTO.grade = UPDATED_GRADE;

        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(updatedGradedCommentDTO)
            .when()
            .put("/api/graded-comments")
            .then()
            .statusCode(OK.getStatusCode());

        // Validate the GradedComment in the database
        var gradedCommentDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/graded-comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(gradedCommentDTOList).hasSize(databaseSizeBeforeUpdate);
        var testGradedCommentDTO = gradedCommentDTOList.stream().filter(it -> updatedGradedCommentDTO.id.equals(it.id)).findFirst().get();
        assertThat(testGradedCommentDTO.zonegeneratedid).isEqualTo(UPDATED_ZONEGENERATEDID);
        assertThat(testGradedCommentDTO.text).isEqualTo(UPDATED_TEXT);
        assertThat(testGradedCommentDTO.grade).isEqualTo(UPDATED_GRADE);
    }

    @Test
    public void updateNonExistingGradedComment() {
        var databaseSizeBeforeUpdate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/graded-comments")
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
            .body(gradedCommentDTO)
            .when()
            .put("/api/graded-comments")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());

        // Validate the GradedComment in the database
        var gradedCommentDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/graded-comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(gradedCommentDTOList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    public void deleteGradedComment() {
        // Initialize the database
        gradedCommentDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(gradedCommentDTO)
            .when()
            .post("/api/graded-comments")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var databaseSizeBeforeDelete = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/graded-comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Delete the gradedComment
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .delete("/api/graded-comments/{id}", gradedCommentDTO.id)
            .then()
            .statusCode(NO_CONTENT.getStatusCode());

        // Validate the database contains one less item
        var gradedCommentDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/graded-comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(gradedCommentDTOList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void getAllGradedComments() {
        // Initialize the database
        gradedCommentDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(gradedCommentDTO)
            .when()
            .post("/api/graded-comments")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        // Get all the gradedCommentList
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/graded-comments?sort=id,desc")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .body("id", hasItem(gradedCommentDTO.id.intValue()))
                        .body("text", hasItem(DEFAULT_TEXT))
                                    .body("grade", hasItem(DEFAULT_GRADE.floatValue()));
    }

    @Test
    public void getGradedComment() {
        // Initialize the database
        gradedCommentDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(gradedCommentDTO)
            .when()
            .post("/api/graded-comments")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var response = // Get the gradedComment
            given()
                .auth()
                .preemptive()
                .oauth2(adminToken)
                .accept(APPLICATION_JSON)
                .when()
                .get("/api/graded-comments/{id}", gradedCommentDTO.id)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ENTITY_TYPE);

        // Get the gradedComment
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/graded-comments/{id}", gradedCommentDTO.id)
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .body("id", is(gradedCommentDTO.id.intValue()))

                .body("zonegeneratedid", is(DEFAULT_ZONEGENERATEDID))
                .body("text", is(DEFAULT_TEXT))
                .body("grade", is(DEFAULT_GRADE.floatValue()));
    }

    @Test
    public void getNonExistingGradedComment() {
        // Get the gradedComment
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/graded-comments/{id}", Long.MAX_VALUE)
            .then()
            .statusCode(NOT_FOUND.getStatusCode());
    }
}
