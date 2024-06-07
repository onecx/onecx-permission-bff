package org.tkit.onecx.permission.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.permission.bff.rs.internal.model.UserAssignmentPageResultDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.UserCriteriaDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.UserPermissionsPageResultDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.UserRolesPageResultDTO;
import gen.org.tkit.onecx.permission.client.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface UserMapper {

    RoleRequest mapUserRoleRequest(UserCriteriaDTO userCriteriaDTO, String token);

    PermissionRequest mapUserPermissionRequest(UserCriteriaDTO userCriteriaDTO, String token);

    AssignmentRequest mapUserAssignmentRequest(UserCriteriaDTO userCriteriaDTO, String token);

    @Mapping(target = "removeStreamItem", ignore = true)
    UserPermissionsPageResultDTO map(PermissionPageResult permissionPageResult);

    @Mapping(target = "removeStreamItem", ignore = true)
    UserRolesPageResultDTO map(RolePageResult rolePageResult);

    @Mapping(target = "removeStreamItem", ignore = true)
    UserAssignmentPageResultDTO map(UserAssignmentPageResult pageResult);

}
