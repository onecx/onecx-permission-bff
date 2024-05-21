package org.tkit.onecx.permission.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.permission.bff.rs.internal.model.*;
import gen.org.tkit.onecx.permission.client.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface AssignmentMapper {
    CreateAssignmentRequest map(CreateAssignmentRequestDTO createAssignmentRequestDTO);

    AssignmentDTO map(Assignment assignment);

    @Mapping(target = "appIds", source = "appIds")
    AssignmentSearchCriteria map(AssignmentSearchCriteriaDTO assignmentSearchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    AssignmentPageResultDTO map(AssignmentPageResult pageResult);

    CreateRoleProductsAssignmentRequest mapRoleProducts(CreateProductAssignmentsRequestDTO createProductAssignmentsRequestDTO);

    default CreateRoleProductAssignmentRequest mapRoleProduct(
            CreateProductAssignmentsRequestDTO createProductAssignmentsRequestDTO, int productIndex) {
        return new CreateRoleProductAssignmentRequest()
                .productName(createProductAssignmentsRequestDTO.getProductNames().get(productIndex))
                .appId(createProductAssignmentsRequestDTO.getAppId());
    }

    CreateRoleProductAssignmentRequest map(CreateRoleApplicationAssignmentRequestDTO createRoleProductAssignmentRequestDTO);

    CreateRoleProductsAssignmentRequest map(CreateRoleProductsAssignmentRequestDTO createRoleProductsAssignmentRequestDTO);

    RevokeRoleProductAssignmentRequest map(RevokeRoleApplicationAssignmentRequestDTO revokeRoleApplicationAssignmentRequestDTO);

    RevokeRoleProductsAssignmentRequest map(RevokeRoleProductsAssignmentRequestDTO revokeRoleProductsAssignmentRequestDTO);
}
