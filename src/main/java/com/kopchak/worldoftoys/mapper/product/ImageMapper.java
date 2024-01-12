package com.kopchak.worldoftoys.mapper.product;

import com.kopchak.worldoftoys.dto.image.ImageDto;
import com.kopchak.worldoftoys.exception.exception.ImageCompressionException;
import com.kopchak.worldoftoys.model.image.Image;
import com.kopchak.worldoftoys.service.ImageService;
import org.mapstruct.Context;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    default ImageDto toImageDto(Image image, @Context ImageService imageService) throws ImageCompressionException {
        String imageName = image.getName();
        byte[] decompressedImg = imageService.decompressImage(image.getImage(), imageName);
        return ImageDto.builder()
                .name(imageName)
                .type(image.getType())
                .image(decompressedImg)
                .build();
    }

//    List<ImageDto> toImageDtoList(Set<Image> set);
}
