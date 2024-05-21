package org.tkit.onecx.permission.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.permission.bff.rs.mappers.AssignmentMapper;
import org.tkit.onecx.permission.bff.rs.mappers.ExceptionMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.permission.bff.rs.internal.AssignmentApiService;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.*;
import gen.org.tkit.onecx.permission.client.api.AssignmentInternalApi;
import gen.org.tkit.onecx.permission.client.model.Assignment;
import gen.org.tkit.onecx.permission.client.model.AssignmentPageResult;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class AssignmentRestController implements AssignmentApiService {

    @RestClient
    @Inject
    AssignmentInternalApi assignmentClient;

    @Inject
    AssignmentMapper mapper;

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

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public Response restException(WebApplicationException ex) {
        return Response.status(ex.getResponse().getStatus()).build();
    }
}
