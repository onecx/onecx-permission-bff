package org.tkit.onecx.permission.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.permission.bff.rs.internal.model.ApplicationPageResultDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.ApplicationSearchCriteriaDTO;
import gen.org.tkit.onecx.permission.client.model.ApplicationPageResult;
import gen.org.tkit.onecx.permission.client.model.ApplicationSearchCriteria;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ApplicationMapper {

    ApplicationSearchCriteria map(ApplicationSearchCriteriaDTO applicationSearchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    ApplicationPageResultDTO map(ApplicationPageResult pageResult);
}
