package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.exception.exception.ImageException;
import com.kopchak.worldoftoys.model.image.Image;
import com.kopchak.worldoftoys.model.product.Product;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    Image convertMultipartFileToImage(MultipartFile multipartFile, Product product) throws ImageException;
    byte[] compressImage(MultipartFile multipartFile) throws ImageException;
    byte[] decompressImage(byte[] data);
}
