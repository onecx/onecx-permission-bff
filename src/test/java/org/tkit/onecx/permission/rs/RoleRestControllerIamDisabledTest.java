package org.tkit.onecx.permission.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.permission.bff.rs.PermissionConfig;
import org.tkit.onecx.permission.bff.rs.controllers.RoleRestController;

import gen.org.tkit.onecx.iam.client.model.RoleIamV1;
import gen.org.tkit.onecx.iam.client.model.RolePageResultIamV1;
import gen.org.tkit.onecx.iam.client.model.RoleSearchCriteriaIamV1;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.IAMRoleSearchCriteriaDTO;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.InjectMock;
import io.quarkus.test.Mock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.config.SmallRyeConfig;

@QuarkusTest
@TestHTTPEndpoint(RoleRestController.class)
class RoleRestControllerIamDisabledTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @InjectMock
    PermissionConfig permissionConfig;

    @Inject
    Config config;

    public static class ConfigProducer {

        @Inject
        Config config;

        @Produces
        @ApplicationScoped
        @Mock
        PermissionConfig config() {
            return config.unwrap(SmallRyeConfig.class).getConfigMapping(PermissionConfig.class);
        }
    }

    @BeforeEach
    void beforeEach() {
        Mockito.when(permissionConfig.restClients()).thenReturn(new PermissionConfig.RestClientsConfig() {
            @Override
            public PermissionConfig.RestClientConfig iam() {
                return () -> false;
            }
        });
    }

    @Test
    void searchIAMRole_deactivated_client_Test() {

        RoleSearchCriteriaIamV1 criteria = new RoleSearchCriteriaIamV1();

        RolePageResultIamV1 pageResult = new RolePageResultIamV1();
        RoleIamV1 role = new RoleIamV1();
        role.name("role1").description("desc1");
        pageResult.stream(List.of(role)).size(1).number(1).totalElements(1L).totalPages(1L);

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/v1/roles/search").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(criteria)))
                .withId(MOCKID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(pageResult)));

        IAMRoleSearchCriteriaDTO criteriaDTO = new IAMRoleSearchCriteriaDTO();

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .post("/iam/search")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
        mockServerClient.clear(MOCKID);
    }
}
