package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.domain.image.Image;
import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.dto.image.ImageDto;
import com.kopchak.worldoftoys.exception.exception.image.ImageCompressionException;
import com.kopchak.worldoftoys.exception.exception.image.ImageExceedsMaxSizeException;
import com.kopchak.worldoftoys.exception.exception.image.InvalidImageFileFormatException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Transactional
@ExtendWith(MockitoExtension.class)
class ImageServiceTest {
    @InjectMocks
    private ImageService imageService;

    private final static String FILENAME = "filename";
    private final static String IMAGE_CONTENT_TYPE = "image/jpg";
    private final static MultipartFile MULTIPART_FILE = mock(MultipartFile.class);

    private Product product;
    private byte[] imageBytes;
    private Image image;

    @BeforeEach
    void setUp() {
        product = new Product();
        imageBytes = "image".getBytes();
        image = Image.builder()
                .name(FILENAME)
                .image(imageBytes)
                .type(IMAGE_CONTENT_TYPE)
                .build();
    }

    @Test
    public void convertMultipartFileToImage_ImageContentType_ReturnsOptionalOfImage() throws Exception {
        String imageExtension = ".jpg";
        String namePattern = FILENAME.concat("[a-zA-Z0-9]{4}").concat(imageExtension);
        Pattern pattern = Pattern.compile(namePattern, Pattern.CASE_INSENSITIVE);

        when(MULTIPART_FILE.getOriginalFilename()).thenReturn(FILENAME);
        when(MULTIPART_FILE.getContentType()).thenReturn(IMAGE_CONTENT_TYPE);
        when(MULTIPART_FILE.getBytes()).thenReturn(imageBytes);

        Optional<Image> returnedImage = imageService.convertMultipartFileToImage(MULTIPART_FILE, product);

        assertThat(returnedImage).isPresent();
        assertThat(returnedImage.get().getImage()).isNotNull();
        assertThat(returnedImage.get().getImage()).isNotEmpty();
        assertThat(returnedImage.get().getProduct()).isEqualTo(product);
        assertThat(returnedImage.get().getType()).isEqualTo(IMAGE_CONTENT_TYPE);
        assertThat(pattern.matcher(returnedImage.get().getName()).find()).isTrue();
    }

    @Test
    public void convertMultipartFileToImage_NullImageContentType_ThrowsInvalidImageFileFormatException() {
        String invalidImageFileFormatExceptionMsg =
                String.format("The file with name: %s must have an image type", FILENAME);

        when(MULTIPART_FILE.getOriginalFilename()).thenReturn(FILENAME);
        when(MULTIPART_FILE.getContentType()).thenReturn(null);

        assertException(InvalidImageFileFormatException.class, invalidImageFileFormatExceptionMsg,
                () -> imageService.convertMultipartFileToImage(MULTIPART_FILE, product));
    }

    @Test
    public void convertMultipartFileToImage_LargeImageString_ThrowsImageExceedsMaxSizeException() throws IOException {
        String image = "image".repeat(100_000_000);
        byte[] imageBytes = image.getBytes();
        MultipartFile multipartFile = mock(MultipartFile.class);
        String imageExceedsMaxSizeExceptionMsg = String.format("The image with name: %s is too large", FILENAME);

        when(multipartFile.getOriginalFilename()).thenReturn(FILENAME);
        when(multipartFile.getContentType()).thenReturn(IMAGE_CONTENT_TYPE);
        when(multipartFile.getBytes()).thenReturn(imageBytes);

        assertException(ImageExceedsMaxSizeException.class, imageExceedsMaxSizeExceptionMsg,
                () -> imageService.convertMultipartFileToImage(multipartFile, product));
    }

    @Test
    public void convertMultipartFileToImage_ThrowIOException_ThrowsImageCompressionException() throws IOException {
        String imageCompressionExceptionMsg = String.format("The image with name: %s cannot be compressed", FILENAME);

        when(MULTIPART_FILE.getOriginalFilename()).thenReturn(FILENAME);
        when(MULTIPART_FILE.getContentType()).thenReturn(IMAGE_CONTENT_TYPE);
        when(MULTIPART_FILE.getBytes()).thenThrow(new IOException());

        assertException(ImageCompressionException.class, imageCompressionExceptionMsg,
                () -> imageService.convertMultipartFileToImage(MULTIPART_FILE, product));
    }

    @Test
    public void decompressImage_Image_ReturnsOptionalOfImageDto() {
        String base64CompressedImageData = "eNrrDPBz5+WS4mJgYOD19HAJAtKsIMzBAiTV097lASmFZI8gXwaGKjUGhoYWBoZfQKGGF" +
                "wwMpQYMDK8SGBisZjAwiBfM2RVoA5RgSvJ2d2H4395/Zj+Qx1ngEVnMwMA3C4QZ26eLuwEF2Us8fV3Zn3PxCIsLR6z79wgoZOHp" +
                "4hjCcZ314EbeBgUGhg1SncyFD/X1kr4wnhf8eexky9ECxm0qZzNbb3+wyunm3CXl06/RUbPp0ab/Fn+5e9evWXcHaIZqiWtESUp" +
                "iSapVclEqkGIwMjAy1jU00DU0CjG0tDIytjK01DYwsDIwWFC26SiKhtz8lMy0StwaLmpzVQI1aMA1lGTmphaXJOYW4NbTZqe9DKh" +
                "HEqQnOD+tpDyxKJWhvLxcLzMvuzg5sSBVL78offY7GymgIgZPVz+XdU4JTQAvPnSb";
        byte[] compressedImageData = Base64.getDecoder().decode(base64CompressedImageData);
        image.setImage(compressedImageData);

        Optional<ImageDto> imageDto = imageService.decompressImage(image);

        assertThat(imageDto).isPresent();
        assertThat(imageDto.get().name()).isEqualTo(FILENAME);
        assertThat(imageDto.get().type()).isEqualTo(IMAGE_CONTENT_TYPE);
        assertThat(imageDto.get().image()).isNotEmpty();
    }

    private void assertException(Class<? extends Exception> expectedExceptionType, String expectedMessage,
                                 Executable executable) {
        Exception exception = assertThrows(expectedExceptionType, executable);
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }
}