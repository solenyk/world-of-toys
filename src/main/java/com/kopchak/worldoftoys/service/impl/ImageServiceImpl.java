package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.exception.exception.ImageException;
import com.kopchak.worldoftoys.model.image.Image;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.repository.product.image.ImageRepository;
import com.kopchak.worldoftoys.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    private final ImageRepository imageRepository;

    public Image convertMultipartFileToImage(MultipartFile multipartFile, Product product) throws ImageException {
        if (isNonImageFile(multipartFile)) {
            throw new ImageException(String.format("File with name: %s must have an image type",
                    multipartFile.getOriginalFilename()));
        }
        Image image = imageRepository
                .findByNameAndProduct_Id(multipartFile.getOriginalFilename(), product.getId())
                .orElse(
                        Image
                                .builder()
                                .name(Objects.requireNonNull(multipartFile.getOriginalFilename())
                                        .concat(RandomStringUtils.randomAlphanumeric(4)))
                                .type(multipartFile.getContentType())
                                .product(product)
                                .build()
                );
        image.setImage(compressImage(multipartFile));
        return image;
    }

    @Override
    public boolean hasNonImageFiles(MultipartFile mainImage, List<MultipartFile> images) {
        if (isNonImageFile(mainImage)) {
            return true;
        }

        for (MultipartFile image : images) {
            if (isNonImageFile(image)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public byte[] compressImage(MultipartFile multipartFile) throws ImageException {
        try {
            byte[] data = multipartFile.getBytes();
            Deflater deflater = new Deflater();
            deflater.setLevel(Deflater.BEST_COMPRESSION);
            deflater.setInput(data);
            deflater.finish();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
            byte[] tmp = new byte[4 * 1024];
            while (!deflater.finished()) {
                int size = deflater.deflate(tmp);
                outputStream.write(tmp, 0, size);
            }
            outputStream.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new ImageException(String.format("File with name: %s cannot be compressed",
                    multipartFile.getOriginalFilename()));
        }
    }

    @Override
    public byte[] decompressImage(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] tmp = new byte[4 * 1024];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(tmp);
                outputStream.write(tmp, 0, count);
            }
            outputStream.close();
        } catch (Exception exception) {
        }
        return outputStream.toByteArray();
    }

    private boolean isNonImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null) {
            return !contentType.startsWith("image/");
        }
        return true;
    }
}
