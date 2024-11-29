package org.tkit.onecx.permission.bff.rs.mappers;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.permission.bff.rs.internal.model.ProblemDetailInvalidParamDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.ProblemDetailParamDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.permission.exim.client.model.EximProblemDetailResponse;
import gen.org.tkit.onecx.permission.model.ProblemDetailResponse;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ExceptionMapper {

    default RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        var dto = exception("CONSTRAINT_VIOLATIONS", ex.getMessage());
        dto.setInvalidParams(createErrorValidationResponse(ex.getConstraintViolations()));
        return RestResponse.status(Response.Status.BAD_REQUEST, dto);
    }

    @Mapping(target = "removeParamsItem", ignore = true)
    @Mapping(target = "params", ignore = true)
    @Mapping(target = "invalidParams", ignore = true)
    @Mapping(target = "removeInvalidParamsItem", ignore = true)
    ProblemDetailResponseDTO exception(String errorCode, String detail);

    default List<ProblemDetailParamDTO> map(Map<String, Object> params) {
        if (params == null) {
            return List.of();
        }
        return params.entrySet().stream().map(e -> {
            var item = new ProblemDetailParamDTO();
            item.setKey(e.getKey());
            if (e.getValue() != null) {
                item.setValue(e.getValue().toString());
            }
            return item;
        }).toList();
    }

    List<ProblemDetailInvalidParamDTO> createErrorValidationResponse(
            Set<ConstraintViolation<?>> constraintViolation);

    @Mapping(target = "name", source = "propertyPath")
    @Mapping(target = "message", source = "message")
    ProblemDetailInvalidParamDTO createError(ConstraintViolation<?> constraintViolation);

    default String mapPath(Path path) {
        return path.toString();
    }

    default Response clientException(ClientWebApplicationException ex) {
        if (ex.getResponse().getStatus() == 500) {
            return Response.status(400).build();
        } else if (ex.getResponse().getStatus() == 409) {
            return Response.status(ex.getResponse().getStatus())
                    .entity(mapImportException(ex.getResponse().readEntity(EximProblemDetailResponse.class))).build();
        } else {
            if (ex.getResponse().getMediaType() != null
                    && ex.getResponse().getMediaType().toString().contains(APPLICATION_JSON)) {
                return Response.status(ex.getResponse().getStatus())
                        .entity(map(ex.getResponse().readEntity(ProblemDetailResponse.class))).build();
            } else {
                return Response.status(ex.getResponse().getStatus()).build();
            }
        }
    }

    @Mapping(target = "removeParamsItem", ignore = true)
    @Mapping(target = "removeInvalidParamsItem", ignore = true)
    ProblemDetailResponseDTO mapImportException(EximProblemDetailResponse eximProblemDetailResponse);

    @Mapping(target = "removeParamsItem", ignore = true)
    @Mapping(target = "removeInvalidParamsItem", ignore = true)
    ProblemDetailResponseDTO map(ProblemDetailResponse problemDetailResponse);
}
