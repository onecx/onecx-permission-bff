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
import org.tkit.onecx.permission.bff.rs.controllers.AssignmentRestController;

import gen.org.tkit.onecx.permission.bff.rs.internal.model.*;
import gen.org.tkit.onecx.permission.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(AssignmentRestController.class)
class AssignmentRestControllerTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @Test
    void createAssignmentTest() {
        CreateAssignmentRequest request = new CreateAssignmentRequest();
        request.permissionId("permission1").roleId("role1");

        Assignment response = new Assignment();
        response.roleId("role1").permissionId("permission1");

        mockServerClient.when(request().withPath("/internal/assignments").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(response)));

        CreateAssignmentRequestDTO input = new CreateAssignmentRequestDTO();
        input.roleId("role1").permissionId("permission1");

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(input)
                .post()
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(AssignmentDTO.class);

        // standard USER get FORBIDDEN with only READ permission
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(USER))
                .header(APM_HEADER_PARAM, USER)
                .contentType(APPLICATION_JSON)
                .body(input)
                .post()
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        Assertions.assertNotNull(output);
        Assertions.assertEquals(request.getRoleId(), output.getRoleId());
        Assertions.assertEquals(request.getPermissionId(), output.getPermissionId());
        mockServerClient.clear(MOCKID);
    }

    @Test
    void createAssignmentEmptyBodyTest() {
        CreateAssignmentRequest request = new CreateAssignmentRequest();

        mockServerClient.when(request().withPath("/internal/assignments").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        CreateAssignmentRequestDTO input = new CreateAssignmentRequestDTO();

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(input)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        mockServerClient.clear(MOCKID);
    }

    @Test
    void searchAssignmentByCriteriaTest() {
        AssignmentSearchCriteria criteria = new AssignmentSearchCriteria();
        criteria.pageNumber(1).pageSize(1).appId(List.of("app1"));

        AssignmentPageResult pageResult = new AssignmentPageResult();
        Assignment assignment = new Assignment();
        assignment.permissionId("permission1").roleId("role1");
        pageResult.stream(List.of(assignment)).size(1).number(1).totalElements(1L).totalPages(1L);

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/assignments/search").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(criteria)))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(pageResult)));

        AssignmentSearchCriteriaDTO criteriaDTO = new AssignmentSearchCriteriaDTO();
        criteriaDTO.pageNumber(1).appIds(List.of("app1")).pageSize(1);

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
                .extract().as(AssignmentPageResultDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(pageResult.getSize(), output.getSize());
        Assertions.assertEquals(pageResult.getStream().size(), output.getStream().size());
        Assertions.assertEquals(pageResult.getStream().get(0).getRoleId(), output.getStream().get(0).getRoleId());
        Assertions.assertEquals(pageResult.getStream().get(0).getPermissionId(), output.getStream().get(0).getPermissionId());

        mockServerClient.clear(MOCKID);
    }

    @Test
    void searchAssignmentsByEmptyCriteriaTest() {

        AssignmentSearchCriteria criteria = new AssignmentSearchCriteria();

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/assignments/search").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(criteria)))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        AssignmentSearchCriteriaDTO criteriaDTO = new AssignmentSearchCriteriaDTO();

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
    void getAssignmentByIdTest() {

        Assignment data = new Assignment();
        data.roleId("role1").permissionId("permission1");
        String id = "assignment1";

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/assignments/" + id).withMethod(HttpMethod.GET))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", id)
                .get("/{id}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(AssignmentDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(data.getRoleId(), output.getRoleId());
        Assertions.assertEquals(data.getPermissionId(), output.getPermissionId());

        mockServerClient.clear(MOCKID);
    }

    @Test
    void getAssignmentByIdNotFoundTest() {
        String notFoundId = "notFound";
        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/assignments/" + notFoundId).withMethod(HttpMethod.GET))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode()));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", notFoundId)
                .get("/{id}")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
        Assertions.assertNotNull(output);

        mockServerClient.clear(MOCKID);
    }

    @Test
    void deleteAssignmentTest() {

        String id = "test-id-1";

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/assignments/" + id).withMethod(HttpMethod.DELETE))
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", id)
                .delete("/{id}")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void createProductAssignmentsTest() {

        CreateProductAssignmentRequest request = new CreateProductAssignmentRequest();
        request.setRoleId("role1");
        request.setProductNames(List.of("product1"));

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/assignments/product").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        CreateProductAssignmentsRequestDTO requestDTO = new CreateProductAssignmentsRequestDTO();
        requestDTO.setRoleId("role1");
        requestDTO.setProductNames(List.of("product1"));
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post("/product")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode());
    }

    @Test
    void revokeAssignmentsTest() {
        RevokeAssignmentRequest request = new RevokeAssignmentRequest();
        request.setRoleId("role1");
        request.setProductNames(List.of("product1"));

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/assignments/revoke").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        RevokeAssignmentRequestDTO requestDTO = new RevokeAssignmentRequestDTO();
        requestDTO.setRoleId("role1");
        requestDTO.setProductNames(List.of("product1"));
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post("/revoke")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }
}
