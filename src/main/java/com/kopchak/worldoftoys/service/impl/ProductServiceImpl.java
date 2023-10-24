package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.dto.product.category.FilteringProductCategoriesDto;
import com.kopchak.worldoftoys.mapper.ProductMapper;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.repository.product.ProductCategoryRepository;
import com.kopchak.worldoftoys.repository.product.ProductRepository;
import com.kopchak.worldoftoys.repository.specifications.impl.ProductSpecificationsImpl;
import com.kopchak.worldoftoys.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductSpecificationsImpl productSpecifications;
    private final ProductMapper productMapper;

    @Override
    public FilteredProductsPageDto getFilteredProducts(int page, int size, String productName, BigDecimal minPrice,
                                                       BigDecimal maxPrice, List<String> originCategories,
                                                       List<String> brandCategories, List<String> ageCategories,
                                                       String priceSortOrder) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Product> spec = productSpecifications.filterByAllCriteria(productName, minPrice,
                maxPrice, originCategories, brandCategories, ageCategories, priceSortOrder);
        Page<Product> productPage = productRepository.findAll(spec, pageable);
        return productMapper.toFilteredProductsPageDto(productPage);
    }

    @Override
    public Optional<ProductDto> getProductDtoBySlug(String slug) {
        Optional<Product> product = productRepository.findBySlug(slug);
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
        return productCategoryRepository.findUniqueFilteringProductCategories(spec);
    }
}
