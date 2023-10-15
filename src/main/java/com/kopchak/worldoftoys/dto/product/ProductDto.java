package com.kopchak.worldoftoys.dto.product;

import com.kopchak.worldoftoys.dto.image.ImageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.BigInteger;

@Getter
@Builder
@AllArgsConstructor
public class ProductDto {
    private String name;

    private String slug;

    private BigDecimal price;

    private BigInteger availableQuantity;

    private ImageDto mainImage;
}
