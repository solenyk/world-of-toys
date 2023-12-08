package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.admin.product.AdminFilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductDto;
import com.kopchak.worldoftoys.dto.admin.product.UpdateProductDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.dto.product.category.FilteringProductCategoriesDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    FilteredProductsPageDto getFilteredProducts(int page, int size, String productName, BigDecimal minPrice, BigDecimal maxPrice,
                                                List<String> originCategories, List<String> brandCategories,
                                                List<String> ageCategories, String priceSortOrder);

    Optional<ProductDto> getProductDtoBySlug(String slug);

    FilteringProductCategoriesDto getFilteringProductCategories(String productName, BigDecimal minPrice, BigDecimal maxPrice,
                                                                List<String> originCategories, List<String> brandCategories,
                                                                List<String> ageCategories);

    AdminFilteredProductsPageDto getAdminFilteredProducts(int page, int size, String productName, BigDecimal minPrice, BigDecimal maxPrice,
                                                     List<String> originCategories, List<String> brandCategories,
                                                     List<String> ageCategories, String priceSortOrder);

    Optional<AdminProductDto> getAdminProductDtoById(Integer productId);

    void updateProduct(Integer productId, UpdateProductDto updateProductDto, MultipartFile mainImage,
                              List<MultipartFile> images) throws IOException;
}
