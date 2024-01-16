package com.kopchak.worldoftoys.dto.product.image;

import lombok.Builder;

@Builder
public record ImageDto(String name, String type, byte[] image) {
}
