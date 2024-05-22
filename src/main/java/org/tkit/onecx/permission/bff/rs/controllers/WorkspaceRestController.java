package org.tkit.onecx.permission.bff.rs.controllers;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.permission.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.permission.bff.rs.mappers.WorkspaceMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.permission.bff.rs.internal.WorkspaceApiService;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.WorkspaceDetailsDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.WorkspaceSearchCriteriaDTO;
import gen.org.tkit.onecx.permission.client.api.WorkspaceExternalApi;
import gen.org.tkit.onecx.permission.client.model.Workspace;
import gen.org.tkit.onecx.permission.client.model.WorkspacePageResult;
import gen.org.tkit.onecx.product.store.client.api.ProductsApi;
import gen.org.tkit.onecx.product.store.client.model.ProductItemLoadSearchCriteria;
import gen.org.tkit.onecx.product.store.client.model.ProductsLoadResult;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class WorkspaceRestController implements WorkspaceApiService {

    @RestClient
    @Inject
    WorkspaceExternalApi workspaceClient;

    @RestClient
    @Inject
    ProductsApi productStoreClient;

    @Inject
    WorkspaceMapper mapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response searchWorkspaces(WorkspaceSearchCriteriaDTO criteriaDTO) {
        try (Response response = workspaceClient.searchWorkspaces(mapper.map(criteriaDTO))) {
            return Response.status(response.getStatus()).entity(mapper.map(response.readEntity(WorkspacePageResult.class)))
                    .build();
        }
    }

    @Override
    public Response getDetailsByWorkspaceName(String workspaceName) {
        try (Response response = workspaceClient.getWorkspaceByName(workspaceName)) {
            WorkspaceDetailsDTO workspaceDetails;

            List<String> workspaceRoles = new ArrayList<>();
            var workspaceResponse = response.readEntity(Workspace.class);
            if (workspaceResponse.getWorkspaceRoles() != null) {
                workspaceRoles = new ArrayList<>(workspaceResponse.getWorkspaceRoles());
            }

            ProductsLoadResult productsLoadResult = new ProductsLoadResult();
            if (workspaceResponse.getProducts() != null) {
                //get mfe and ms for each product by name from product-store
                ProductItemLoadSearchCriteria mfeAndMsCriteria = new ProductItemLoadSearchCriteria();
                mfeAndMsCriteria.setPageSize(workspaceResponse.getProducts().size());
                mfeAndMsCriteria.setProductNames(new ArrayList<>(workspaceResponse.getProducts()));

                try (Response productStoreResponse = productStoreClient.loadProductsByCriteria(mfeAndMsCriteria)) {
                    productsLoadResult = productStoreResponse.readEntity(ProductsLoadResult.class);
                }
            }

            workspaceDetails = mapper.map(workspaceRoles, productsLoadResult);

            return Response.status(Response.Status.OK).entity(workspaceDetails).build();
        }
    }

    @ServerExceptionMapper
    public Response clientRestException(ClientWebApplicationException ex) {
        return exceptionMapper.clientException(ex);
    }
}
