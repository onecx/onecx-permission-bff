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

import gen.org.tkit.onecx.permission.bff.rs.internal.model.UserRolesAndPermissionsCriteriaDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.UserRolesAndPermissionsPageResultDTO;
import gen.org.tkit.onecx.permission.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(UserRestController.class)
class UserRestControllerTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @Test
    void getUserRolesAndPermissions() {
        var testToken = "someToken";
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

        UserRolesAndPermissionsCriteriaDTO criteriaDTO = new UserRolesAndPermissionsCriteriaDTO();
        criteriaDTO.setPermissionsPageNumber(0);
        criteriaDTO.setPermissionsPageSize(5);
        criteriaDTO.setRolesPageNumber(0);
        criteriaDTO.setRolesPageSize(5);
        criteriaDTO.setToken(testToken);

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(UserRolesAndPermissionsPageResultDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(1, output.getRoles().getStream().size());
        Assertions.assertEquals(1, output.getPermissions().getStream().size());

        mockServerClient.clear("mock1");
        mockServerClient.clear("mock2");
    }

    @Test
    void getUserRolesAndPermissions_BAD_REQUEST() {
        var testToken = "someToken";
        RoleRequest roleRequest = new RoleRequest();
        roleRequest.pageNumber(0).pageSize(5).token(testToken);

        mockServerClient.when(request().withPath("/internal/roles/me").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(roleRequest)))
                .withId("mock1")
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        PermissionRequest permissionRequest = new PermissionRequest();
        permissionRequest.pageNumber(0).pageSize(5).token(testToken);

        mockServerClient.when(request().withPath("/internal/permissions/me").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(permissionRequest)))
                .withId("mock2")
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        UserRolesAndPermissionsCriteriaDTO criteriaDTO = new UserRolesAndPermissionsCriteriaDTO();
        criteriaDTO.setPermissionsPageNumber(0);
        criteriaDTO.setPermissionsPageSize(5);
        criteriaDTO.setRolesPageNumber(0);
        criteriaDTO.setRolesPageSize(5);
        criteriaDTO.setToken(testToken);

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        Assertions.assertNotNull(output);
        mockServerClient.clear("mock1");
        mockServerClient.clear("mock2");
    }
}
