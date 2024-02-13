package org.tkit.onecx.permission.bff.rs.controllers;

import gen.org.tkit.onecx.permission.bff.rs.internal.WorkspaceApiService;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.WorkspacePageResultDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.WorkspaceSearchCriteriaDTO;
import gen.org.tkit.onecx.permission.client.api.WorkspaceExternalApi;
import gen.org.tkit.onecx.permission.client.model.WorkspacePageResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.permission.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.permission.bff.rs.mappers.WorkspaceMapper;
import org.tkit.quarkus.log.cdi.LogService;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class WorkspaceRestController implements WorkspaceApiService {

  @RestClient
  @Inject
  WorkspaceExternalApi workspaceClient;

  @Inject
  WorkspaceMapper mapper;

  @Inject
  ExceptionMapper exceptionMapper;

  @Override
  public Response searchWorkspaces(WorkspaceSearchCriteriaDTO workspaceSearchCriteriaDTO) {
    try (Response response = workspaceClient.searchWorkspaces(mapper.map(workspaceSearchCriteriaDTO))) {
      WorkspacePageResultDTO responseDTO = mapper.map(response.readEntity(WorkspacePageResult.class));
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
