package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.domain.user.AppUser;
import com.kopchak.worldoftoys.dto.cart.RequestCartItemDto;
import com.kopchak.worldoftoys.dto.cart.UserCartDetailsDto;
import com.kopchak.worldoftoys.exception.exception.cart.CartValidationException;
import com.kopchak.worldoftoys.exception.exception.product.ProductNotFoundException;

public interface CartService {
    void addProductToCart(RequestCartItemDto requestCartItemDto, AppUser user) throws ProductNotFoundException;

    UserCartDetailsDto getUserCartDetails(AppUser user);

    void updateUserCartItem(RequestCartItemDto requestCartItemDto, AppUser user) throws ProductNotFoundException;

    void deleteUserCartItem(RequestCartItemDto requestCartItemDto, AppUser user) throws ProductNotFoundException;

    void verifyCartBeforeOrderCreation(AppUser user) throws CartValidationException;
}
