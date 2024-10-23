package org.tkit.onecx.permission.bff.rs.controllers;

import static jakarta.ws.rs.core.Response.Status.*;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.permission.bff.rs.PermissionConfig;
import org.tkit.onecx.permission.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.permission.bff.rs.mappers.RoleMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.iam.client.api.AdminRoleControllerApi;
import gen.org.tkit.onecx.iam.client.model.RolePageResultIamV1;
import gen.org.tkit.onecx.permission.bff.rs.internal.RoleApiService;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.*;
import gen.org.tkit.onecx.permission.client.api.RoleInternalApi;
import gen.org.tkit.onecx.permission.client.model.Role;
import gen.org.tkit.onecx.permission.client.model.RolePageResult;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class RoleRestController implements RoleApiService {

    @RestClient
    @Inject
    RoleInternalApi roleClient;

    @RestClient
    @Inject
    AdminRoleControllerApi iamClient;

    @Inject
    RoleMapper mapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    PermissionConfig config;

    @Override
    public Response createRole(CreateRolesRequestDTO createsRoleRequestDTO) {
        List<RoleDTO> createdRoles = new ArrayList<>();
        createsRoleRequestDTO.getRoles().forEach(r -> {
            try (Response response = roleClient
                    .createRole(mapper.map(r))) {
                createdRoles.add(mapper.map(response.readEntity(Role.class)));
            } catch (Exception ex) {
                // ignore failed creation
            }
        });
        if (!createdRoles.isEmpty()) {
            return Response.status(CREATED).entity(createdRoles).build();
        } else {
            return Response.status(BAD_REQUEST)
                    .entity(new ProblemDetailResponseDTO().errorCode("400").detail("No roles created")).build();
        }
    }

    @Override
    public Response deleteRole(String id) {
        try (Response response = roleClient.deleteRole(id)) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response getRoleById(String id) {
        try (Response response = roleClient.getRoleById(id)) {
            RoleDTO responseDTO = mapper.map(response.readEntity(Role.class));
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @Override
    public Response searchAvailableRoles(IAMRoleSearchCriteriaDTO searchCriteriaDTO) {
        if (!config.restClients().iam().enabled()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        try (Response response = iamClient.rolesSearchByCriteria(mapper.map(searchCriteriaDTO))) {
            IAMRolePageResultDTO responseDTO = mapper
                    .map(response.readEntity(RolePageResultIamV1.class));
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @Override
    public Response searchRoles(RoleSearchCriteriaDTO roleSearchCriteriaDTO) {
        try (Response response = roleClient.searchRoles(mapper.map(roleSearchCriteriaDTO))) {
            RolePageResultDTO responseDTO = mapper.map(response.readEntity(RolePageResult.class));
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @Override
    public Response updateRole(String id, UpdateRoleRequestDTO updateRoleRequestDTO) {
        try (Response response = roleClient.updateRole(id,
                mapper.map(updateRoleRequestDTO))) {
            return Response.status(response.getStatus()).build();
        }
    }

    @ServerExceptionMapper
    public Response restException(WebApplicationException ex) {
        return Response.status(ex.getResponse().getStatus()).build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }
}
