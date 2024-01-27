package com.kopchak.worldoftoys.dto.product;

import com.kopchak.worldoftoys.dto.image.ImageDto;
import lombok.Builder;

import java.math.BigDecimal;
import java.math.BigInteger;

@Builder
public record FilteredProductDto(String name, String slug, BigDecimal price, BigInteger availableQuantity,
                                 ImageDto mainImage) {
}
