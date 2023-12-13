package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.exception.exception.ImageException;
import com.kopchak.worldoftoys.model.image.Image;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.repository.product.image.ImageRepository;
import com.kopchak.worldoftoys.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {
    private final ImageRepository imageRepository;

    @Override
    public Image convertMultipartFileToImage(MultipartFile multipartFile, Product product) throws ImageException {
        if (isNonImageFile(multipartFile)) {
            String fileName = multipartFile.getOriginalFilename();
            log.error("File with name: {} must have an image type", fileName);
            throw new ImageException(String.format("File with name: %s must have an image type", fileName));
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

    private byte[] compressImage(MultipartFile multipartFile) throws ImageException {
        try {
            byte[] data = multipartFile.getBytes();
            Deflater deflater = new Deflater();
            deflater.setLevel(Deflater.BEST_COMPRESSION);
            deflater.setInput(data);
            deflater.finish();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
            byte[] compressedData = compressData(deflater, outputStream);
            outputStream.close();
            return compressedData;
        } catch (IOException e) {
            String fileName = multipartFile.getOriginalFilename();
            log.error("File with name: {} cannot be compressed", fileName);
            throw new ImageException(String.format("File with name: %s cannot be compressed", fileName));
        }
    }

    private byte[] compressData(Deflater deflater, ByteArrayOutputStream outputStream) throws IOException {
        byte[] tmp = new byte[4 * 1024];
        while (!deflater.finished()) {
            int size = deflater.deflate(tmp);
            outputStream.write(tmp, 0, size);
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
