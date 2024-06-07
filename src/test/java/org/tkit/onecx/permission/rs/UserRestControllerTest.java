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
import org.tkit.onecx.permission.bff.rs.controllers.UserRestController;

import gen.org.tkit.onecx.permission.bff.rs.internal.model.*;
import gen.org.tkit.onecx.permission.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(UserRestController.class)
class UserRestControllerTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    final String TOKEN = keycloakClient.getAccessToken(ADMIN);

    @Test
    void getUserRoles() {
        var testToken = "Bearer " + TOKEN;
        RoleRequest roleRequest = new RoleRequest();
        roleRequest.pageNumber(0).pageSize(5).token(testToken);

        RolePageResult roleResponse = new RolePageResult();
        roleResponse.stream(List.of(new Role().name("role1")));
        mockServerClient.when(request().withPath("/internal/roles/me").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(roleRequest)))
                .withId("mock1")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(roleResponse)));

        UserCriteriaDTO criteriaDTO = new UserCriteriaDTO();
        criteriaDTO.setPageNumber(0);
        criteriaDTO.setPageSize(5);

        var output = given()
                .when()
                .auth().oauth2(TOKEN)
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .post("/roles")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(UserRolesPageResultDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(1, output.getStream().size());
        mockServerClient.clear("mock1");
    }

    @Test
    void getUserPermissions() {
        var testToken = "Bearer " + TOKEN;

        PermissionRequest permissionRequest = new PermissionRequest();
        permissionRequest.pageNumber(0).pageSize(5).token(testToken);

        PermissionPageResult permissionResponse = new PermissionPageResult();
        permissionResponse.stream(List.of(new Permission().resource("resource1").action("action1")));
        mockServerClient.when(request().withPath("/internal/permissions/me").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(permissionRequest)))
                .withId("mock2")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(permissionResponse)));

        UserCriteriaDTO criteriaDTO = new UserCriteriaDTO();
        criteriaDTO.setPageNumber(0);
        criteriaDTO.setPageSize(5);

        var output = given()
                .when()
                .auth().oauth2(TOKEN)
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .post("/permissions")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(UserPermissionsPageResultDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(1, output.getStream().size());
        mockServerClient.clear("mock2");
    }

    @Test
    void getUserAssignments() {
        var testToken = "Bearer " + TOKEN;
        AssignmentRequest assignmentRequest = new AssignmentRequest();
        assignmentRequest.pageNumber(0).pageSize(5).token(testToken);

        UserAssignmentPageResult assignmentResponse = new UserAssignmentPageResult();
        assignmentResponse.stream(List.of(new UserAssignment().roleName("role1").applicationId("app1")));
        mockServerClient.when(request().withPath("/internal/assignments/me").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(assignmentRequest)))
                .withId("mock1")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(assignmentResponse)));

        UserCriteriaDTO criteriaDTO = new UserCriteriaDTO();
        criteriaDTO.setPageNumber(0);
        criteriaDTO.setPageSize(5);

        var output = given()
                .when()
                .auth().oauth2(TOKEN)
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .post("/assignments")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(UserAssignmentPageResultDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(1, output.getStream().size());
        mockServerClient.clear("mock1");
    }

    @Test
    void getUserRoles_BAD_REQUEST() {
        var testToken = "Bearer " + TOKEN;
        RoleRequest roleRequest = new RoleRequest();
        roleRequest.pageNumber(0).pageSize(5).token(testToken);

        mockServerClient.when(request().withPath("/internal/roles/me").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(roleRequest)))
                .withId("mock1")
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        UserCriteriaDTO criteriaDTO = new UserCriteriaDTO();
        criteriaDTO.setPageNumber(0);
        criteriaDTO.setPageSize(5);

        var output = given()
                .when()
                .auth().oauth2(TOKEN)
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .post("/roles")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        Assertions.assertNotNull(output);
        mockServerClient.clear("mock1");
        mockServerClient.clear("mock2");
    }

    @Test
    void getUserPermissions_BAD_REQUEST() {
        var testToken = "Bearer " + TOKEN;

        PermissionRequest permissionRequest = new PermissionRequest();
        permissionRequest.pageNumber(0).pageSize(5).token(testToken);

        mockServerClient.when(request().withPath("/internal/permissions/me").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(permissionRequest)))
                .withId("mock2")
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        UserCriteriaDTO criteriaDTO = new UserCriteriaDTO();
        criteriaDTO.setPageNumber(0);
        criteriaDTO.setPageSize(5);

        var output = given()
                .when()
                .auth().oauth2(TOKEN)
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .post("/permissions")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        Assertions.assertNotNull(output);
        mockServerClient.clear("mock2");
    }

    @Test
    void getUserAssignments_BAD_REQUEST() {
        var testToken = "Bearer " + TOKEN;
        AssignmentRequest assignmentRequest = new AssignmentRequest();
        assignmentRequest.pageNumber(0).pageSize(5).token(testToken);

        mockServerClient.when(request().withPath("/internal/assignments/me").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(assignmentRequest)))
                .withId("mock2")
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        UserCriteriaDTO criteriaDTO = new UserCriteriaDTO();
        criteriaDTO.setPageNumber(0);
        criteriaDTO.setPageSize(5);

        var output = given()
                .when()
                .auth().oauth2(TOKEN)
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .post("/assignments")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        Assertions.assertNotNull(output);
        mockServerClient.clear("mock2");
    }
}
