package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.domain.image.Image;
import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.dto.image.ImageDto;
import com.kopchak.worldoftoys.exception.exception.image.ext.ImageCompressionException;
import com.kopchak.worldoftoys.exception.exception.image.ext.ImageExceedsMaxSizeException;
import com.kopchak.worldoftoys.exception.exception.image.ext.InvalidImageFileFormatException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface ImageService {
    Image convertMultipartFileToImage(MultipartFile multipartFile, Product product)
            throws InvalidImageFileFormatException, ImageCompressionException, ImageExceedsMaxSizeException;

    Optional<ImageDto> decompressImage(Image image);
}
