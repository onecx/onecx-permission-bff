package org.tkit.onecx.permission.bff.rs.controllers;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.permission.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.permission.bff.rs.mappers.UserMapper;

import gen.org.tkit.onecx.permission.bff.rs.internal.UserApiService;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.UserRolesAndPermissionsCriteriaDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.UserRolesAndPermissionsPageResultDTO;
import gen.org.tkit.onecx.permission.client.api.PermissionInternalApi;
import gen.org.tkit.onecx.permission.client.api.RoleInternalApi;
import gen.org.tkit.onecx.permission.client.model.PermissionPageResult;
import gen.org.tkit.onecx.permission.client.model.RolePageResult;

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

    @Override
    public Response getUserRolesAndPermissions(UserRolesAndPermissionsCriteriaDTO userRolesAndPermissionsCriteriaDTO) {
        UserRolesAndPermissionsPageResultDTO resultDTO;
        try (Response roleResponse = roleClient.getUserRoles(userMapper.mapRoleRequest(userRolesAndPermissionsCriteriaDTO))) {
            try (Response permissionResponse = permissionClient
                    .getUsersPermissions(userMapper.mapPermissionRequest(userRolesAndPermissionsCriteriaDTO))) {
                resultDTO = userMapper.map(roleResponse.readEntity(RolePageResult.class),
                        permissionResponse.readEntity(PermissionPageResult.class));
            }
        }
        return Response.status(Response.Status.OK).entity(resultDTO).build();
    }

    @ServerExceptionMapper
    public Response clientRestException(ClientWebApplicationException ex) {
        return exceptionMapper.clientException(ex);
    }
}
