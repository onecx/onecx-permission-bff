package org.tkit.onecx.permission.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.permission.bff.rs.internal.model.UserRolesAndPermissionsCriteriaDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.UserRolesAndPermissionsPageResultDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.UserRolesAndPermissionsPageResultPermissionsDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.UserRolesAndPermissionsPageResultRolesDTO;
import gen.org.tkit.onecx.permission.client.model.PermissionPageResult;
import gen.org.tkit.onecx.permission.client.model.PermissionRequest;
import gen.org.tkit.onecx.permission.client.model.RolePageResult;
import gen.org.tkit.onecx.permission.client.model.RoleRequest;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface UserMapper {

    @Mapping(target = "pageSize", source = "rolesPageSize")
    @Mapping(target = "pageNumber", source = "rolesPageNumber")
    RoleRequest mapRoleRequest(UserRolesAndPermissionsCriteriaDTO userRolesAndPermissionsCriteriaDTO);

    @Mapping(target = "pageSize", source = "permissionsPageSize")
    @Mapping(target = "pageNumber", source = "permissionsPageNumber")
    PermissionRequest mapPermissionRequest(UserRolesAndPermissionsCriteriaDTO userRolesAndPermissionsCriteriaDTO);

    @Mapping(target = "roles", source = "rolePageResult")
    @Mapping(target = "permissions", source = "permissionPageResult")
    UserRolesAndPermissionsPageResultDTO map(RolePageResult rolePageResult, PermissionPageResult permissionPageResult);

    @Mapping(target = "removeStreamItem", ignore = true)
    UserRolesAndPermissionsPageResultRolesDTO map(RolePageResult rolePageResult);

    @Mapping(target = "removeStreamItem", ignore = true)
    UserRolesAndPermissionsPageResultPermissionsDTO map(PermissionPageResult permissionPageResult);
}
