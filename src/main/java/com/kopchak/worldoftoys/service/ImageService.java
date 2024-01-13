package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.image.ImageDto;
import com.kopchak.worldoftoys.exception.exception.ImageCompressionException;
import com.kopchak.worldoftoys.exception.exception.ImageDecompressionException;
import com.kopchak.worldoftoys.exception.exception.ImageExceedsMaxSizeException;
import com.kopchak.worldoftoys.exception.exception.InvalidImageFileFormatException;
import com.kopchak.worldoftoys.model.image.Image;
import com.kopchak.worldoftoys.model.product.Product;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    Image convertMultipartFileToImage(MultipartFile multipartFile, Product product) throws InvalidImageFileFormatException, ImageCompressionException, ImageExceedsMaxSizeException;
    ImageDto generateDecompressedImageDto(Image image) throws ImageDecompressionException;
}
