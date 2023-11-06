package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.cart.AddCartItemDto;
import com.kopchak.worldoftoys.dto.cart.UserCartDetailsDto;

public interface CartService {
    void addProductToCart(AddCartItemDto addCartItemDto, String email);
    UserCartDetailsDto getUserCartDetails(String email);
    void updateUserCartItem(AddCartItemDto addCartItemDto, String email);
    void deleteUserCartItem(AddCartItemDto addCartItemDto, String email);
}
