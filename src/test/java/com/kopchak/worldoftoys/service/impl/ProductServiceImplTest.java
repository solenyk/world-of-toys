package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.domain.image.Image;
import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.domain.product.category.AgeCategory;
import com.kopchak.worldoftoys.domain.product.category.BrandCategory;
import com.kopchak.worldoftoys.domain.product.category.OriginCategory;
import com.kopchak.worldoftoys.domain.product.category.ProductCategory;
import com.kopchak.worldoftoys.dto.admin.product.AddUpdateProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductsPageDto;
import com.kopchak.worldoftoys.dto.admin.category.CategoryIdDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.dto.product.category.FilteringCategoriesDto;
import com.kopchak.worldoftoys.dto.product.image.ImageDto;
import com.kopchak.worldoftoys.exception.exception.image.ext.ImageDecompressionException;
import com.kopchak.worldoftoys.exception.exception.product.ProductNotFoundException;
import com.kopchak.worldoftoys.mapper.product.CategoryMapper;
import com.kopchak.worldoftoys.mapper.product.ProductMapper;
import com.kopchak.worldoftoys.repository.product.CategoryRepository;
import com.kopchak.worldoftoys.repository.product.ProductRepository;
import com.kopchak.worldoftoys.repository.specifications.impl.ProductSpecificationsImpl;
import com.kopchak.worldoftoys.service.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ImageService imageService;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductSpecificationsImpl productSpecifications;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private final static String PRODUCT_SLUG = "lyalka-klaymber";
    private final static String PRICE_SORT_ORDER = "asc";
    private final static Integer PRODUCT_ID = 1;
    private final static int PAGE = 0;
    private final static int SIZE = 3;
    private String productName;
    private BigDecimal minProductPrice;
    private BigDecimal maxProductPrice;
    private List<String> originCategories;
    private List<String> brandCategories;
    private List<String> ageCategories;
    private Product product;
    private ImageDto imageDto;
    private Specification<Product> spec;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        productName = "лялька";
        minProductPrice = BigDecimal.valueOf(350);
        maxProductPrice = BigDecimal.valueOf(1000);
        originCategories = List.of("china", "ukraine");
        brandCategories = List.of("сurlimals", "devilon");
        ageCategories = List.of("do-1-roku", "vid-1-do-3-rokiv");
        product = Product.builder().id(PRODUCT_ID).mainImage(new Image()).images(Set.of(new Image())).build();
        imageDto = ImageDto.builder().build();
        spec = Specification.where(null);
        pageable = PageRequest.of(PAGE, SIZE);
    }

    @Test
    public void getProductDtoBySlug_ExistentProduct_ReturnsProductDto() throws ImageDecompressionException, ProductNotFoundException {
        ProductDto expectedProductDto = ProductDto.builder().build();

        when(productRepository.findBySlug(eq(PRODUCT_SLUG))).thenReturn(Optional.of(product));
        when(imageService.generateDecompressedImageDto(any())).thenReturn(imageDto);
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
        var expectedFilteredProductsPageDto = new FilteredProductsPageDto(new ArrayList<>(), 3L, 2L);

        when(productSpecifications.filterByAllCriteria(eq(productName), eq(minProductPrice), eq(maxProductPrice),
                eq(originCategories), eq(brandCategories), eq(ageCategories), eq(PRICE_SORT_ORDER), any()))
                .thenReturn(spec);
        when(productRepository.findAll(eq(spec), eq(pageable))).thenReturn(Page.empty());
        when(productMapper.toFilteredProductsPageDto(any())).thenReturn(expectedFilteredProductsPageDto);

        var actualFilteredProductsPageDto = productService.getFilteredProductsPage(PAGE, SIZE, productName, minProductPrice,
                maxProductPrice, originCategories, brandCategories, ageCategories, PRICE_SORT_ORDER);

        assertThat(actualFilteredProductsPageDto).isNotNull();
        assertThat(actualFilteredProductsPageDto).isEqualTo(expectedFilteredProductsPageDto);
    }

    @Test
    public void getAdminFilteredProducts_ReturnsAdminProductsPageDto() {
        var expectedAdminProductsPageDto = AdminProductsPageDto.builder().build();

        when(productSpecifications.filterByAllCriteria(eq(productName), eq(minProductPrice), eq(maxProductPrice),
                eq(originCategories), eq(brandCategories), eq(ageCategories), eq(PRICE_SORT_ORDER), any()))
                .thenReturn(spec);
        when(productRepository.findAll(eq(spec), eq(pageable))).thenReturn(Page.empty());
        when(productMapper.toAdminFilteredProductsPageDto(any())).thenReturn(expectedAdminProductsPageDto);

        var actualAdminProductsPageDto = productService.getAdminProductsPage(PAGE, SIZE, productName,
                minProductPrice, maxProductPrice, originCategories, brandCategories, ageCategories,
                PRICE_SORT_ORDER, null);

        assertThat(actualAdminProductsPageDto).isNotNull();
        assertThat(actualAdminProductsPageDto).isEqualTo(expectedAdminProductsPageDto);
    }

    @Test
    public void getAdminProductDtoById_ExistentProduct_ReturnsAdminProductDto() throws ImageDecompressionException, ProductNotFoundException {
        var imageDtoList = Collections.singletonList(imageDto);
        var expectedAdminProductDto = AdminProductDto
                .builder()
                .id(PRODUCT_ID)
                .name(productName)
                .slug(PRODUCT_SLUG)
                .price(minProductPrice)
                .mainImage(imageDto)
                .build();

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(imageService.generateDecompressedImageDto(any())).thenReturn(imageDto);
        when(productMapper.toAdminProductDto(eq(product), eq(imageDto), eq(imageDtoList)))
                .thenReturn(expectedAdminProductDto);

        var actualAdminProductDto = productService.getProductById(PRODUCT_ID);

        assertThat(actualAdminProductDto).isNotNull();
        assertThat(actualAdminProductDto).isEqualTo(expectedAdminProductDto);
    }

    @Test
    public void getAdminProductDtoById_NonExistentProduct_ThrowProductNotFoundException() {
        String productNotFoundExceptionMsg = String.format("The product with id: %d is not found.", PRODUCT_ID);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertException(ProductNotFoundException.class, productNotFoundExceptionMsg,
                () -> productService.getProductById(PRODUCT_ID));
    }

    @Test
    public void updateProduct_() throws Exception {
        CategoryIdDto categoryIdDto = new CategoryIdDto(1);
        var addUpdateProductDto = AddUpdateProductDto
                .builder()
                .name(productName)
                .brandCategory(categoryIdDto)
                .originCategory(categoryIdDto)
                .ageCategories(List.of(categoryIdDto))
                .build();
        BrandCategory expectedBrandCategory = new BrandCategory();
        OriginCategory expectedOriginCategory = new OriginCategory();
        AgeCategory expectedAgeCategory = new AgeCategory();

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.findByName(productName)).thenReturn(Optional.empty());
        when(productMapper.toProduct(addUpdateProductDto)).thenReturn(product);
        when(categoryRepository.findById(eq(categoryIdDto.id()), eq(BrandCategory.class)))
                .thenReturn(expectedBrandCategory);
        when(categoryRepository.findById(eq(categoryIdDto.id()), eq(OriginCategory.class)))
                .thenReturn(expectedOriginCategory);
        when(categoryRepository.findById(eq(categoryIdDto.id()), eq(AgeCategory.class)))
                .thenReturn(expectedAgeCategory);

        productService.updateProduct(PRODUCT_ID, addUpdateProductDto, null, null);

        verify(productRepository).save(product);

        assertThat(product.getBrandCategory()).isEqualTo(expectedBrandCategory);
        assertThat(product.getOriginCategory()).isEqualTo(expectedOriginCategory);
        assertThat(product.getAgeCategories()).isNotNull();
        assertThat(product.getAgeCategories().size()).isEqualTo(1);
        assertThat(product.getAgeCategories().contains(expectedAgeCategory)).isTrue();
    }

    private void assertException(Class<? extends Exception> expectedExceptionType,
                                 String expectedMessage, Executable executable) {
        Exception exception = assertThrows(expectedExceptionType, executable);
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }
}