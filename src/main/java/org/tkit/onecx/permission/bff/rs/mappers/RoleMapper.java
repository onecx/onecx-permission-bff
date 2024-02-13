package org.tkit.onecx.permission.bff.rs.mappers;

import gen.org.tkit.onecx.permission.bff.rs.internal.model.*;
import gen.org.tkit.onecx.permission.client.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface RoleMapper {
  CreateRoleRequest map(CreateRoleRequestDTO createRoleRequestDTO);

  RoleDTO map(Role role);

  RoleSearchCriteria map(RoleSearchCriteriaDTO roleSearchCriteriaDTO);

  @Mapping(target = "removeStreamItem", ignore = true)
  RolePageResultDTO map(RolePageResult pageResult);

  UpdateRoleRequest map(UpdateRoleRequestDTO updateRoleRequestDTO);
}
