package com.kopchak.worldoftoys.dto.image;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImageDto {
    private String name;
    private String type;
    private byte[] image;
}
