package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.admin.product.AddUpdateProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductsPageDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.exception.exception.category.CategoryNotFoundException;
import com.kopchak.worldoftoys.exception.exception.image.ImageException;
import com.kopchak.worldoftoys.exception.exception.image.ext.ImageDecompressionException;
import com.kopchak.worldoftoys.exception.exception.product.DuplicateProductNameException;
import com.kopchak.worldoftoys.exception.exception.product.ProductNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    FilteredProductsPageDto getFilteredProductsPage(int page, int size, String productName, BigDecimal minPrice,
                                                    BigDecimal maxPrice, List<String> originCategories,
                                                    List<String> brandCategories, List<String> ageCategories,
                                                    String priceSortOrder) throws ImageDecompressionException;

    ProductDto getProductBySlug(String productSlug) throws ProductNotFoundException, ImageDecompressionException;

    AdminProductsPageDto getAdminProductsPage(int page, int size, String productName, BigDecimal minPrice,
                                              BigDecimal maxPrice, List<String> originCategories,
                                              List<String> brandCategories, List<String> ageCategories,
                                              String priceSortOrder, String availability) throws ImageDecompressionException;

    AdminProductDto getProductById(Integer productId) throws ProductNotFoundException, ImageDecompressionException;

    void updateProduct(Integer productId, AddUpdateProductDto addUpdateProductDto, MultipartFile mainImageFile,
                       List<MultipartFile> imageFileList)
            throws CategoryNotFoundException, ProductNotFoundException, DuplicateProductNameException, ImageException;

    void createProduct(AddUpdateProductDto addUpdateProductDto, MultipartFile mainImageFile,
                       List<MultipartFile> imageFileList)
            throws DuplicateProductNameException, CategoryNotFoundException, ImageException;
}
