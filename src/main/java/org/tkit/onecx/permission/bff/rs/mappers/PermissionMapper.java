package org.tkit.onecx.permission.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.permission.bff.rs.internal.model.*;
import gen.org.tkit.onecx.permission.client.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface PermissionMapper {
    PermissionSearchCriteria map(PermissionSearchCriteriaDTO permissionSearchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    PermissionPageResultDTO map(PermissionPageResult pageResult);

    CreatePermissionRequest map(CreatePermissionRequestDTO createPermissionRequestDTO);

    UpdatePermissionRequest map(UpdatePermissionRequestDTO updatePermissionRequestDTO);

    PermissionDTO map(Permission permission);

}
