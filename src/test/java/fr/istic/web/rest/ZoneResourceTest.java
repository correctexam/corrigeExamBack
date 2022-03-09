package fr.istic.web.rest;

import static io.restassured.RestAssured.given;
import static io.restassured.config.ObjectMapperConfig.objectMapperConfig;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

import fr.istic.TestUtil;
import fr.istic.service.dto.ZoneDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import liquibase.Liquibase;
import io.quarkus.liquibase.LiquibaseFactory;
import org.junit.jupiter.api.*;

import javax.inject.Inject;

import java.util.List;
    
@QuarkusTest
public class ZoneResourceTest {

    private static final TypeRef<ZoneDTO> ENTITY_TYPE = new TypeRef<>() {
    };

    private static final TypeRef<List<ZoneDTO>> LIST_OF_ENTITY_TYPE = new TypeRef<>() {
    };

    private static final Integer DEFAULT_X_INIT = 1;
    private static final Integer UPDATED_X_INIT = 2;

    private static final Integer DEFAULT_Y_INIT = 1;
    private static final Integer UPDATED_Y_INIT = 2;

    private static final Integer DEFAULT_X_FINAL = 1;
    private static final Integer UPDATED_X_FINAL = 2;

    private static final Integer DEFAULT_Y_FINAL = 1;
    private static final Integer UPDATED_Y_FINAL = 2;



    String adminToken;

    ZoneDTO zoneDTO;

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
    public static ZoneDTO createEntity() {
        var zoneDTO = new ZoneDTO();
        zoneDTO.xInit = DEFAULT_X_INIT;
        zoneDTO.yInit = DEFAULT_Y_INIT;
        zoneDTO.xFinal = DEFAULT_X_FINAL;
        zoneDTO.yFinal = DEFAULT_Y_FINAL;
        return zoneDTO;
    }

    @BeforeEach
    public void initTest() {
        zoneDTO = createEntity();
    }

    @Test
    public void createZone() {
        var databaseSizeBeforeCreate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/zones")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Create the Zone
        zoneDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(zoneDTO)
            .when()
            .post("/api/zones")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        // Validate the Zone in the database
        var zoneDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/zones")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(zoneDTOList).hasSize(databaseSizeBeforeCreate + 1);
        var testZoneDTO = zoneDTOList.stream().filter(it -> zoneDTO.id.equals(it.id)).findFirst().get();
        assertThat(testZoneDTO.xInit).isEqualTo(DEFAULT_X_INIT);
        assertThat(testZoneDTO.yInit).isEqualTo(DEFAULT_Y_INIT);
        assertThat(testZoneDTO.xFinal).isEqualTo(DEFAULT_X_FINAL);
        assertThat(testZoneDTO.yFinal).isEqualTo(DEFAULT_Y_FINAL);
    }

    @Test
    public void createZoneWithExistingId() {
        var databaseSizeBeforeCreate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/zones")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Create the Zone with an existing ID
        zoneDTO.id = 1L;

        // An entity with an existing ID cannot be created, so this API call must fail
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(zoneDTO)
            .when()
            .post("/api/zones")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());

        // Validate the Zone in the database
        var zoneDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/zones")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(zoneDTOList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    public void updateZone() {
        // Initialize the database
        zoneDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(zoneDTO)
            .when()
            .post("/api/zones")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var databaseSizeBeforeUpdate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/zones")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Get the zone
        var updatedZoneDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/zones/{id}", zoneDTO.id)
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().body().as(ENTITY_TYPE);

        // Update the zone
        updatedZoneDTO.xInit = UPDATED_X_INIT;
        updatedZoneDTO.yInit = UPDATED_Y_INIT;
        updatedZoneDTO.xFinal = UPDATED_X_FINAL;
        updatedZoneDTO.yFinal = UPDATED_Y_FINAL;

        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(updatedZoneDTO)
            .when()
            .put("/api/zones")
            .then()
            .statusCode(OK.getStatusCode());

        // Validate the Zone in the database
        var zoneDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/zones")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(zoneDTOList).hasSize(databaseSizeBeforeUpdate);
        var testZoneDTO = zoneDTOList.stream().filter(it -> updatedZoneDTO.id.equals(it.id)).findFirst().get();
        assertThat(testZoneDTO.xInit).isEqualTo(UPDATED_X_INIT);
        assertThat(testZoneDTO.yInit).isEqualTo(UPDATED_Y_INIT);
        assertThat(testZoneDTO.xFinal).isEqualTo(UPDATED_X_FINAL);
        assertThat(testZoneDTO.yFinal).isEqualTo(UPDATED_Y_FINAL);
    }

    @Test
    public void updateNonExistingZone() {
        var databaseSizeBeforeUpdate = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/zones")
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
            .body(zoneDTO)
            .when()
            .put("/api/zones")
            .then()
            .statusCode(BAD_REQUEST.getStatusCode());

        // Validate the Zone in the database
        var zoneDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/zones")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(zoneDTOList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    public void deleteZone() {
        // Initialize the database
        zoneDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(zoneDTO)
            .when()
            .post("/api/zones")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var databaseSizeBeforeDelete = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/zones")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE)
            .size();

        // Delete the zone
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .delete("/api/zones/{id}", zoneDTO.id)
            .then()
            .statusCode(NO_CONTENT.getStatusCode());

        // Validate the database contains one less item
        var zoneDTOList = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/zones")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .extract().as(LIST_OF_ENTITY_TYPE);

        assertThat(zoneDTOList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void getAllZones() {
        // Initialize the database
        zoneDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(zoneDTO)
            .when()
            .post("/api/zones")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        // Get all the zoneList
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/zones?sort=id,desc")
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .body("id", hasItem(zoneDTO.id.intValue()))
            .body("xInit", hasItem(DEFAULT_X_INIT.intValue()))            .body("yInit", hasItem(DEFAULT_Y_INIT.intValue()))            .body("xFinal", hasItem(DEFAULT_X_FINAL.intValue()))            .body("yFinal", hasItem(DEFAULT_Y_FINAL.intValue()));
    }

    @Test
    public void getZone() {
        // Initialize the database
        zoneDTO = given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .body(zoneDTO)
            .when()
            .post("/api/zones")
            .then()
            .statusCode(CREATED.getStatusCode())
            .extract().as(ENTITY_TYPE);

        var response = // Get the zone
            given()
                .auth()
                .preemptive()
                .oauth2(adminToken)
                .accept(APPLICATION_JSON)
                .when()
                .get("/api/zones/{id}", zoneDTO.id)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ENTITY_TYPE);

        // Get the zone
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/zones/{id}", zoneDTO.id)
            .then()
            .statusCode(OK.getStatusCode())
            .contentType(APPLICATION_JSON)
            .body("id", is(zoneDTO.id.intValue()))
            
                .body("xInit", is(DEFAULT_X_INIT.intValue()))
                .body("yInit", is(DEFAULT_Y_INIT.intValue()))
                .body("xFinal", is(DEFAULT_X_FINAL.intValue()))
                .body("yFinal", is(DEFAULT_Y_FINAL.intValue()));
    }

    @Test
    public void getNonExistingZone() {
        // Get the zone
        given()
            .auth()
            .preemptive()
            .oauth2(adminToken)
            .accept(APPLICATION_JSON)
            .when()
            .get("/api/zones/{id}", Long.MAX_VALUE)
            .then()
            .statusCode(NOT_FOUND.getStatusCode());
    }
}
