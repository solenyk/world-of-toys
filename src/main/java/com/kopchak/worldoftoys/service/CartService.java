package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.cart.RequestCartItemDto;
import com.kopchak.worldoftoys.dto.cart.UserCartDetailsDto;
import com.kopchak.worldoftoys.model.user.AppUser;

public interface CartService {
    void addProductToCart(RequestCartItemDto requestCartItemDto, AppUser user);
    UserCartDetailsDto getUserCartDetails(AppUser user);
    void updateUserCartItem(RequestCartItemDto requestCartItemDto, AppUser user);
    void deleteUserCartItem(RequestCartItemDto requestCartItemDto, AppUser user);
}
