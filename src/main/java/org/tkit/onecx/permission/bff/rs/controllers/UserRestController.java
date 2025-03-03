package org.tkit.onecx.permission.bff.rs.controllers;

import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.permission.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.permission.bff.rs.mappers.UserMapper;

import gen.org.tkit.onecx.permission.bff.rs.internal.UserApiService;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.UserCriteriaDTO;
import gen.org.tkit.onecx.permission.client.api.AssignmentInternalApi;
import gen.org.tkit.onecx.permission.client.api.PermissionInternalApi;
import gen.org.tkit.onecx.permission.client.api.RoleInternalApi;
import gen.org.tkit.onecx.permission.client.model.PermissionPageResult;
import gen.org.tkit.onecx.permission.client.model.RolePageResult;
import gen.org.tkit.onecx.permission.client.model.UserAssignmentPageResult;

public class UserRestController implements UserApiService {

    @Inject
    UserMapper userMapper;
    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    @RestClient
    RoleInternalApi roleClient;

    @Inject
    @RestClient
    PermissionInternalApi permissionClient;

    @Inject
    @RestClient
    AssignmentInternalApi assignmentClient;

    @Inject
    HttpHeaders headers;

    @Override
    public Response getUserRoles(UserCriteriaDTO userCriteriaDTO) {
        var token = headers.getHeaderString(AUTHORIZATION);
        try (Response roleResponse = roleClient
                .getUserRoles(userMapper.mapUserRoleRequest(userCriteriaDTO, token))) {
            return Response.status(Response.Status.OK)
                    .entity(userMapper.map(roleResponse.readEntity(RolePageResult.class))).build();
        }
    }

    @Override
    public Response getUserPermissions(UserCriteriaDTO userCriteriaDTO) {
        var token = headers.getHeaderString(AUTHORIZATION);
        try (Response permissionResponse = permissionClient
                .getUsersPermissions(userMapper.mapUserPermissionRequest(userCriteriaDTO, token))) {
            return Response.status(Response.Status.OK).entity(userMapper.map(
                    permissionResponse.readEntity(PermissionPageResult.class))).build();
        }
    }

    @Override
    public Response getTokenRoles() {
        var token = headers.getHeaderString(AUTHORIZATION);
        try (Response response = roleClient.getTokenRoles(token)) {
            return Response.status(response.getStatus()).entity(response.readEntity(String[].class)).build();
        }
    }

    @Override
    public Response getUserAssignments(UserCriteriaDTO userCriteriaDTO) {
        var token = headers.getHeaderString(AUTHORIZATION);
        try (Response assignmentResponse = assignmentClient
                .getUserAssignments(userMapper.mapUserAssignmentRequest(userCriteriaDTO, token))) {
            return Response.status(Response.Status.OK).entity(userMapper.map(
                    assignmentResponse.readEntity(UserAssignmentPageResult.class))).build();
        }
    }

    @ServerExceptionMapper
    public Response clientRestException(ClientWebApplicationException ex) {
        return exceptionMapper.clientException(ex);
    }
}
