package org.tkit.onecx.permission.bff.rs.controllers;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.permission.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.permission.bff.rs.mappers.WorkspaceMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.permission.bff.rs.internal.WorkspaceApiService;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.WorkspaceDetailsDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.WorkspaceSearchCriteriaDTO;
import gen.org.tkit.onecx.permission.client.api.WorkspaceExternalApi;
import gen.org.tkit.onecx.permission.client.model.Product;
import gen.org.tkit.onecx.permission.client.model.Workspace;
import gen.org.tkit.onecx.permission.client.model.WorkspaceLoad;
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
    public Response getAllProductsByWorkspaceName(String workspaceName) {
        try (Response response = workspaceClient.loadWorkspaceByName(workspaceName)) {
            return Response.status(response.getStatus()).entity(mapper.map(response.readEntity(WorkspaceLoad.class))).build();
        }
    }

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
            List<String> productNames;
            List<String> workspaceRoles = new ArrayList<>();
            ProductsLoadResult productsLoadResult = new ProductsLoadResult();
            var workspaceResponse = response.readEntity(Workspace.class);
            if (workspaceResponse.getWorkspaceRoles() != null) {
                workspaceRoles = workspaceResponse.getWorkspaceRoles().stream().toList();
            }

            //get products of workspace
            try (Response wsProductsResponse = workspaceClient.loadWorkspaceByName(workspaceName)) {
                //list of product names registered in workspace
                productNames = wsProductsResponse.readEntity(WorkspaceLoad.class).getProducts().stream()
                        .map(Product::getProductName).toList();
                if (!productNames.isEmpty()) {
                    //get mfe and ms for each product by name from product-store
                    ProductItemLoadSearchCriteria mfeAndMsCriteria = new ProductItemLoadSearchCriteria();
                    mfeAndMsCriteria.setProductNames(productNames);
                    try (Response productStoreResponse = productStoreClient.loadProductsByCriteria(mfeAndMsCriteria)) {
                        productsLoadResult = productStoreResponse.readEntity(ProductsLoadResult.class);
                    }
                }
                workspaceDetails = mapper.map(workspaceRoles, productsLoadResult);
            }
            return Response.status(Response.Status.OK).entity(workspaceDetails).build();
        }
    }

    @ServerExceptionMapper
    public Response restException(WebApplicationException ex) {
        return Response.status(ex.getResponse().getStatus()).build();
    }
}
