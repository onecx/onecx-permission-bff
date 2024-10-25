package org.tkit.onecx.permission.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.permission.bff.rs.controllers.AssignmentRestController;

import gen.org.tkit.onecx.iam.client.model.RoleIamV1;
import gen.org.tkit.onecx.iam.client.model.UserRolesResponseIamV1;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.*;
import gen.org.tkit.onecx.permission.client.model.*;
import gen.org.tkit.onecx.permission.exim.client.model.AssignmentSnapshot;
import gen.org.tkit.onecx.permission.exim.client.model.ExportAssignmentsRequest;
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
        criteria.pageNumber(1).pageSize(1).roleId("role1").appIds(List.of("app1"));

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
        criteriaDTO.pageNumber(1).roleId("role1").appIds(List.of("app1")).pageSize(1);

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
    void grantRoleAssignments_Test() {
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/assignments/grant/role123").withMethod(HttpMethod.POST))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode()));
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("roleId", "role123")
                .post("/grant/{roleId}")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode());
    }

    @Test
    void grantRoleApplicationAssignments_Test() {

        CreateRoleProductAssignmentRequest request = new CreateRoleProductAssignmentRequest();
        request.setAppId("app1");
        request.setProductName("product1");
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/assignments/grant/role123/product").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode()));

        CreateRoleApplicationAssignmentRequestDTO requestDTO = new CreateRoleApplicationAssignmentRequestDTO();
        requestDTO.setAppId("app1");
        requestDTO.setProductName("product1");
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .pathParam("roleId", "role123")
                .post("/grant/{roleId}/application")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode());
    }

    @Test
    void grantRoleProductsAssignments_Test() {

        CreateRoleProductsAssignmentRequest request = new CreateRoleProductsAssignmentRequest();
        request.setProductNames(List.of("product1", "product2", "product3"));
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/assignments/grant/role123/products").withMethod(HttpMethod.POST))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode()));

        CreateRoleProductsAssignmentRequestDTO requestDTO = new CreateRoleProductsAssignmentRequestDTO();
        requestDTO.setProductNames(List.of("product1", "product2", "product3"));

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .pathParam("roleId", "role123")
                .post("/grant/{roleId}/products")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode());
    }

    @Test
    void revokeRoleAssignments_Test() {
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/assignments/revoke/role123").withMethod(HttpMethod.POST))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode()));
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("roleId", "role123")
                .post("/revoke/{roleId}")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void revokeRoleApplicationAssignments_Test() {

        RevokeRoleProductAssignmentRequest request = new RevokeRoleProductAssignmentRequest();
        request.setAppId("app1");
        request.setProductName("product1");
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/assignments/revoke/role123/product").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode()));

        RevokeRoleApplicationAssignmentRequestDTO requestDTO = new RevokeRoleApplicationAssignmentRequestDTO();
        requestDTO.setAppId("app1");
        requestDTO.setProductName("product1");
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .pathParam("roleId", "role123")
                .post("/revoke/{roleId}/application")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void revokeRoleProductsAssignments_Test() {

        RevokeRoleProductsAssignmentRequest request = new RevokeRoleProductsAssignmentRequest();
        request.setProductNames(List.of("product1", "product2", "product3"));
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/assignments/revoke/role123/products").withMethod(HttpMethod.POST))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode()));

        RevokeRoleProductsAssignmentRequestDTO requestDTO = new RevokeRoleProductsAssignmentRequestDTO();
        requestDTO.setProductNames(List.of("product1", "product2", "product3"));

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .pathParam("roleId", "role123")
                .post("/revoke/{roleId}/products")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void exportAssignmentsTest() {

        ExportAssignmentsRequest request = new ExportAssignmentsRequest();
        request.setProductNames(Set.of("product1"));

        AssignmentSnapshot snapshot = new AssignmentSnapshot();
        snapshot.setAssignments(Map.of("product1",
                Map.of("app1", Map.of("role1", Map.of("resource1", List.of("read", "write"))))));

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/exim/v1/assignments/export").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId("MOCK_ID")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(snapshot)));

        ExportAssignmentsRequestDTO requestDTO = new ExportAssignmentsRequestDTO();
        requestDTO.setProductNames(Set.of("product1"));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post("/export")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(AssignmentSnapshot.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(2, output.getAssignments().get("product1").get("app1").get("role1").get("resource1").size());
        mockServerClient.clear("MOCK_ID");
    }

    @Test
    void importAssignmentsTest() {
        AssignmentSnapshot assignmentSnapshot = new AssignmentSnapshot();
        assignmentSnapshot.setAssignments(Map.of("product1",
                Map.of("app1", Map.of("role1", Map.of("resource1", List.of("read", "write"))))));

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/exim/v1/assignments/import").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(assignmentSnapshot)))
                .withId("MOCK_ID")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode()));

        AssignmentSnapshot assignmentSnapshotDTO = new AssignmentSnapshot();
        assignmentSnapshotDTO.setAssignments(Map.of("product1",
                Map.of("app1", Map.of("role1", Map.of("resource1", List.of("read", "write"))))));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(assignmentSnapshotDTO)
                .post("/import")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        Assertions.assertNotNull(output);
        mockServerClient.clear("MOCK_ID");

    }

    @Test
    void searchUsersAssignmentsByCriteriaTest() {

        UserRolesResponseIamV1 rolesReponse = new UserRolesResponseIamV1();
        rolesReponse.roles(List.of(new RoleIamV1().name("role1")));

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/v1/user/roles/user1").withMethod(HttpMethod.GET))
                .withId("MOCK_IAM_KC")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(rolesReponse)));

        AssignmentRolesSearchCriteria criteria = new AssignmentRolesSearchCriteria();
        criteria.pageNumber(1).pageSize(1).roles(List.of("role1"));

        UserAssignmentPageResult pageResult = new UserAssignmentPageResult();
        UserAssignment assignment = new UserAssignment();
        assignment.productName("product1").roleName("role1");
        pageResult.stream(List.of(assignment)).size(1).number(1).totalElements(1L).totalPages(1L);

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/assignments/roles/search").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(criteria)))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(pageResult)));

        AssignmentUserSearchCriteriaDTO criteriaDTO = new AssignmentUserSearchCriteriaDTO();
        criteriaDTO.pageNumber(1).userId("user1").pageSize(1);

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .post("/user/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(UserAssignmentPageResultDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(pageResult.getSize(), output.getSize());
        Assertions.assertEquals(pageResult.getStream().size(), output.getStream().size());
        Assertions.assertEquals(pageResult.getStream().get(0).getRoleName(), output.getStream().get(0).getRoleName());
        Assertions.assertEquals(pageResult.getStream().get(0).getProductName(), output.getStream().get(0).getProductName());

        mockServerClient.clear(MOCKID);
        mockServerClient.clear("MOCK_IAM_KC");
    }

    @Test
    void searchUsersAssignmentsByCriteria_No_Roles_Test() {

        UserRolesResponseIamV1 rolesReponse = new UserRolesResponseIamV1();

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/v1/user/roles/user1").withMethod(HttpMethod.GET))
                .withId("MOCK_IAM_KC")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(rolesReponse)));

        AssignmentRolesSearchCriteria criteria = new AssignmentRolesSearchCriteria();
        criteria.pageNumber(1).pageSize(1).roles(List.of("role1"));

        AssignmentUserSearchCriteriaDTO criteriaDTO = new AssignmentUserSearchCriteriaDTO();
        criteriaDTO.pageNumber(1).userId("user1").pageSize(1);

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .post("/user/search")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        mockServerClient.clear("MOCK_IAM_KC");
    }

    @Test
    void searchUsersAssignmentsByCriteria_error_kc_Test() {

        UserRolesResponseIamV1 rolesReponse = new UserRolesResponseIamV1();
        rolesReponse.roles(List.of());

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/v1/user/roles/user1").withMethod(HttpMethod.GET))
                .withId("MOCK_IAM_KC")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(rolesReponse)));

        AssignmentRolesSearchCriteria criteria = new AssignmentRolesSearchCriteria();
        criteria.pageNumber(1).pageSize(1).roles(List.of("role1"));

        AssignmentUserSearchCriteriaDTO criteriaDTO = new AssignmentUserSearchCriteriaDTO();
        criteriaDTO.pageNumber(1).userId("user1").pageSize(1);

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .post("/user/search")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        mockServerClient.clear("MOCK_IAM_KC");
    }

    @Test
    void searchUsersAssignmentsByCriteria_error_permission_api_Test() {

        UserRolesResponseIamV1 rolesReponse = new UserRolesResponseIamV1();
        rolesReponse.roles(List.of(new RoleIamV1().name("role1")));

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/v1/user/roles/user1").withMethod(HttpMethod.GET))
                .withId("MOCK_IAM_KC")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(rolesReponse)));

        AssignmentRolesSearchCriteria criteria = new AssignmentRolesSearchCriteria();
        criteria.pageNumber(1).pageSize(1).roles(List.of("role1"));

        AssignmentPageResult pageResult = new AssignmentPageResult();
        Assignment assignment = new Assignment();
        assignment.permissionId("permission1").roleId("role1");
        pageResult.stream(List.of(assignment)).size(1).number(1).totalElements(1L).totalPages(1L);

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/assignments/roles/search").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(criteria)))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode()));

        AssignmentUserSearchCriteriaDTO criteriaDTO = new AssignmentUserSearchCriteriaDTO();
        criteriaDTO.pageNumber(1).userId("user1").pageSize(1);

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .post("/user/search")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        mockServerClient.clear("MOCK_IAM_KC");
        mockServerClient.clear(MOCKID);
    }
}
