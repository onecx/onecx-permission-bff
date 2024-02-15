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
import org.tkit.onecx.permission.bff.rs.controllers.ApplicationRestController;

import gen.org.tkit.onecx.permission.bff.rs.internal.model.ApplicationPageResultDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.ApplicationSearchCriteriaDTO;
import gen.org.tkit.onecx.permission.client.model.Application;
import gen.org.tkit.onecx.permission.client.model.ApplicationPageResult;
import gen.org.tkit.onecx.permission.client.model.ApplicationSearchCriteria;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(ApplicationRestController.class)
class ApplicationRestControllerTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @Test
    void searchApplicationByCriteriaTest() {
        ApplicationSearchCriteria criteria = new ApplicationSearchCriteria();
        criteria.pageNumber(1).pageSize(1).appId("app1");

        ApplicationPageResult pageResult = new ApplicationPageResult();
        Application application = new Application();
        application.appId("app1").name("app1");
        pageResult.stream(List.of(application)).size(1).number(1).totalElements(1L).totalPages(1L);

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/applications/search").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(criteria)))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(pageResult)));

        ApplicationSearchCriteriaDTO criteriaDTO = new ApplicationSearchCriteriaDTO();
        criteriaDTO.pageNumber(1).appId("app1").pageSize(1);

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
                .extract().as(ApplicationPageResultDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(pageResult.getSize(), output.getSize());
        Assertions.assertEquals(pageResult.getStream().size(), output.getStream().size());
        Assertions.assertEquals(pageResult.getStream().get(0).getAppId(), output.getStream().get(0).getAppId());
        Assertions.assertEquals(pageResult.getStream().get(0).getName(), output.getStream().get(0).getName());

        mockServerClient.clear(MOCKID);
    }

    @Test
    void searchApplicationsByEmptyCriteriaTest() {

        ApplicationSearchCriteria criteria = new ApplicationSearchCriteria();

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/applications/search").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(criteria)))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        ApplicationSearchCriteriaDTO criteriaDTO = new ApplicationSearchCriteriaDTO();

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
