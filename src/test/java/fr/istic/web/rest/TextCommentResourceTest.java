package fr.istic.web.rest;

import static io.restassured.RestAssured.given;
import static io.restassured.config.ObjectMapperConfig.objectMapperConfig;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

import fr.istic.TestUtil;
import fr.istic.service.dto.TextCommentDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import liquibase.Liquibase;
import io.quarkus.liquibase.LiquibaseFactory;
import org.junit.jupiter.api.*;

import jakarta.inject.Inject;

import java.util.List;

@QuarkusTest
public class TextCommentResourceTest {

    private static final TypeRef<TextCommentDTO> ENTITY_TYPE = new TypeRef<>() {
    };

    private static final TypeRef<List<TextCommentDTO>> LIST_OF_ENTITY_TYPE = new TypeRef<>() {
    };

    private static final String DEFAULT_TEXT = "AAAAAAAAAA";
    private static final String UPDATED_TEXT = "BBBBBBBBBB";

    private static final String DEFAULT_ZONEGENERATEDID = "AAAAAAAAAA";
    private static final String UPDATED_ZONEGENERATEDID = "BBBBBBBBBB";



    String adminToken;

    TextCommentDTO textCommentDTO;

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
    public static TextCommentDTO createEntity() {
        var textCommentDTO = new TextCommentDTO();
        textCommentDTO.text = DEFAULT_TEXT;
        textCommentDTO.zonegeneratedid = DEFAULT_ZONEGENERATEDID;
        return textCommentDTO;
    }

    @BeforeEach
    public void initTest() {
        textCommentDTO = createEntity();
    }

    @Test
    public void createTextComment() {
        var databaseSizeBeforeCreate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/text-comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Create the TextComment
        textCommentDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(textCommentDTO)
            .when()
            .post("/api/text-comments")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        // Validate the TextComment in the database
        var textCommentDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/text-comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(textCommentDTOList).hasSize(databaseSizeBeforeCreate + 1);
        var testTextCommentDTO = textCommentDTOList.stream().filter(it -> textCommentDTO.id.equals(it.id)).findFirst().get();
        assertThat(testTextCommentDTO.text).isEqualTo(DEFAULT_TEXT);
        assertThat(testTextCommentDTO.zonegeneratedid).isEqualTo(DEFAULT_ZONEGENERATEDID);
    }

    @Test
    public void createTextCommentWithExistingId() {
        var databaseSizeBeforeCreate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/text-comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Create the TextComment with an existing ID
        textCommentDTO.id = 1L;

        // An entity with an existing ID cannot be created, so this API call must fail
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(textCommentDTO)
            .when()
            .post("/api/text-comments")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());

        // Validate the TextComment in the database
        var textCommentDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/text-comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(textCommentDTOList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    public void updateTextComment() {
        // Initialize the database
        textCommentDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(textCommentDTO)
            .when()
            .post("/api/text-comments")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var databaseSizeBeforeUpdate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/text-comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Get the textComment
        var updatedTextCommentDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/text-comments/{id}", textCommentDTO.id)
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().body().as(ENTITY_TYPE);

        // Update the textComment
        updatedTextCommentDTO.text = UPDATED_TEXT;
        updatedTextCommentDTO.zonegeneratedid = UPDATED_ZONEGENERATEDID;

        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(updatedTextCommentDTO)
            .when()
            .put("/api/text-comments")
            .then()
            .statusCode(OK.getStatusCode());

        // Validate the TextComment in the database
        var textCommentDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/text-comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(textCommentDTOList).hasSize(databaseSizeBeforeUpdate);
        var testTextCommentDTO = textCommentDTOList.stream().filter(it -> updatedTextCommentDTO.id.equals(it.id)).findFirst().get();
        assertThat(testTextCommentDTO.text).isEqualTo(UPDATED_TEXT);
        assertThat(testTextCommentDTO.zonegeneratedid).isEqualTo(UPDATED_ZONEGENERATEDID);
    }

    @Test
    public void updateNonExistingTextComment() {
        var databaseSizeBeforeUpdate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/text-comments")
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
            .body(textCommentDTO)
            .when()
            .put("/api/text-comments")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());

        // Validate the TextComment in the database
        var textCommentDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/text-comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(textCommentDTOList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    public void deleteTextComment() {
        // Initialize the database
        textCommentDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(textCommentDTO)
            .when()
            .post("/api/text-comments")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var databaseSizeBeforeDelete = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/text-comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Delete the textComment
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .delete("/api/text-comments/{id}", textCommentDTO.id)
            .then()
            .statusCode(NO_CONTENT.getStatusCode());

        // Validate the database contains one less item
        var textCommentDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/text-comments")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(textCommentDTOList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void getAllTextComments() {
        // Initialize the database
        textCommentDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(textCommentDTO)
            .when()
            .post("/api/text-comments")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        // Get all the textCommentList
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/text-comments?sort=id,desc")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .body("id", hasItem(textCommentDTO.id.intValue()))
            .body("text", hasItem(DEFAULT_TEXT))            .body("zonegeneratedid", hasItem(DEFAULT_ZONEGENERATEDID));
    }

    @Test
    public void getTextComment() {
        // Initialize the database
        textCommentDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(textCommentDTO)
            .when()
            .post("/api/text-comments")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var response = // Get the textComment
            given()
                .auth()
                .preemptive()
                .oauth2(adminToken)
                .accept(APPLICATION_JSON)
                .when()
                .get("/api/text-comments/{id}", textCommentDTO.id)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ENTITY_TYPE);

        // Get the textComment
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/text-comments/{id}", textCommentDTO.id)
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .body("id", is(textCommentDTO.id.intValue()))

                .body("text", is(DEFAULT_TEXT))
                .body("zonegeneratedid", is(DEFAULT_ZONEGENERATEDID));
    }

    @Test
    public void getNonExistingTextComment() {
        // Get the textComment
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/text-comments/{id}", Long.MAX_VALUE)
            .then()
            .statusCode(NOT_FOUND.getStatusCode());
    }
}
