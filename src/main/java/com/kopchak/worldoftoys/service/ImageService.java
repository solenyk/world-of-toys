package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.exception.exception.ImageException;
import com.kopchak.worldoftoys.model.image.Image;
import com.kopchak.worldoftoys.model.product.Product;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    Image convertMultipartFileToImage(MultipartFile multipartFile, Product product) throws ImageException;
    boolean hasNonImageFiles(MultipartFile mainImage, List<MultipartFile> images);
    byte[] compressImage(MultipartFile multipartFile) throws ImageException;
    byte[] decompressImage(byte[] data);
}
