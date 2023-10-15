package com.kopchak.worldoftoys.dto.image;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ImageDto {
    private String name;
    private String type;
    private byte[] image;
}
