package com.kopchak.worldoftoys.dto.image;

import lombok.Builder;

@Builder
public record ImageDto(String name, String type, byte[] image) {
}
