package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.domain.image.Image;
import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.exception.exception.image.ext.ImageExceedsMaxSizeException;
import com.kopchak.worldoftoys.exception.exception.image.ext.InvalidImageFileFormatException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceImplTest {
    @InjectMocks
    private ImageServiceImpl imageService;

    @Test
    public void convertMultipartFileToImage_ImageContentType_ReturnsImage() throws Exception {
        String filename = "filename";
        String imageContentType = "image/jpg";
        String imageExtension = ".jpg";
        String namePattern = filename.concat("[a-zA-Z0-9]{4}").concat(imageExtension);
        Pattern pattern = Pattern.compile(namePattern, Pattern.CASE_INSENSITIVE);
        byte[] imageBytes = filename.getBytes();
        MultipartFile multipartFile = mock(MultipartFile.class);
        Product product = new Product();

        when(multipartFile.getOriginalFilename()).thenReturn(filename);
        when(multipartFile.getContentType()).thenReturn(imageContentType);
        when(multipartFile.getBytes()).thenReturn(imageBytes);

        Image returnedImage = imageService.convertMultipartFileToImage(multipartFile, product);

        assertThat(returnedImage).isNotNull();
        assertThat(returnedImage.getImage()).isNotNull();
        assertThat(returnedImage.getImage()).isNotEmpty();
        assertThat(returnedImage.getProduct()).isEqualTo(product);
        assertThat(returnedImage.getType()).isEqualTo(imageContentType);
        assertThat(pattern.matcher(returnedImage.getName()).find()).isTrue();
    }

    @Test
    public void convertMultipartFileToImage_NonImageContentType_ThrowsInvalidImageFileFormatException() {
        String filename = "filename";
        MultipartFile multipartFile = mock(MultipartFile.class);
        Product product = new Product();
        String invalidImageFileFormatExceptionMsg =
                String.format("The file with name: %s must have an image type", filename);

        when(multipartFile.getOriginalFilename()).thenReturn(filename);
        when(multipartFile.getContentType()).thenReturn(null);

        assertException(InvalidImageFileFormatException.class, invalidImageFileFormatExceptionMsg,
                () -> imageService.convertMultipartFileToImage(multipartFile, product));
    }

    @Test
    public void convertMultipartFileToImage_LargeImageString_ThrowsImageExceedsMaxSizeException() throws IOException {
        String filename = "filename";
        String imageContentType = "image/jpg";
        String image = "image".repeat(100_000_000);
        byte[] imageBytes = image.getBytes();
        MultipartFile multipartFile = mock(MultipartFile.class);
        Product product = new Product();
        String imageExceedsMaxSizeExceptionMsg = String.format("The image with name: %s is too large", filename);

        when(multipartFile.getOriginalFilename()).thenReturn(filename);
        when(multipartFile.getContentType()).thenReturn(imageContentType);
        when(multipartFile.getBytes()).thenReturn(imageBytes);

        assertException(ImageExceedsMaxSizeException.class, imageExceedsMaxSizeExceptionMsg,
                () -> imageService.convertMultipartFileToImage(multipartFile, product));
    }

    private void assertException(Class<? extends Exception> expectedExceptionType, String expectedMessage,
                                 Executable executable) {
        Exception exception = assertThrows(expectedExceptionType, executable);
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }
}