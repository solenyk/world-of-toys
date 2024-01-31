package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.domain.image.Image;
import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.dto.image.ImageDto;
import com.kopchak.worldoftoys.exception.exception.image.ext.ImageCompressionException;
import com.kopchak.worldoftoys.exception.exception.image.ext.ImageExceedsMaxSizeException;
import com.kopchak.worldoftoys.exception.exception.image.ext.InvalidImageFileFormatException;
import com.kopchak.worldoftoys.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {
    private final static int MAX_IMG_COMPRESSION_SIZE = 100_000;
    private final static String IMAGE_CONTENT_TYPE_PREFIX = "image/";

    @Override
    public Image convertMultipartFileToImage(MultipartFile multipartFile, Product product)
            throws InvalidImageFileFormatException, ImageCompressionException, ImageExceedsMaxSizeException {
        String fileName = multipartFile.getOriginalFilename();
        if (isNonImageFile(multipartFile)) {
            String errMsg = String.format("The file with name: %s must have an image type", fileName);
            log.error(errMsg);
            throw new InvalidImageFileFormatException(errMsg);
        }
        byte[] compressedImg = compressImage(multipartFile, fileName);
        if (compressedImg.length > MAX_IMG_COMPRESSION_SIZE) {
            String errMsg = String.format("The image with name: %s is too large", fileName);
            log.error(errMsg);
            throw new ImageExceedsMaxSizeException(errMsg);
        }
        String generatedName = generateImageName(multipartFile);
        return Image
                .builder()
                .name(generatedName)
                .type(multipartFile.getContentType())
                .image(compressedImg)
                .product(product)
                .build();
    }

    @Override
    public Optional<ImageDto> decompressImage(Image image) {
        return Optional.ofNullable(image)
                .map(img -> {
                    try {
                        String imageName = img.getName();
                        byte[] decompressedImg = decompressImageData(img.getImage());
                        log.info("The image with name: {} was successfully decompressed", imageName);
                        return Optional.of(new ImageDto(imageName, img.getType(), decompressedImg));
                    } catch (IOException | DataFormatException e) {
                        log.error("Error decompressing the image with name: {}", img.getName(), e);
                        return Optional.<ImageDto>empty();
                    }
                })
                .orElse(Optional.empty());
    }

    private boolean isNonImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null) {
            return !contentType.startsWith(IMAGE_CONTENT_TYPE_PREFIX);
        }
        return true;
    }

    private byte[] compressImage(MultipartFile multipartFile, String fileName) throws ImageCompressionException {
        try {
            byte[] data = multipartFile.getBytes();
            Deflater deflater = new Deflater();
            deflater.setLevel(Deflater.BEST_COMPRESSION);
            deflater.setInput(data);
            deflater.finish();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
            byte[] compressedData = compressData(deflater, outputStream);
            outputStream.close();
            log.info("The image with name: {} was successfully compressed", fileName);
            return compressedData;
        } catch (IOException e) {
            String errorMsg = String.format("The image with name: %s cannot be compressed", fileName);
            log.error(errorMsg);
            throw new ImageCompressionException(errorMsg);
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

    private String generateImageName(MultipartFile multipartFile) {
        String fileExtension = ".".concat(multipartFile.getContentType().replace(IMAGE_CONTENT_TYPE_PREFIX, ""));
        String randString = RandomStringUtils.randomAlphanumeric(4);
        String filename = multipartFile.getOriginalFilename();
        return filename == null ? randString.concat(fileExtension) :
                filename.replace(fileExtension, "").concat(randString).concat(fileExtension);
    }

    private byte[] decompressImageData(byte[] compressedData) throws IOException, DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(compressedData);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(compressedData.length)) {
            byte[] buffer = new byte[4 * 1024];

            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            return outputStream.toByteArray();
        }
    }
}
