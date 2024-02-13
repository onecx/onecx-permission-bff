package org.tkit.onecx.permission.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.permission.bff.rs.internal.model.WorkspacePageResultDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.WorkspaceSearchCriteriaDTO;
import gen.org.tkit.onecx.permission.client.model.WorkspacePageResult;
import gen.org.tkit.onecx.permission.client.model.WorkspaceSearchCriteria;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface WorkspaceMapper {
    @Mapping(target = "themeName", ignore = true)
    WorkspaceSearchCriteria map(WorkspaceSearchCriteriaDTO workspaceSearchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    WorkspacePageResultDTO map(WorkspacePageResult pageResult);
}
