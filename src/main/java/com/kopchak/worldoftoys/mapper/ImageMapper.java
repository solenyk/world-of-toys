package com.kopchak.worldoftoys.mapper;

import com.kopchak.worldoftoys.dto.image.ImageDto;
import com.kopchak.worldoftoys.model.image.Image;

public class ImageMapper {
    public ImageDto toImageDto(Image image) {
        return ImageDto
                .builder()
                .name(image.getName())
                .type(image.getType())
                .image(image.getImage())
                .build();
    }
}
