package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.mapper.ProductMapper;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.repository.product.ProductRepository;
import com.kopchak.worldoftoys.repository.product.specifications.impl.ProductSpecificationsImpl;
import com.kopchak.worldoftoys.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductSpecificationsImpl productSpecifications;
    private final ProductMapper productMapper;

    @Override
    public List<ProductDto> getAllProducts(int page, int size, String productName, BigDecimal minPrice, BigDecimal maxPrice,
                                           List<String> originCategories, List<String> brandCategories,
                                           List<String> ageCategories) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Product> spec = Specification.where(null);
        if(productName != null){
            spec = spec.and(productSpecifications.hasProductName(productName));
        }
        if(minPrice != null){
            spec = spec.and(productSpecifications.hasPriceGreaterThanOrEqualTo(minPrice));
        }
        if(maxPrice != null){
            spec = spec.and(productSpecifications.hasPriceLessThanOrEqualTo(maxPrice));
        }
        if(originCategories != null && !originCategories.isEmpty()){
            spec = spec.and(productSpecifications.hasProductInOriginCategory(originCategories));
        }
        if(brandCategories != null && !brandCategories.isEmpty()){
            spec = spec.and(productSpecifications.hasProductInBrandCategory(brandCategories));
        }
        if(ageCategories != null && !ageCategories.isEmpty()){
            spec = spec.and(productSpecifications.hasProductInAgeCategory(ageCategories));
        }
        return productRepository.findAll(spec, pageable)
                .getContent()
                .stream().map(productMapper::toProductDto)
                .collect(Collectors.toList());
    }
}
