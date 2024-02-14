package org.tkit.onecx.permission.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.tkit.onecx.permission.bff.rs.mappers.WorkspaceMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.permission.bff.rs.internal.WorkspaceApiService;
import gen.org.tkit.onecx.permission.client.api.ProductExternalApi;
import gen.org.tkit.onecx.permission.client.api.WorkspaceExternalApi;
import gen.org.tkit.onecx.permission.client.model.Product;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class WorkspaceRestController implements WorkspaceApiService {

    @RestClient
    @Inject
    WorkspaceExternalApi workspaceClient;

    @RestClient
    @Inject
    ProductExternalApi productClient;

    @Inject
    WorkspaceMapper mapper;

    @Override
    public Response getAllProductsByWorkspaceName(String workspaceName) {
        try (Response response = productClient.getProducts(workspaceName)) {
            return Response.status(response.getStatus()).entity(mapper.map(response.readEntity(Product[].class))).build();
        }
    }

    @Override
    public Response getAllWorkspaceNames() {
        try (Response response = workspaceClient.getAllWorkspaceNames()) {
            return Response.status(response.getStatus()).entity(response.readEntity(String[].class)).build();
        }
    }
}
