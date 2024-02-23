package org.tkit.onecx.permission.rs;

import static io.restassured.RestAssured.given;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.tkit.onecx.permission.bff.rs.controllers.WorkspaceRestController;

import gen.org.tkit.onecx.permission.bff.rs.internal.model.ProductDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.WorkspaceDetailsDTO;
import gen.org.tkit.onecx.permission.client.model.Product;
import gen.org.tkit.onecx.permission.client.model.Workspace;
import gen.org.tkit.onecx.product.store.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;

@QuarkusTest
@TestHTTPEndpoint(WorkspaceRestController.class)
class WorkspaceRestControllerTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @Test
    void getAllWorkspaceNamesTest() {
        ArrayList<String> workspaceNames = new ArrayList<>();
        workspaceNames.add("workspace1");
        workspaceNames.add("workspace2");
        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/v1/workspaces").withMethod(HttpMethod.GET))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withBody(JsonBody.json(workspaceNames)));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .get()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(new TypeRef<String[]>() {
                });

        Assertions.assertNotNull(output);
        Assertions.assertTrue(Arrays.stream(output).toList().contains("workspace1"));
        Assertions.assertTrue(Arrays.stream(output).toList().contains("workspace2"));

    }

    @Test
    void getAllProductsByWorkspaceNameTest() {
        String workspaceName = "workspace1";

        Product product1 = new Product();
        product1.productName("product1");
        Product product2 = new Product();
        product2.productName("product2");

        ArrayList<Product> products = new ArrayList<>();
        products.add(product1);
        products.add(product2);

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/v1/workspaces/" + workspaceName + "/products").withMethod(HttpMethod.GET))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withBody(JsonBody.json(products)));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .pathParam("workspaceName", workspaceName)
                .get("/{workspaceName}/products")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(new TypeRef<ProductDTO[]>() {
                });

        Assertions.assertNotNull(output);
        Assertions.assertEquals(2, Arrays.stream(output).toList().size());
    }

    @Test
    void getDetailsByWorkspaceNameTest() {

        String workspaceName = "test-workspace";
        Workspace workspace = new Workspace();
        workspace.name("test-workspace").workspaceRoles(Set.of("role1", "role2"));

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/v1/workspaces/name/" + workspaceName).withMethod(HttpMethod.GET))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withBody(JsonBody.json(workspace)));

        List<Product> productsOfWorkspace = new ArrayList<>();
        Product product1 = new Product();
        product1.productName("product1");
        Product product2 = new Product();
        product2.productName("product2");
        productsOfWorkspace.add(product1);
        productsOfWorkspace.add(product2);

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/v1/workspaces/" + workspaceName + "/products").withMethod(HttpMethod.GET))
                .withId("MOCKID2")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withBody(JsonBody.json(productsOfWorkspace)));

        List<String> productNames = List.of("product1", "product2");
        ProductItemLoadSearchCriteria criteria = new ProductItemLoadSearchCriteria();
        criteria.setProductNames(productNames);

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
        MicroserviceAbstract ms2 = new MicroserviceAbstract();
        ms2.appId("ms2").appName("ms2");
        productsAbstract2.setMicrofrontends(List.of(mfe2));
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
        mockServerClient.clear("MOCKID2");
        mockServerClient.clear("MOCKID3");
    }

}
