package org.tkit.onecx.permission.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.permission.bff.rs.controllers.WorkspaceRestController;

import gen.org.tkit.onecx.permission.bff.rs.internal.model.*;
import gen.org.tkit.onecx.permission.client.model.*;
import gen.org.tkit.onecx.permission.client.model.Product;
import gen.org.tkit.onecx.product.store.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(WorkspaceRestController.class)
class WorkspaceRestControllerTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @Test
    void searchWorkspacesTest() {
        WorkspaceSearchCriteria criteria = new WorkspaceSearchCriteria();
        criteria.setPageNumber(0);
        criteria.setPageSize(100);

        WorkspaceAbstract abstract1 = new WorkspaceAbstract();
        abstract1.setName("workspace1");
        WorkspaceAbstract abstract2 = new WorkspaceAbstract();
        abstract1.setName("workspace2");

        WorkspacePageResult pageResult = new WorkspacePageResult();
        pageResult.setStream(List.of(abstract1, abstract2));
        pageResult.setTotalElements(2L);
        pageResult.setNumber(0);
        pageResult.setTotalPages(1L);
        pageResult.setSize(100);

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/v1/workspaces/search").withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(criteria))
                        .withContentType(MediaType.APPLICATION_JSON))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withBody(JsonBody.json(pageResult)));
        var input = new WorkspaceSearchCriteriaDTO();
        input.setPageSize(100);
        input.setPageNumber(0);

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(input)
                .post("/search")
                .then()
                .contentType(APPLICATION_JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(WorkspacePageResultDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(2, output.getStream().size());
    }

    @Test
    void searchWorkspaces_Internal_Server_Error_Test() {
        WorkspaceSearchCriteria criteria = new WorkspaceSearchCriteria();
        criteria.setPageNumber(0);
        criteria.setPageSize(100);

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/v1/workspaces/search").withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(criteria))
                        .withContentType(MediaType.APPLICATION_JSON))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
        var input = new WorkspaceSearchCriteriaDTO();
        input.setPageSize(100);
        input.setPageNumber(0);

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(input)
                .post("/search")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void getDetailsByWorkspaceNameTest() {

        String workspaceName = "test-workspace";
        Workspace workspace = new Workspace();
        workspace.name("test-workspace").workspaceRoles(Set.of("role1", "role2"))
                .products(Set.of("product1", "product2"));

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/v1/workspaces/" + workspaceName).withMethod(HttpMethod.GET))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withBody(JsonBody.json(workspace)));

        List<String> productNames = List.of("product1", "product2");
        ProductItemLoadSearchCriteria criteria = new ProductItemLoadSearchCriteria();
        criteria.setProductNames(productNames);
        criteria.setPageSize(productNames.size());

        ProductsLoadResult result = new ProductsLoadResult();

        ProductsAbstract productsAbstract1 = new ProductsAbstract();
        productsAbstract1.setName("product1");
        MicrofrontendAbstract mfe1 = new MicrofrontendAbstract();
        mfe1.appId("mfe1").appName("mfe1");
        MicroserviceAbstract ms1 = new MicroserviceAbstract();
        ms1.appId("ms1").appName("ms1");
        productsAbstract1.setMicrofrontends(List.of(mfe1));
        productsAbstract1.setMicroservices(List.of(ms1));

        ProductsAbstract productsAbstract2 = new ProductsAbstract();
        productsAbstract2.setName("product2");
        MicrofrontendAbstract mfe2 = new MicrofrontendAbstract();
        mfe2.appId("mfe2").appName("mfe2");
        MicrofrontendAbstract mfe2duplication = new MicrofrontendAbstract();
        mfe2duplication.appId("mfe2").appName("mfe2");
        MicroserviceAbstract ms2 = new MicroserviceAbstract();
        ms2.appId("ms2").appName("ms2");
        productsAbstract2.setMicrofrontends(List.of(mfe2, mfe2duplication));
        productsAbstract2.setMicroservices(List.of(ms2));

        result.setStream(List.of(productsAbstract1, productsAbstract2));
        result.setTotalElements(2L);
        result.setNumber(0);
        result.setSize(2);
        result.setTotalPages(1L);

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/v1/products/load").withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(criteria)))
                .withId("MOCKID3")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withBody(JsonBody.json(result)));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .pathParam("workspaceName", workspaceName)
                .get("/{workspaceName}/details")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(WorkspaceDetailsDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(2, output.getProducts().size());
        Assertions.assertTrue(output.getWorkspaceRoles().contains("role1"));
        Assertions.assertTrue(output.getWorkspaceRoles().contains("role2"));
        Assertions.assertEquals(1, output.getProducts().get(0).getMfe().size());
        Assertions.assertEquals(1, output.getProducts().get(0).getMs().size());
        Assertions.assertNotNull(output.getProducts().get(0).getMfe().get(0).getAppId());

        Assertions.assertEquals(1, output.getProducts().get(1).getMfe().size());
        Assertions.assertEquals(1, output.getProducts().get(1).getMs().size());
        Assertions.assertNotNull(output.getProducts().get(1).getMs().get(0).getAppId());

        mockServerClient.clear(MOCKID);
        mockServerClient.clear("MOCKID3");
    }

    @Test
    void getDetailsByWorkspaceNameMissingRolesAndMissingProductNamesTest() {

        String workspaceName = "test-workspace";
        Workspace workspace = new Workspace();
        workspace.name("test-workspace");

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/v1/workspaces/" + workspaceName).withMethod(HttpMethod.GET))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withBody(JsonBody.json(workspace)));

        List<Product> productsOfWorkspace = new ArrayList<>();

        WorkspaceLoad loadResponse = new WorkspaceLoad();
        loadResponse.setName(workspaceName);
        loadResponse.setProducts(productsOfWorkspace);
        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/v1/workspaces/" + workspaceName + "/load").withMethod(HttpMethod.GET))
                .withId("MOCKID2")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withBody(JsonBody.json(loadResponse)));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .pathParam("workspaceName", workspaceName)
                .get("/{workspaceName}/details")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(WorkspaceDetailsDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertTrue(output.getProducts().isEmpty());
        Assertions.assertTrue(output.getWorkspaceRoles().isEmpty());

        mockServerClient.clear(MOCKID);
        mockServerClient.clear("MOCKID2");
    }

    @Test
    void getDetailsByInvalidWorkspaceNameTest() {

        String workspaceName = "test-workspace";
        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/v1/workspaces/" + workspaceName).withMethod(HttpMethod.GET))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode()));

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .pathParam("workspaceName", workspaceName)
                .get("/{workspaceName}/details")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        mockServerClient.clear(MOCKID);

    }
}
