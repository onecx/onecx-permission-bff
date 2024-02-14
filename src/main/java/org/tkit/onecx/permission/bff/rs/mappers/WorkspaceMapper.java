package org.tkit.onecx.permission.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.permission.bff.rs.internal.model.ProductDTO;
import gen.org.tkit.onecx.permission.client.model.Product;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface WorkspaceMapper {
    ProductDTO[] map(Product[] products);

    ProductDTO map(Product product);
}
