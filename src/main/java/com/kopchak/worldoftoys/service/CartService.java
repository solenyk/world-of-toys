package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.cart.AddCartItemDto;

public interface CartService {
    void addProductToCart(AddCartItemDto addCartItemDto, String email);
}
