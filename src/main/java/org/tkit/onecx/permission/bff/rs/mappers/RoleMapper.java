package org.tkit.onecx.permission.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.iam.client.model.RolePageResultIamV1;
import gen.org.tkit.onecx.iam.client.model.RoleSearchCriteriaIamV1;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.*;
import gen.org.tkit.onecx.permission.client.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface RoleMapper {
    CreateRoleRequest map(CreateRoleRequestDTO createRoleRequestDTO);

    RoleDTO map(Role role);

    RoleSearchCriteria map(RoleSearchCriteriaDTO roleSearchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    RolePageResultDTO map(RolePageResult pageResult);

    UpdateRoleRequest map(UpdateRoleRequestDTO updateRoleRequestDTO);

    RoleSearchCriteriaIamV1 map(IAMRoleSearchCriteriaDTO searchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    IAMRolePageResultDTO map(RolePageResultIamV1 pageResultIamV1);
}
