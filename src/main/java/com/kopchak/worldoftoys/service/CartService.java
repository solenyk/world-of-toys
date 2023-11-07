package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.cart.RequestCartItemDto;
import com.kopchak.worldoftoys.dto.cart.UserCartDetailsDto;

public interface CartService {
    void addProductToCart(RequestCartItemDto requestCartItemDto, String email);
    UserCartDetailsDto getUserCartDetails(String email);
    void updateUserCartItem(RequestCartItemDto requestCartItemDto, String email);
    void deleteUserCartItem(RequestCartItemDto requestCartItemDto, String email);
}
