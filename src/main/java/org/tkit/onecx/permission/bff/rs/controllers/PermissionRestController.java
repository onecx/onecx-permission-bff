package org.tkit.onecx.permission.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.permission.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.permission.bff.rs.mappers.PermissionMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.permission.bff.rs.internal.PermissionApiService;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.PermissionPageResultDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.PermissionSearchCriteriaDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.permission.client.api.PermissionInternalApi;
import gen.org.tkit.onecx.permission.client.model.PermissionPageResult;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class PermissionRestController implements PermissionApiService {

    @RestClient
    @Inject
    PermissionInternalApi permissionClient;

    @Inject
    PermissionMapper mapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response searchPermissions(PermissionSearchCriteriaDTO permissionSearchCriteriaDTO) {
        try (Response response = permissionClient.searchPermissions(mapper.map(permissionSearchCriteriaDTO))) {
            PermissionPageResultDTO responseDTO = mapper.map(response.readEntity(PermissionPageResult.class));
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @ServerExceptionMapper
    public Response restException(WebApplicationException ex) {
        return Response.status(ex.getResponse().getStatus()).build();
    }
}
