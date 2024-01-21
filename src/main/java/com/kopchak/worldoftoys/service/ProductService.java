package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.admin.product.AddUpdateProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminFilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductDto;
import com.kopchak.worldoftoys.dto.admin.product.category.AdminCategoryDto;
import com.kopchak.worldoftoys.dto.admin.product.category.CategoryNameDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.dto.product.category.FilteringCategoriesDto;
import com.kopchak.worldoftoys.exception.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface ProductService {
    FilteredProductsPageDto getFilteredProducts(int page, int size, String productName, BigDecimal minPrice, BigDecimal maxPrice,
                                                List<String> originCategories, List<String> brandCategories,
                                                List<String> ageCategories, String priceSortOrder);

    ProductDto getProductDtoBySlug(String productSlug) throws ProductNotFoundException, ImageDecompressionException;

    FilteringCategoriesDto getFilteringCategories(String productName, BigDecimal minPrice, BigDecimal maxPrice,
                                                  List<String> originCategories, List<String> brandCategories,
                                                  List<String> ageCategories);

    AdminFilteredProductsPageDto getAdminFilteredProducts(int page, int size, String productName, BigDecimal minPrice,
                                                          BigDecimal maxPrice, List<String> originCategories,
                                                          List<String> brandCategories, List<String> ageCategories,
                                                          String priceSortOrder, String availability);

    AdminProductDto getAdminProductDtoById(Integer productId) throws ProductNotFoundException, ImageDecompressionException;

    void updateProduct(Integer productId, AddUpdateProductDto addUpdateProductDto, MultipartFile mainImageFile,
                       List<MultipartFile> imageFileList)
            throws CategoryNotFoundException, ProductNotFoundException, ImageCompressionException, ImageExceedsMaxSizeException, InvalidImageFileFormatException;

    void createProduct(AddUpdateProductDto addUpdateProductDto, MultipartFile mainImageFile,
                       List<MultipartFile> imageFileList)
            throws CategoryNotFoundException, ProductNotFoundException, ImageCompressionException, ImageExceedsMaxSizeException, InvalidImageFileFormatException;

    Set<AdminCategoryDto> getAdminCategories(String categoryType) throws CategoryNotFoundException, InvalidCategoryTypeException;

    void deleteCategory(String category, Integer categoryId) throws CategoryContainsProductsException, InvalidCategoryTypeException;

    void updateCategory(String categoryType, Integer categoryId, CategoryNameDto categoryNameDto)
            throws CategoryNotFoundException, CategoryAlreadyExistsException, InvalidCategoryTypeException;

    void createCategory(String categoryType, CategoryNameDto categoryNameDto)
            throws CategoryAlreadyExistsException, InvalidCategoryTypeException, CategoryCreationException;
}
