package org.tkit.onecx.permission.bff.rs.mappers;

import gen.org.tkit.onecx.permission.bff.rs.internal.model.PermissionPageResultDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.PermissionSearchCriteriaDTO;
import gen.org.tkit.onecx.permission.client.model.PermissionPageResult;
import gen.org.tkit.onecx.permission.client.model.PermissionSearchCriteria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface PermissionMapper {
  PermissionSearchCriteria map(PermissionSearchCriteriaDTO permissionSearchCriteriaDTO);
  @Mapping(target = "removeStreamItem", ignore = true)
  PermissionPageResultDTO map(PermissionPageResult pageResult);

}
