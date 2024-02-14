package org.tkit.onecx.permission.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.permission.bff.rs.mappers.ApplicationMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.permission.bff.rs.internal.ApplicationApiService;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.ApplicationPageResultDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.ApplicationSearchCriteriaDTO;
import gen.org.tkit.onecx.permission.client.api.ApplicationInternalApi;
import gen.org.tkit.onecx.permission.client.model.ApplicationPageResult;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class ApplicationRestController implements ApplicationApiService {

    @RestClient
    @Inject
    ApplicationInternalApi applicationClient;

    @Inject
    ApplicationMapper mapper;

    @Override
    public Response searchApplications(ApplicationSearchCriteriaDTO applicationSearchCriteriaDTO) {
        try (Response response = applicationClient.searchApplications(mapper.map(applicationSearchCriteriaDTO))) {
            ApplicationPageResultDTO responseDTO = mapper.map(response.readEntity(ApplicationPageResult.class));
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @ServerExceptionMapper
    public Response restException(WebApplicationException ex) {
        return Response.status(ex.getResponse().getStatus()).build();
    }
}
