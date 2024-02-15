package org.tkit.onecx.permission.rs;

import static io.restassured.RestAssured.given;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.ArrayList;
import java.util.Arrays;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.tkit.onecx.permission.bff.rs.controllers.WorkspaceRestController;

import gen.org.tkit.onecx.permission.bff.rs.internal.model.ProductDTO;
import gen.org.tkit.onecx.permission.client.model.Product;
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

}
