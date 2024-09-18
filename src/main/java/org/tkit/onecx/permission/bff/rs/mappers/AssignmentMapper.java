package org.tkit.onecx.permission.bff.rs.mappers;

import jakarta.inject.Inject;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import gen.org.tkit.onecx.permission.bff.rs.internal.model.*;
import gen.org.tkit.onecx.permission.client.model.*;
import gen.org.tkit.onecx.permission.exim.client.model.AssignmentSnapshot;
import gen.org.tkit.onecx.permission.exim.client.model.ExportAssignmentsRequest;

@Mapper(uses = { OffsetDateTimeMapper.class })
public abstract class AssignmentMapper {

    @Inject
    ObjectMapper mapper;

    public abstract CreateAssignmentRequest map(CreateAssignmentRequestDTO createAssignmentRequestDTO);

    public abstract AssignmentDTO map(Assignment assignment);

    @Mapping(target = "appIds", source = "appIds")
    public abstract AssignmentSearchCriteria map(AssignmentSearchCriteriaDTO assignmentSearchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    public abstract AssignmentPageResultDTO map(AssignmentPageResult pageResult);

    public abstract CreateRoleProductsAssignmentRequest mapRoleProducts(
            CreateProductAssignmentsRequestDTO createProductAssignmentsRequestDTO);

    public CreateRoleProductAssignmentRequest mapRoleProduct(
            CreateProductAssignmentsRequestDTO createProductAssignmentsRequestDTO, int productIndex) {
        return new CreateRoleProductAssignmentRequest()
                .productName(createProductAssignmentsRequestDTO.getProductNames().get(productIndex))
                .appId(createProductAssignmentsRequestDTO.getAppId());
    }

    public abstract CreateRoleProductAssignmentRequest map(
            CreateRoleApplicationAssignmentRequestDTO createRoleProductAssignmentRequestDTO);

    public abstract CreateRoleProductsAssignmentRequest map(
            CreateRoleProductsAssignmentRequestDTO createRoleProductsAssignmentRequestDTO);

    public abstract RevokeRoleProductAssignmentRequest map(
            RevokeRoleApplicationAssignmentRequestDTO revokeRoleApplicationAssignmentRequestDTO);

    public abstract RevokeRoleProductsAssignmentRequest map(
            RevokeRoleProductsAssignmentRequestDTO revokeRoleProductsAssignmentRequestDTO);

    public AssignmentSnapshot createSnapshot(Object object) {
        return mapper.convertValue(object, AssignmentSnapshot.class);
    }

    public abstract ExportAssignmentsRequest map(ExportAssignmentsRequestDTO exportAssignmentsRequestDTO);

}
