package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.product.image.ImageDto;
import com.kopchak.worldoftoys.exception.ImageCompressionException;
import com.kopchak.worldoftoys.exception.ImageDecompressionException;
import com.kopchak.worldoftoys.exception.ImageExceedsMaxSizeException;
import com.kopchak.worldoftoys.exception.InvalidImageFileFormatException;
import com.kopchak.worldoftoys.domain.image.Image;
import com.kopchak.worldoftoys.domain.product.Product;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    Image convertMultipartFileToImage(MultipartFile multipartFile, Product product)
            throws InvalidImageFileFormatException, ImageCompressionException, ImageExceedsMaxSizeException;

    ImageDto generateDecompressedImageDto(Image image) throws ImageDecompressionException;
}
