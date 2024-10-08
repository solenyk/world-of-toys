package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.domain.image.Image;
import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.domain.product.category.AgeCategory;
import com.kopchak.worldoftoys.domain.product.category.BrandCategory;
import com.kopchak.worldoftoys.domain.product.category.OriginCategory;
import com.kopchak.worldoftoys.dto.admin.category.CategoryIdDto;
import com.kopchak.worldoftoys.dto.admin.product.AddUpdateProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminFilteredProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductsPageDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.dto.image.ImageDto;
import com.kopchak.worldoftoys.exception.exception.product.DuplicateProductNameException;
import com.kopchak.worldoftoys.exception.exception.product.ProductNotFoundException;
import com.kopchak.worldoftoys.mapper.product.ProductMapper;
import com.kopchak.worldoftoys.repository.product.ProductRepository;
import com.kopchak.worldoftoys.repository.specifications.impl.ProductSpecificationsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ImageService imageService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private ProductSpecificationsImpl productSpecifications;
    @Mock
    private ProductMapper productMapper;
    @InjectMocks
    private ProductService productService;

    private final static String PRODUCT_SLUG = "lyalka-klaymber";
    private final static String PRICE_SORT_ORDER = "asc";
    private final static Integer PRODUCT_ID = 1;
    private final static String PRODUCT_NAME = "лялька";
    private final static String PRODUCT_ID_NOT_FOUND_EXCEPTION_MSG =
            String.format("The product with id: %d is not found.", PRODUCT_ID);
    private final static String DUPLICATE_PRODUCT_NAME_EXCEPTION_MSG =
            String.format("The product with name: %s is already exist", PRODUCT_NAME);

    private final static int PAGE = 0;
    private final static int SIZE = 3;
    private BigDecimal minProductPrice;
    private BigDecimal maxProductPrice;
    private List<String> originCategories;
    private List<String> brandCategories;
    private List<String> ageCategories;
    private Product product;
    private ImageDto imageDto;
    private Specification<Product> spec;
    private Pageable pageable;
    private CategoryIdDto categoryIdDto;
    private AddUpdateProductDto addUpdateProductDto;
    private BrandCategory brandCategory;
    private OriginCategory originCategory;
    private AgeCategory ageCategory;

    @BeforeEach
    void setUp() {
        minProductPrice = BigDecimal.valueOf(350);
        maxProductPrice = BigDecimal.valueOf(1000);
        originCategories = List.of("china", "ukraine");
        brandCategories = List.of("сurlimals", "devilon");
        ageCategories = List.of("do-1-roku", "vid-1-do-3-rokiv");
        product = Product.builder().id(PRODUCT_ID).mainImage(new Image()).images(Set.of(new Image())).build();
        imageDto = ImageDto.builder().build();
        spec = Specification.where(null);
        pageable = PageRequest.of(PAGE, SIZE);
        categoryIdDto = new CategoryIdDto(1);
        addUpdateProductDto = AddUpdateProductDto
                .builder()
                .name(PRODUCT_NAME)
                .brandCategory(categoryIdDto)
                .originCategory(categoryIdDto)
                .ageCategories(List.of(categoryIdDto))
                .build();
        brandCategory = new BrandCategory();
        originCategory = new OriginCategory();
        ageCategory = new AgeCategory();
    }

    @Test
    public void getProductDtoBySlug_ExistentProduct_ReturnsProductDto() throws Exception {
        ProductDto expectedProductDto = ProductDto.builder().build();

        when(productRepository.findBySlug(eq(PRODUCT_SLUG))).thenReturn(Optional.of(product));
        when(imageService.decompressImage(any())).thenReturn(Optional.of(imageDto));
        when(productMapper.toProductDto(eq(product), any(), any())).thenReturn(expectedProductDto);

        ProductDto actualProductDto = productService.getProductBySlug(PRODUCT_SLUG);

        assertThat(actualProductDto).isNotNull();
        assertThat(actualProductDto).isEqualTo(expectedProductDto);
    }

    @Test
    public void getProductDtoBySlug_NonExistentProduct_ThrowsProductNotFoundException() {
        String productNotFoundExceptionMsg = String.format("The product with slug: %s is not found.", PRODUCT_SLUG);

        when(productRepository.findBySlug(eq(PRODUCT_SLUG))).thenReturn(Optional.empty());

        assertException(ProductNotFoundException.class, productNotFoundExceptionMsg,
                () -> productService.getProductBySlug(PRODUCT_SLUG));
    }

    @Test
    public void getFilteredProducts_ReturnsFilteredProductsPageDto() {
        Page<Product> productPage = new PageImpl<>(List.of(product), PageRequest.of(0, 5), 5);
        FilteredProductDto filteredProductDto = FilteredProductDto.builder().build();
        var expectedFilteredProductsPageDto =
                new FilteredProductsPageDto(List.of(filteredProductDto), 5L, 1L);

        when(productSpecifications.filterByAllCriteria(eq(PRODUCT_NAME), eq(minProductPrice), eq(maxProductPrice),
                eq(originCategories), eq(brandCategories), eq(ageCategories), eq(PRICE_SORT_ORDER), any()))
                .thenReturn(spec);
        when(productRepository.findAll(eq(spec), eq(pageable))).thenReturn(productPage);
        when(imageService.decompressImage(eq(product.getMainImage()))).thenReturn(Optional.empty());
        when(productMapper.toFilteredProductDto(eq(product), any())).thenReturn(filteredProductDto);

        var actualFilteredProductsPageDto = productService.getFilteredProductsPage(PAGE, SIZE, PRODUCT_NAME,
                minProductPrice, maxProductPrice, originCategories, brandCategories, ageCategories, PRICE_SORT_ORDER);

        assertThat(actualFilteredProductsPageDto).isNotNull();
        assertThat(actualFilteredProductsPageDto).isEqualTo(expectedFilteredProductsPageDto);
    }

    @Test
    public void getAdminFilteredProducts_ReturnsAdminProductsPageDto() {
        Page<Product> productPage = new PageImpl<>(List.of(product), PageRequest.of(0, 5), 5);
        AdminFilteredProductDto adminFilteredProductDto = AdminFilteredProductDto.builder().build();
        var expectedAdminProductsPageDto =
                new AdminProductsPageDto(List.of(adminFilteredProductDto), 5L, 1L);

        when(productSpecifications.filterByAllCriteria(eq(PRODUCT_NAME), eq(minProductPrice), eq(maxProductPrice),
                eq(originCategories), eq(brandCategories), eq(ageCategories), eq(PRICE_SORT_ORDER), any()))
                .thenReturn(spec);
        when(productRepository.findAll(eq(spec), eq(pageable))).thenReturn(productPage);
        when(imageService.decompressImage(eq(product.getMainImage()))).thenReturn(Optional.empty());
        when(productMapper.toAdminFilteredProductDto(eq(product), any())).thenReturn(adminFilteredProductDto);

        var actualAdminProductsPageDto = productService.getAdminProductsPage(PAGE, SIZE, PRODUCT_NAME,
                minProductPrice, maxProductPrice, originCategories, brandCategories, ageCategories,
                PRICE_SORT_ORDER, null);

        assertThat(actualAdminProductsPageDto).isNotNull();
        assertThat(actualAdminProductsPageDto).isEqualTo(expectedAdminProductsPageDto);
    }

    @Test
    public void getAdminProductDtoById_ExistentProduct_ReturnsAdminProductDto() throws Exception {
        var imageDtoList = Collections.singletonList(imageDto);
        var expectedAdminProductDto = AdminProductDto
                .builder()
                .id(PRODUCT_ID)
                .name(PRODUCT_NAME)
                .slug(PRODUCT_SLUG)
                .price(minProductPrice)
                .mainImage(imageDto)
                .build();

        when(productRepository.findById(eq(PRODUCT_ID))).thenReturn(Optional.of(product));
        when(imageService.decompressImage(any())).thenReturn(Optional.of(imageDto));
        when(productMapper.toAdminProductDto(eq(product), eq(imageDto), eq(imageDtoList)))
                .thenReturn(expectedAdminProductDto);

        var actualAdminProductDto = productService.getProductById(PRODUCT_ID);

        assertThat(actualAdminProductDto).isNotNull();
        assertThat(actualAdminProductDto).isEqualTo(expectedAdminProductDto);
    }

    @Test
    public void getAdminProductDtoById_NonExistentProduct_ThrowProductNotFoundException() {
        when(productRepository.findById(eq(PRODUCT_ID))).thenReturn(Optional.empty());

        assertException(ProductNotFoundException.class, PRODUCT_ID_NOT_FOUND_EXCEPTION_MSG,
                () -> productService.getProductById(PRODUCT_ID));
    }

    @Test
    public void updateProduct_ExistentProductIdAndNonExistentProductName() throws Exception {
        when(productRepository.findById(eq(PRODUCT_ID))).thenReturn(Optional.of(product));
        when(productRepository.findByName(eq(PRODUCT_NAME))).thenReturn(Optional.empty());
        when(productMapper.toProduct(eq(addUpdateProductDto))).thenReturn(product);
        when(categoryService.findCategoryByIdAndType(eq(categoryIdDto.id()), eq(BrandCategory.class)))
                .thenReturn(brandCategory);
        when(categoryService.findCategoryByIdAndType(eq(categoryIdDto.id()), eq(OriginCategory.class)))
                .thenReturn(originCategory);
        when(categoryService.findCategoryByIdAndType(eq(categoryIdDto.id()), eq(AgeCategory.class)))
                .thenReturn(ageCategory);

        productService.updateProduct(PRODUCT_ID, addUpdateProductDto, null, null);

        verify(productRepository).save(eq(product));

        assertThat(product.getBrandCategory()).isEqualTo(brandCategory);
        assertThat(product.getOriginCategory()).isEqualTo(originCategory);
        assertThat(product.getAgeCategories()).isNotNull();
        assertThat(product.getAgeCategories().size()).isEqualTo(1);
        assertThat(product.getAgeCategories().contains(ageCategory)).isTrue();
    }

    @Test
    public void updateProduct_NonExistentProductId_ThrowProductNotFoundException() {
        when(productRepository.findById(eq(PRODUCT_ID))).thenReturn(Optional.empty());

        assertException(ProductNotFoundException.class, PRODUCT_ID_NOT_FOUND_EXCEPTION_MSG,
                () -> productService.updateProduct(PRODUCT_ID, addUpdateProductDto, null, null));

        verify(productRepository, never()).save(any());
    }

    @Test
    public void updateProduct_DuplicateProductName_ThrowDuplicateProductNameException() {
        product.setId(2);

        when(productRepository.findById(eq(PRODUCT_ID))).thenReturn(Optional.of(product));
        when(productRepository.findByName(eq(PRODUCT_NAME))).thenReturn(Optional.of(product));

        assertException(DuplicateProductNameException.class, DUPLICATE_PRODUCT_NAME_EXCEPTION_MSG,
                () -> productService.updateProduct(PRODUCT_ID, addUpdateProductDto, null, null));

        verify(productRepository, never()).save(any());
    }

    @Test
    public void createProduct_NonExistentProductName() throws Exception {
        MultipartFile image = mock(MultipartFile.class);
        List<MultipartFile> images = List.of(image);

        when(productRepository.findByName(eq(PRODUCT_NAME))).thenReturn(Optional.empty());
        when(productMapper.toProduct(eq(addUpdateProductDto))).thenReturn(product);
        when(categoryService.findCategoryByIdAndType(eq(categoryIdDto.id()), eq(BrandCategory.class)))
                .thenReturn(brandCategory);
        when(categoryService.findCategoryByIdAndType(eq(categoryIdDto.id()), eq(OriginCategory.class)))
                .thenReturn(originCategory);
        when(categoryService.findCategoryByIdAndType(eq(categoryIdDto.id()), eq(AgeCategory.class)))
                .thenReturn(ageCategory);

        productService.createProduct(addUpdateProductDto, image, images);

        verify(productRepository).save(eq(product));

        assertThat(product.getBrandCategory()).isEqualTo(brandCategory);
        assertThat(product.getOriginCategory()).isEqualTo(originCategory);
        assertThat(product.getAgeCategories()).isNotNull();
        assertThat(product.getAgeCategories().contains(ageCategory)).isTrue();
    }

    @Test
    public void createProduct_DuplicateProductName_ThrowDuplicateProductNameException() {
        when(productRepository.findByName(eq(PRODUCT_NAME))).thenReturn(Optional.of(product));

        assertException(DuplicateProductNameException.class, DUPLICATE_PRODUCT_NAME_EXCEPTION_MSG,
                () -> productService.createProduct(addUpdateProductDto, null, null));

        verify(productRepository, never()).save(any());
    }

    private void assertException(Class<? extends Exception> expectedExceptionType,
                                 String expectedMessage, Executable executable) {
        Exception exception = assertThrows(expectedExceptionType, executable);
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }
}