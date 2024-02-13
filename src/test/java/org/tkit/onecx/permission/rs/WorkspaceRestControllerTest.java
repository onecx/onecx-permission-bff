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
import org.tkit.onecx.permission.bff.rs.controllers.WorkspaceRestController;

import gen.org.tkit.onecx.permission.bff.rs.internal.model.WorkspacePageResultDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.WorkspaceSearchCriteriaDTO;
import gen.org.tkit.onecx.permission.client.model.WorkspaceAbstract;
import gen.org.tkit.onecx.permission.client.model.WorkspacePageResult;
import gen.org.tkit.onecx.permission.client.model.WorkspaceSearchCriteria;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(WorkspaceRestController.class)
class WorkspaceRestControllerTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @Test
    void searchWorkspaceByCriteriaTest() {
        WorkspaceSearchCriteria criteria = new WorkspaceSearchCriteria();
        criteria.pageNumber(1).pageSize(1);

        WorkspacePageResult pageResult = new WorkspacePageResult();
        WorkspaceAbstract workspace = new WorkspaceAbstract();
        workspace.name("workspace1").description("desc1");
        pageResult.stream(List.of(workspace)).size(1).number(1).totalElements(1L).totalPages(1L);

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/v1/workspaces/search").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(criteria)))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(pageResult)));

        WorkspaceSearchCriteriaDTO criteriaDTO = new WorkspaceSearchCriteriaDTO();
        criteriaDTO.pageNumber(1).pageSize(1);

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
                .extract().as(WorkspacePageResultDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(pageResult.getSize(), output.getSize());
        Assertions.assertEquals(pageResult.getStream().size(), output.getStream().size());
        Assertions.assertEquals(pageResult.getStream().get(0).getName(), output.getStream().get(0).getName());
        Assertions.assertEquals(pageResult.getStream().get(0).getDescription(), output.getStream().get(0).getDescription());

        mockServerClient.clear(MOCKID);
    }

    @Test
    void searchWorkspacesByEmptyCriteriaTest() {

        WorkspaceSearchCriteria criteria = new WorkspaceSearchCriteria();

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/v1/workspaces/search").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(criteria)))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        WorkspaceSearchCriteriaDTO criteriaDTO = new WorkspaceSearchCriteriaDTO();

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        mockServerClient.clear(MOCKID);
    }
}
