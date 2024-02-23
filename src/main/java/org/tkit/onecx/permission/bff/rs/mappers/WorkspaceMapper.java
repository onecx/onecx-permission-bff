package org.tkit.onecx.permission.bff.rs.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.permission.bff.rs.internal.model.ProductDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.ProductDetailsDTO;
import gen.org.tkit.onecx.permission.bff.rs.internal.model.WorkspaceDetailsDTO;
import gen.org.tkit.onecx.permission.client.model.Product;
import gen.org.tkit.onecx.product.store.client.model.ProductsAbstract;
import gen.org.tkit.onecx.product.store.client.model.ProductsLoadResult;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface WorkspaceMapper {
    ProductDTO[] map(Product[] products);

    ProductDTO map(Product product);

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
        workspaceDetailsDTO.setProducts(map(productsLoadResult.getStream()));
        return workspaceDetailsDTO;
    }
}
