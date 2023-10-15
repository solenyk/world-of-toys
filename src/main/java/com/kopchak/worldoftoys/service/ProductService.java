package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.product.ProductDto;

import java.util.List;

public interface ProductService {
    List<ProductDto> getAllProducts(int page, int size);
}
