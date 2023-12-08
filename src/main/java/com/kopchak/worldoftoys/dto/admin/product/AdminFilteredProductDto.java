package com.kopchak.worldoftoys.dto.admin.product;

import com.kopchak.worldoftoys.dto.image.ImageDto;
import lombok.Builder;

import java.math.BigDecimal;
import java.math.BigInteger;

@Builder
public record AdminFilteredProductDto(Integer id, String name, String slug, BigDecimal price,
                                      BigInteger availableQuantity, ImageDto mainImage) {
}