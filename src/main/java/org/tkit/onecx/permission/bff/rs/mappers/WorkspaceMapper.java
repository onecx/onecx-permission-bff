package org.tkit.onecx.permission.bff.rs.mappers;

import java.util.ArrayList;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.permission.bff.rs.internal.model.*;
import gen.org.tkit.onecx.permission.client.model.WorkspacePageResult;
import gen.org.tkit.onecx.permission.client.model.WorkspaceSearchCriteria;
import gen.org.tkit.onecx.product.store.client.model.ProductsAbstract;
import gen.org.tkit.onecx.product.store.client.model.ProductsLoadResult;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface WorkspaceMapper {

    @Mapping(target = "removeMsItem", ignore = true)
    @Mapping(target = "removeMfeItem", ignore = true)
    @Mapping(target = "productName", source = "name")
    @Mapping(target = "ms", source = "microservices")
    @Mapping(target = "mfe", source = "microfrontends")
    ProductDetailsDTO map(ProductsAbstract productsAbstract);

    List<ProductDetailsDTO> map(List<ProductsAbstract> productsAbstracts);

    default WorkspaceDetailsDTO map(List<String> workspaceRoles, ProductsLoadResult productsLoadResult) {
        WorkspaceDetailsDTO workspaceDetailsDTO = new WorkspaceDetailsDTO();
        workspaceDetailsDTO.setWorkspaceRoles(workspaceRoles);
        if (productsLoadResult.getStream() != null) {
            workspaceDetailsDTO.setProducts(map(productsLoadResult.getStream()));
        } else {
            workspaceDetailsDTO.setProducts(new ArrayList<>());
        }
        return workspaceDetailsDTO;
    }

    @Mapping(target = "baseUrl", ignore = true)
    WorkspaceSearchCriteria map(WorkspaceSearchCriteriaDTO criteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    WorkspacePageResultDTO map(WorkspacePageResult pageResult);
}
