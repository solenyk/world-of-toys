package com.kopchak.worldoftoys.mapper;

import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.model.product.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",
        uses = {ImageMapper.class})
public interface ProductMapper {
    ProductDto toProductDto(Product product);
}
