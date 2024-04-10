package org.tkit.onecx.permission.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.List;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.permission.bff.rs.controllers.PermissionRestController;

import gen.org.tkit.onecx.permission.bff.rs.internal.model.*;
import gen.org.tkit.onecx.permission.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(PermissionRestController.class)
class PermissionRestControllerTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @Test
    void searchPermissionsByCriteriaTest() {

        PermissionSearchCriteria criteria = new PermissionSearchCriteria();
        criteria.pageNumber(1).appId("app1").pageSize(1);
        criteria.setProductNames(List.of("product1"));

        PermissionPageResult pageResult = new PermissionPageResult();
        Permission permission = new Permission();
        permission.appId("app1").action("delete").id("id1");
        permission.setProductName("product1");
        pageResult.stream(List.of(permission)).size(1).number(1).totalElements(1L).totalPages(1L);

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/permissions/search").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(criteria)))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(pageResult)));

        PermissionSearchCriteriaDTO criteriaDTO = new PermissionSearchCriteriaDTO();
        criteriaDTO.pageNumber(1).appId("app1").pageSize(1).productNames(List.of("product1"));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .post("/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(PermissionPageResultDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(pageResult.getSize(), output.getSize());
        Assertions.assertEquals(pageResult.getStream().size(), output.getStream().size());
        Assertions.assertEquals(pageResult.getStream().get(0).getAppId(), output.getStream().get(0).getAppId());
        mockServerClient.clear(MOCKID);
    }

    @Test
    void searchPermissionsByEmptyCriteriaTest() {

        PermissionSearchCriteria criteria = new PermissionSearchCriteria();

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/permissions/search").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(criteria)))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        PermissionSearchCriteriaDTO criteriaDTO = new PermissionSearchCriteriaDTO();

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .post("/search")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        mockServerClient.clear(MOCKID);
    }

    @Test
    void getPermissionTest() {

        Permission permission = new Permission();
        permission.appId("app1").action("delete").id("id1");
        permission.setProductName("product1");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/permissions/id1").withMethod(HttpMethod.GET))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(permission)));

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .get("/id1")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        mockServerClient.clear(MOCKID);
    }

    @Test
    void createPermissionTest() {

        Permission permission = new Permission();
        permission.appId("testAppId1").action("CREATE").id("id1");
        permission.setProductName("product1");

        CreatePermissionRequest createPermissionRequest = new CreatePermissionRequest();
        createPermissionRequest.action("CREATE").appId("testAppId1");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/permissions").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(createPermissionRequest)))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(permission)));

        CreatePermissionRequestDTO createPermissionRequestDTO = new CreatePermissionRequestDTO();
        createPermissionRequestDTO.action("CREATE").appId("testAppId1").productName("product1");

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(createPermissionRequestDTO)
                .post()
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(PermissionDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(permission.getAppId(), output.getAppId());
        Assertions.assertEquals(permission.getAction(), output.getAction());
        Assertions.assertEquals(permission.getProductName(), output.getProductName());

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponse.class);

        mockServerClient.clear(MOCKID);
    }

    @Test
    void updatePermissionTest() {
        Permission permission = new Permission();
        permission.appId("testAppId1").action("PUT").id("id1");
        permission.setProductName("product1");

        UpdatePermissionRequest updatePermissionRequest = new UpdatePermissionRequest();
        updatePermissionRequest.action("PUT").appId("testAppId1");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/permissions/id1").withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(updatePermissionRequest)))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(permission)));

        UpdatePermissionRequestDTO updatePermissionRequestDTO = new UpdatePermissionRequestDTO();
        updatePermissionRequestDTO.action("PUT").appId("testAppId1").productName("product1").modificationCount(0);

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(updatePermissionRequestDTO)
                .put("/id1")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(PermissionDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(permission.getAppId(), output.getAppId());
        Assertions.assertEquals(permission.getAction(), output.getAction());
        Assertions.assertEquals(permission.getProductName(), output.getProductName());

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .put("/id1")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponse.class);

        mockServerClient.clear(MOCKID);
    }

    @Test
    void updatePermissionWithoutModificationCountTest() {
        Permission permission = new Permission();
        permission.appId("testAppId1").action("PUT").id("id1");
        permission.setProductName("product1");

        UpdatePermissionRequest updatePermissionRequest = new UpdatePermissionRequest();
        updatePermissionRequest.action("PUT").appId("testAppId1");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/permissions/id1").withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(updatePermissionRequest)))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(permission)));

        UpdatePermissionRequestDTO updatePermissionRequestDTO = new UpdatePermissionRequestDTO();
        updatePermissionRequestDTO.action("PUT").appId("testAppId1").productName("product1");

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(updatePermissionRequestDTO)
                .put("/id1")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponse.class);

        mockServerClient.clear(MOCKID);
    }

    @Test
    void deletePermissionTest() {

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/permissions/id1").withMethod(HttpMethod.DELETE))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode()));

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .delete("/id1")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        mockServerClient.clear(MOCKID);
    }
}
