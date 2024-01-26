package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.product.image.ImageDto;
import com.kopchak.worldoftoys.exception.exception.image.ext.ImageCompressionException;
import com.kopchak.worldoftoys.exception.exception.image.ext.ImageDecompressionException;
import com.kopchak.worldoftoys.exception.exception.image.ext.ImageExceedsMaxSizeException;
import com.kopchak.worldoftoys.exception.exception.image.ext.InvalidImageFileFormatException;
import com.kopchak.worldoftoys.domain.image.Image;
import com.kopchak.worldoftoys.domain.product.Product;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    Image convertMultipartFileToImage(MultipartFile multipartFile, Product product)
            throws InvalidImageFileFormatException, ImageCompressionException, ImageExceedsMaxSizeException;

    ImageDto decompressImage(Image image) throws ImageDecompressionException;
}
