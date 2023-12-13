package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.admin.product.AddUpdateProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminFilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.dto.product.category.FilteringProductCategoriesDto;
import com.kopchak.worldoftoys.exception.exception.CategoryNotFoundException;
import com.kopchak.worldoftoys.exception.exception.ImageException;
import com.kopchak.worldoftoys.mapper.product.ProductMapper;
import com.kopchak.worldoftoys.model.image.Image;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.repository.product.ProductCategoryRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductSpecificationsImpl productSpecifications;
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
    public Optional<ProductDto> getProductDtoBySlug(String productSlug) {
        Optional<Product> product = productRepository.findBySlug(productSlug);
        log.info("Fetched product by slug: '{}'", productSlug);
        return product.map(productMapper::toProductDto);
    }

    @Override
    public FilteringProductCategoriesDto getFilteringProductCategories(String productName, BigDecimal minPrice,
                                                                       BigDecimal maxPrice,
                                                                       List<String> originCategories,
                                                                       List<String> brandCategories,
                                                                       List<String> ageCategories) {
        Specification<Product> spec = productSpecifications.filterByProductNamePriceAndCategories(productName, minPrice,
                maxPrice, originCategories, brandCategories, ageCategories);
        var filteringProductCategoriesDto = productCategoryRepository.findUniqueFilteringProductCategories(spec);
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
    public Optional<AdminProductDto> getAdminProductDtoById(Integer productId) {
        Optional<Product> product = productRepository.findById(productId);
        log.info("Fetched product by id: '{}'", productId);
        return product.map(productMapper::toAdminProductDto);
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

    public void updateProduct(Integer productId, AddUpdateProductDto addUpdateProductDto, MultipartFile mainImageFile,
                              List<MultipartFile> imageFilesList) throws CategoryNotFoundException, ImageException {
        Product product = productMapper.toProduct(addUpdateProductDto, productId, productCategoryRepository,
                mainImageFile, imageFilesList, imageService);
        productRepository.save(product);
        log.info("The product with id: {} was successfully saved", productId);
        Set<Image> productImagesSet = product.getImages();
        productImagesSet.add(product.getMainImage());
        Set<String> imageNames = productImagesSet.stream().map(Image::getName).collect(Collectors.toSet());
        imageRepository.deleteImagesByProductIdNotInNames(productId, imageNames);
        log.info("Product photos that were missing during the update have been removed in " +
                "the updated version of the product");
    }
}
