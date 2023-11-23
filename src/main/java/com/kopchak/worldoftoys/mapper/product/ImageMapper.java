package com.kopchak.worldoftoys.mapper.product;

import com.kopchak.worldoftoys.dto.image.ImageDto;
import com.kopchak.worldoftoys.model.image.Image;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    ImageDto toImageDto(Image image);
}
