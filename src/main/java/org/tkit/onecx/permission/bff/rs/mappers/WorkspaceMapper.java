package org.tkit.onecx.permission.bff.rs.mappers;

import gen.org.tkit.onecx.permission.bff.rs.internal.model.WorkspacePageResultDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.WorkspaceSearchCriteriaDTO;
import gen.org.tkit.onecx.permission.client.model.WorkspacePageResult;
import gen.org.tkit.onecx.permission.client.model.WorkspaceSearchCriteria;
import org.mapstruct.Mapper;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface WorkspaceMapper {
  WorkspaceSearchCriteria map(WorkspaceSearchCriteriaDTO workspaceSearchCriteriaDTO);

  WorkspacePageResultDTO map(WorkspacePageResult pageResult);
}
