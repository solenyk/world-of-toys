package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.mapper.ProductMapper;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.repository.product.ProductRepository;
import com.kopchak.worldoftoys.repository.product.specifications.impl.ProductSpecificationsImpl;
import com.kopchak.worldoftoys.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductSpecificationsImpl productSpecifications;
    private final ProductMapper productMapper;

    @Override
    public FilteredProductsPageDto getAllProducts(int page, int size, String productName, BigDecimal minPrice, BigDecimal maxPrice,
                                                  List<String> originCategories, List<String> brandCategories,
                                                  List<String> ageCategories, String priceSortOrder) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Product> spec = Specification
                .where(productSpecifications.hasProductName(productName))
                .and(productSpecifications.hasPriceGreaterThanOrEqualTo(minPrice))
                .and(productSpecifications.hasPriceLessThanOrEqualTo(maxPrice))
                .and(productSpecifications.hasProductInOriginCategory(originCategories))
                .and(productSpecifications.hasProductInBrandCategory(brandCategories))
                .and(productSpecifications.hasProductInAgeCategory(ageCategories))
                .and(productSpecifications.sortByPrice(priceSortOrder));
        Page<Product> productPage = productRepository.findAll(spec, pageable);
        return productMapper.toFilteredProductsPageDto(productPage);
    }
}
