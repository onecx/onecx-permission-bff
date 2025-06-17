package org.tkit.onecx.permission.bff.rs.controllers;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.onecx.permission.bff.rs.mappers.AssignmentMapper;
import org.tkit.onecx.permission.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.permission.bff.rs.mappers.UserMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.iam.client.api.AdminControllerApi;
import gen.org.tkit.onecx.iam.client.model.RoleIamV1;
import gen.org.tkit.onecx.iam.client.model.UserRolesResponseIamV1;
import gen.org.tkit.onecx.permission.bff.rs.internal.AssignmentApiService;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.*;
import gen.org.tkit.onecx.permission.client.api.AssignmentInternalApi;
import gen.org.tkit.onecx.permission.client.api.EximInternalApi;
import gen.org.tkit.onecx.permission.client.model.Assignment;
import gen.org.tkit.onecx.permission.client.model.AssignmentPageResult;
import gen.org.tkit.onecx.permission.client.model.AssignmentSnapshot;
import gen.org.tkit.onecx.permission.client.model.UserAssignmentPageResult;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class AssignmentRestController implements AssignmentApiService {

    private static final Logger log = LoggerFactory.getLogger(AssignmentRestController.class);
    @RestClient
    @Inject
    AssignmentInternalApi assignmentClient;

    @RestClient
    @Inject
    EximInternalApi assignmentEximClient;

    @RestClient
    @Inject
    AdminControllerApi iamClient;

    @Inject
    AssignmentMapper mapper;

    @Inject
    UserMapper userMapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response createAssignment(CreateAssignmentRequestDTO createAssignmentRequestDTO) {
        try (Response response = assignmentClient
                .createAssignment(mapper.map(createAssignmentRequestDTO))) {
            AssignmentDTO responseDTO = mapper.map(response.readEntity(Assignment.class));
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @Override
    public Response grantRoleAssignments(String roleId) {
        try (Response response = assignmentClient.grantRoleAssignments(roleId)) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response grantRoleApplicationAssignments(String roleId,
            CreateRoleApplicationAssignmentRequestDTO createRoleApplicationAssignmentRequestDTO) {
        try (Response response = assignmentClient.grantRoleApplicationAssignments(roleId,
                mapper.map(createRoleApplicationAssignmentRequestDTO))) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response grantRoleProductsAssignments(String roleId,
            CreateRoleProductsAssignmentRequestDTO createRoleProductsAssignmentRequestDTO) {
        try (Response response = assignmentClient.grantRoleProductsAssignments(roleId,
                mapper.map(createRoleProductsAssignmentRequestDTO))) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response importAssignments(Object dto) {
        try (Response response = assignmentEximClient.importAssignments(mapper.createSnapshot(dto))) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response revokeRoleApplicationAssignments(String roleId,
            RevokeRoleApplicationAssignmentRequestDTO revokeRoleApplicationAssignmentRequestDTO) {
        try (Response response = assignmentClient.revokeRoleApplicationAssignments(roleId,
                mapper.map(revokeRoleApplicationAssignmentRequestDTO))) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response revokeRoleAssignments(String roleId) {
        try (Response response = assignmentClient.revokeRoleAssignments(roleId)) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response revokeRoleProductsAssignments(String roleId,
            RevokeRoleProductsAssignmentRequestDTO revokeRoleProductsAssignmentRequestDTO) {
        try (Response response = assignmentClient.revokeRoleProductsAssignments(roleId,
                mapper.map(revokeRoleProductsAssignmentRequestDTO))) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response deleteAssignment(String id) {
        try (Response response = assignmentClient.deleteAssignment(id)) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response exportAssignments(ExportAssignmentsRequestDTO exportAssignmentsRequestDTO) {
        try (Response response = assignmentEximClient.exportAssignments(mapper.map(exportAssignmentsRequestDTO))) {
            return Response.status(response.getStatus())
                    .entity(response.readEntity(AssignmentSnapshot.class))
                    .build();
        }
    }

    @Override
    public Response getAssignment(String id) {
        try (Response response = assignmentClient.getAssignment(id)) {
            AssignmentDTO responseDTO = mapper.map(response.readEntity(Assignment.class));
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @Override
    public Response searchAssignments(AssignmentSearchCriteriaDTO assignmentSearchCriteriaDTO) {
        try (Response response = assignmentClient.searchAssignments(mapper.map(assignmentSearchCriteriaDTO))) {
            AssignmentPageResultDTO responseDTO = mapper.map(response.readEntity(AssignmentPageResult.class));
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @Override
    public Response searchUserAssignments(AssignmentUserSearchCriteriaDTO assignmentUserSearchCriteriaDTO) {
        UserAssignmentPageResult pageResult;
        List<String> roles = List.of();
        try (Response response = iamClient.getUserRoles(assignmentUserSearchCriteriaDTO.getUserId(),
                mapper.maps(assignmentUserSearchCriteriaDTO))) {
            UserRolesResponseIamV1 roleResponse = response.readEntity(UserRolesResponseIamV1.class);
            if (roleResponse.getRoles() != null) {
                roles = roleResponse.getRoles().stream().map(RoleIamV1::getName).toList();
            }
        } catch (ClientWebApplicationException exception) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
                    .entity(exceptionMapper.exception("400", "USER_NOT_FOUND")).build();
        }
        if (roles.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        try (Response assignmentResponse = assignmentClient
                .searchAssignmentsByRoles(mapper.map(assignmentUserSearchCriteriaDTO, roles))) {
            pageResult = assignmentResponse.readEntity(UserAssignmentPageResult.class);
            return Response.status(assignmentResponse.getStatus()).entity(userMapper.map(pageResult)).build();
        }
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public Response clientRestException(ClientWebApplicationException ex) {
        return exceptionMapper.clientException(ex);
    }
}
