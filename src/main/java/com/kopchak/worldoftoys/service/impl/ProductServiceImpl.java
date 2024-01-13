package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.admin.product.AddUpdateProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminFilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductDto;
import com.kopchak.worldoftoys.dto.admin.product.category.AdminCategoryDto;
import com.kopchak.worldoftoys.dto.admin.product.category.CategoryIdDto;
import com.kopchak.worldoftoys.dto.admin.product.category.CategoryNameDto;
import com.kopchak.worldoftoys.dto.product.image.ImageDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.dto.product.category.FilteringCategoriesDto;
import com.kopchak.worldoftoys.exception.*;
import com.kopchak.worldoftoys.mapper.product.CategoryMapper;
import com.kopchak.worldoftoys.mapper.product.ProductMapper;
import com.kopchak.worldoftoys.domain.image.Image;
import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.domain.product.category.AgeCategory;
import com.kopchak.worldoftoys.domain.product.category.BrandCategory;
import com.kopchak.worldoftoys.domain.product.category.OriginCategory;
import com.kopchak.worldoftoys.domain.product.category.ProductCategory;
import com.kopchak.worldoftoys.domain.product.category.type.CategoryType;
import com.kopchak.worldoftoys.repository.product.CategoryRepository;
import com.kopchak.worldoftoys.repository.product.ProductRepository;
import com.kopchak.worldoftoys.repository.product.image.ImageRepository;
import com.kopchak.worldoftoys.repository.specifications.impl.ProductSpecificationsImpl;
import com.kopchak.worldoftoys.service.ImageService;
import com.kopchak.worldoftoys.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductSpecificationsImpl productSpecifications;
    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final ImageRepository imageRepository;
    private final ImageService imageService;

    @Override
    public FilteredProductsPageDto getFilteredProducts(int page, int size, String productName, BigDecimal minPrice,
                                                       BigDecimal maxPrice, List<String> originCategories,
                                                       List<String> brandCategories, List<String> ageCategories,
                                                       String priceSortOrder) {
        Page<Product> productPage = getFilteredProductPage(page, size, productName, minPrice, maxPrice,
                originCategories, brandCategories, ageCategories, priceSortOrder);
        return productMapper.toFilteredProductsPageDto(productPage);
    }

    @Override
    public ProductDto getProductDtoBySlug(String productSlug) throws ProductNotFoundException, ImageDecompressionException {
        Optional<Product> productOptional = productRepository.findBySlug(productSlug);
        if (productOptional.isEmpty()) {
            String errMsg = String.format("The product with slug: %s is not found.", productSlug);
            log.error(errMsg);
            throw new ProductNotFoundException(errMsg);
        }
        Product product = productOptional.get();
        Image mainImage = product.getMainImage();
        ImageDto mainImageDto = imageService.generateDecompressedImageDto(mainImage);
        List<ImageDto> imageDtoList = getDecompressedProductImageDtoList(product.getImages(), mainImage);
        log.info("Fetched product by slug: '{}'", productSlug);
        return productMapper.toProductDto(product, mainImageDto, imageDtoList);
    }

    @Override
    public FilteringCategoriesDto getFilteringCategories(String productName, BigDecimal minPrice,
                                                         BigDecimal maxPrice,
                                                         List<String> originCategories,
                                                         List<String> brandCategories,
                                                         List<String> ageCategories) {
        Specification<Product> spec = productSpecifications.filterByProductNamePriceAndCategories(productName, minPrice,
                maxPrice, originCategories, brandCategories, ageCategories);
        var filteringProductCategoriesDto = categoryRepository.findUniqueFilteringProductCategories(spec);
        log.info("Fetched filtering product categories - Product Name: '{}', Min Price: {}, Max Price: {}, " +
                        "Origin Categories: {}, Brand Categories: {}, Age Categories: {}",
                productName, minPrice, maxPrice, originCategories, brandCategories, ageCategories);
        return filteringProductCategoriesDto;
    }

    @Override
    public AdminFilteredProductsPageDto getAdminFilteredProducts(int page, int size, String productName,
                                                                 BigDecimal minPrice, BigDecimal maxPrice,
                                                                 List<String> originCategories,
                                                                 List<String> brandCategories,
                                                                 List<String> ageCategories, String priceSortOrder) {
        Page<Product> productPage = getFilteredProductPage(page, size, productName, minPrice, maxPrice,
                originCategories, brandCategories, ageCategories, priceSortOrder);
        return productMapper.toAdminFilteredProductsPageDto(productPage);
    }

    @Override
    public AdminProductDto getAdminProductDtoById(Integer productId) throws ProductNotFoundException, ImageDecompressionException {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            String errMsg = String.format("The product with id: %d is not found.", productId);
            log.error(errMsg);
            throw new ProductNotFoundException(errMsg);
        }
        Product product = productOptional.get();
        Image mainImage = product.getMainImage();
        ImageDto mainImageDto = imageService.generateDecompressedImageDto(mainImage);
        List<ImageDto> imageDtoList = getDecompressedProductImageDtoList(product.getImages(), mainImage);
        log.info("Fetched product by id: '{}'", productId);
        return productMapper.toAdminProductDto(product, mainImageDto, imageDtoList);
    }

    private Page<Product> getFilteredProductPage(int page, int size, String productName, BigDecimal minPrice,
                                                 BigDecimal maxPrice, List<String> originCategories,
                                                 List<String> brandCategories, List<String> ageCategories,
                                                 String priceSortOrder) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Product> spec = productSpecifications.filterByAllCriteria(productName, minPrice,
                maxPrice, originCategories, brandCategories, ageCategories, priceSortOrder);
        Page<Product> productPage = productRepository.findAll(spec, pageable);
        log.info("Fetched filtered products - Page: {}, Size: {}, Product Name: '{}', Min Price: {}, Max Price: {}, " +
                        "Origin Categories: {}, Brand Categories: {}, Age Categories: {}, Price Sort Order: '{}'",
                page, size, productName, minPrice, maxPrice, originCategories, brandCategories, ageCategories,
                priceSortOrder);
        return productPage;
    }

    @Override
    public void updateProduct(Integer productId, AddUpdateProductDto addUpdateProductDto, MultipartFile mainImageFile,
                              List<MultipartFile> imageFilesList)
            throws InvalidCategoryTypeException, ProductNotFoundException, ImageCompressionException, ImageExceedsMaxSizeException, InvalidImageFileFormatException {
        String productName = addUpdateProductDto.name();
        Optional<Product> productOptional = productRepository.findByName(productName);
        if (productOptional.isPresent() && !productOptional.get().getId().equals(productId)) {
            throw new ProductNotFoundException(String.format("The product with name: %s is already exist", productName));
        }
        Product product = buildProductFromDtoAndImages(addUpdateProductDto, mainImageFile, imageFilesList);
        product.setId(productId);
        productRepository.save(product);
        updateProductImages(product);
        log.info("The product with id: {} was successfully updated", productId);
    }

    @Override
    public void addProduct(AddUpdateProductDto addUpdateProductDto, MultipartFile mainImageFile,
                           List<MultipartFile> imageFileList) throws InvalidCategoryTypeException, ProductNotFoundException, ImageCompressionException, ImageExceedsMaxSizeException, InvalidImageFileFormatException {
        String productName = addUpdateProductDto.name();
        if (productRepository.findByName(productName).isPresent()) {
            throw new ProductNotFoundException(String.format("The product with name: %s is already exist", productName));
        }
        Product product = buildProductFromDtoAndImages(addUpdateProductDto, mainImageFile, imageFileList);
        productRepository.save(product);
        log.info("The product with name: {} was successfully saved", product.getName());
    }

    @Override
    public void deleteProduct(Integer productId) {
        productRepository.deleteById(productId);
    }

    @Override
    public Set<AdminCategoryDto> getAdminCategories(String categoryType) throws InvalidCategoryTypeException {
        Class<? extends ProductCategory> categoryClass = getCategoryByCategoryType(categoryType);
        var categories = categoryRepository.findAllCategories(categoryClass);
        return categoryMapper.toAdminCategoryDtoSet(categories);
    }

    @Override
    public void deleteCategory(String categoryType, Integer categoryId) throws InvalidCategoryTypeException {
        Class<? extends ProductCategory> categoryClass = getCategoryByCategoryType(categoryType);
        categoryRepository.deleteCategory(categoryClass, categoryId);
    }

    @Override
    public void updateCategory(String categoryType, Integer categoryId, CategoryNameDto categoryNameDto)
            throws InvalidCategoryTypeException {
        Class<? extends ProductCategory> categoryClass = getCategoryByCategoryType(categoryType);
        categoryRepository.updateCategory(categoryClass, categoryId, categoryNameDto.name());
    }

    @Override
    public void addCategory(String categoryType, CategoryNameDto categoryNameDto) throws InvalidCategoryTypeException {
        Class<? extends ProductCategory> categoryClass = getCategoryByCategoryType(categoryType);
        categoryRepository.addCategory(categoryClass, categoryNameDto.name());
    }

    private Class<? extends ProductCategory> getCategoryByCategoryType(String categoryType)
            throws InvalidCategoryTypeException {
        return switch (CategoryType.findByValue(categoryType)) {
            case BRANDS -> BrandCategory.class;
            case ORIGINS -> OriginCategory.class;
            case AGES -> AgeCategory.class;
        };
    }

    private Product buildProductFromDtoAndImages(AddUpdateProductDto addUpdateProductDto, MultipartFile mainImageFile,
                                                 List<MultipartFile> imageFilesList) throws InvalidCategoryTypeException,
            ImageCompressionException, ImageExceedsMaxSizeException, InvalidImageFileFormatException {
        Product product = productMapper.toProduct(addUpdateProductDto);
        setProductCategories(product, addUpdateProductDto);
        setProductImages(product, mainImageFile, imageFilesList);
        return product;
    }

    private void setProductCategories(Product product, AddUpdateProductDto productDto)
            throws InvalidCategoryTypeException {
        product.setBrandCategory(categoryRepository.findById(productDto.brandCategory().id(), BrandCategory.class));
        product.setOriginCategory(categoryRepository.findById(productDto.originCategory().id(), OriginCategory.class));
        Set<AgeCategory> ageCategories = new LinkedHashSet<>();
        for (CategoryIdDto ageCategory : productDto.ageCategories()) {
            ageCategories.add(categoryRepository.findById(ageCategory.id(), AgeCategory.class));
        }
        product.setAgeCategories(ageCategories);
    }

    private void setProductImages(Product product, MultipartFile mainImageFile, List<MultipartFile> imageFilesList)
            throws ImageCompressionException, ImageExceedsMaxSizeException, InvalidImageFileFormatException {
        Image mainImage = imageService.convertMultipartFileToImage(mainImageFile, product);
        Set<Image> imagesSet = new LinkedHashSet<>();
        for (MultipartFile image : imageFilesList) {
            imagesSet.add(imageService.convertMultipartFileToImage(image, product));
        }
        product.setMainImage(mainImage);
        product.setImages(imagesSet);
    }

    private List<ImageDto> getDecompressedProductImageDtoList(Set<Image> images, Image mainImage)
            throws ImageDecompressionException {
        List<ImageDto> imageDtoList = new ArrayList<>();
        for (Image image : images) {
            if (!image.equals(mainImage)) {
                imageDtoList.add(imageService.generateDecompressedImageDto(image));
            }
        }
        return imageDtoList;
    }

    private void updateProductImages(Product product) {
        Set<Image> productImagesSet = product.getImages();
        productImagesSet.add(product.getMainImage());
        Set<String> imageNames = productImagesSet.stream().map(Image::getName).collect(Collectors.toSet());
        imageRepository.deleteImagesByProductIdNotInNames(product.getId(), imageNames);
        log.info("Product photos that were missing during the update have been removed in " +
                "the updated version of the product");
    }
}
