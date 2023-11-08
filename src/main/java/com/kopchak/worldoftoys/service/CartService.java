package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.cart.RequestCartItemDto;
import com.kopchak.worldoftoys.dto.cart.UserCartDetailsDto;

public interface CartService {
    void addProductToCart(RequestCartItemDto requestCartItemDto, String userEmail);
    UserCartDetailsDto getUserCartDetails(String userEmail);
    void updateUserCartItem(RequestCartItemDto requestCartItemDto, String userEmail);
    void deleteUserCartItem(RequestCartItemDto requestCartItemDto, String userEmail);
}
