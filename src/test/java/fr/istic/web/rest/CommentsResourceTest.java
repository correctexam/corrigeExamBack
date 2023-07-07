package fr.istic.web.rest;

import static io.restassured.RestAssured.given;
import static io.restassured.config.ObjectMapperConfig.objectMapperConfig;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;

import fr.istic.TestUtil;
import fr.istic.service.dto.CommentsDTO;
import fr.istic.web.rest.vm.ManagedUserVM;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import liquibase.Liquibase;
import io.quarkus.liquibase.LiquibaseFactory;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;

import org.junit.jupiter.api.*;

import javax.inject.Inject;

import java.util.List;
import java.util.regex.Pattern;

@QuarkusTest
public class CommentsResourceTest {

    private static final TypeRef<CommentsDTO> ENTITY_TYPE = new TypeRef<>() {
    };

    private static final TypeRef<List<CommentsDTO>> LIST_OF_ENTITY_TYPE = new TypeRef<>() {
    };

    private static final String DEFAULT_ZONEGENERATEDID = "AAAAAAAAAA";
    private static final String UPDATED_ZONEGENERATEDID = "BBBBBBBBBB";

    private static final String DEFAULT_JSON_DATA = "AAAAAAAAAA";
    private static final String UPDATED_JSON_DATA = "BBBBBBBBBB";



    String adminToken;

    CommentsDTO commentsDTO;

    @Inject
    LiquibaseFactory liquibaseFactory;

    @Inject
    MockMailbox mailbox;
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
    public static CommentsDTO createEntity() {
        var commentsDTO = new CommentsDTO();
        commentsDTO.zonegeneratedid = DEFAULT_ZONEGENERATEDID;
        commentsDTO.jsonData = DEFAULT_JSON_DATA;
        return commentsDTO;
    }

    @BeforeEach
    public void initTest() {
        commentsDTO = createEntity();
    }

    @Test
    public void createComments() {
        var databaseSizeBeforeCreate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Create the Comments
        commentsDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(commentsDTO)
            .when()
            .post("/api/comments")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        // Validate the Comments in the database
        var commentsDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(commentsDTOList).hasSize(databaseSizeBeforeCreate + 1);
        var testCommentsDTO = commentsDTOList.stream().filter(it -> commentsDTO.id.equals(it.id)).findFirst().get();
        assertThat(testCommentsDTO.zonegeneratedid).isEqualTo(DEFAULT_ZONEGENERATEDID);
        assertThat(testCommentsDTO.jsonData).isEqualTo(DEFAULT_JSON_DATA);
    }

    @Test
    public void createCommentsWithExistingId() {
        var databaseSizeBeforeCreate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Create the Comments with an existing ID
        commentsDTO.id = 1L;

        // An entity with an existing ID cannot be created, so this API call must fail
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(commentsDTO)
            .when()
            .post("/api/comments")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());

        // Validate the Comments in the database
        var commentsDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(commentsDTOList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    public void updateComments() {
        // Initialize the database
        commentsDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(commentsDTO)
            .when()
            .post("/api/comments")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var databaseSizeBeforeUpdate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Get the comments
        var updatedCommentsDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/comments/{id}", commentsDTO.id)
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().body().as(ENTITY_TYPE);

        // Update the comments
        updatedCommentsDTO.zonegeneratedid = UPDATED_ZONEGENERATEDID;
        updatedCommentsDTO.jsonData = UPDATED_JSON_DATA;

        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(updatedCommentsDTO)
            .when()
            .put("/api/comments")
            .then()
            .statusCode(OK.getStatusCode());

        // Validate the Comments in the database
        var commentsDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(commentsDTOList).hasSize(databaseSizeBeforeUpdate);
        var testCommentsDTO = commentsDTOList.stream().filter(it -> updatedCommentsDTO.id.equals(it.id)).findFirst().get();
        assertThat(testCommentsDTO.zonegeneratedid).isEqualTo(UPDATED_ZONEGENERATEDID);
        assertThat(testCommentsDTO.jsonData).isEqualTo(UPDATED_JSON_DATA);
    }

    @Test
    public void updateNonExistingComments() {
        var databaseSizeBeforeUpdate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/comments")
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
            .body(commentsDTO)
            .when()
            .put("/api/comments")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());

        // Validate the Comments in the database
        var commentsDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(commentsDTOList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    public void deleteComments() {
        // Initialize the database
        commentsDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(commentsDTO)
            .when()
            .post("/api/comments")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var databaseSizeBeforeDelete = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Delete the comments
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .delete("/api/comments/{id}", commentsDTO.id)
            .then()
            .statusCode(NO_CONTENT.getStatusCode());

        // Validate the database contains one less item
        var commentsDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(commentsDTOList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void getAllComments() {
        // Initialize the database
        commentsDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(commentsDTO)
            .when()
            .post("/api/comments")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        // Get all the commentsList
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/comments?sort=id,desc")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .body("id", hasItem(commentsDTO.id.intValue()))
            .body("zonegeneratedid", hasItem(DEFAULT_ZONEGENERATEDID))            .body("jsonData", hasItem(DEFAULT_JSON_DATA));
    }

    @Test
    public void getComments() {
        // Initialize the database
        commentsDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(commentsDTO)
            .when()
            .post("/api/comments")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var response = // Get the comments
            given()
                .auth()
                .preemptive()
                .oauth2(adminToken)
                .accept(APPLICATION_JSON)
                .when()
                .get("/api/comments/{id}", commentsDTO.id)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ENTITY_TYPE);

        // Get the comments
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/comments/{id}", commentsDTO.id)
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .body("id", is(commentsDTO.id.intValue()))

                .body("zonegeneratedid", is(DEFAULT_ZONEGENERATEDID))
                .body("jsonData", is(DEFAULT_JSON_DATA));
    }

    @Test
    public void getNonExistingComments() {
        // Get the comments
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/comments/{id}", Long.MAX_VALUE)
            .then()
            .statusCode(NOT_FOUND.getStatusCode());
    }
}
