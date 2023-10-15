package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.mapper.ProductMapper;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.repository.product.ProductRepository;
import com.kopchak.worldoftoys.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    public List<ProductDto> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Product> spec = Specification.where(null);
        ProductMapper productMapper = new ProductMapper();
        return productRepository.findAll(spec, pageable)
                .getContent()
                .stream().map(productMapper::toProductDto)
                .collect(Collectors.toList());
    }
}
