package org.tkit.onecx.permission.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.permission.bff.rs.internal.model.AssignmentDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.AssignmentPageResultDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.AssignmentSearchCriteriaDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.CreateAssignmentRequestDTO;
import gen.org.tkit.onecx.permission.client.model.Assignment;
import gen.org.tkit.onecx.permission.client.model.AssignmentPageResult;
import gen.org.tkit.onecx.permission.client.model.AssignmentSearchCriteria;
import gen.org.tkit.onecx.permission.client.model.CreateAssignmentRequest;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface AssignmentMapper {
    CreateAssignmentRequest map(CreateAssignmentRequestDTO createAssignmentRequestDTO);

    AssignmentDTO map(Assignment assignment);

    AssignmentSearchCriteria map(AssignmentSearchCriteriaDTO assignmentSearchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    AssignmentPageResultDTO map(AssignmentPageResult pageResult);
}
