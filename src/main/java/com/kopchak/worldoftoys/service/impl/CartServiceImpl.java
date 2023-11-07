package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.cart.RequestCartItemDto;
import com.kopchak.worldoftoys.dto.cart.CartItemDto;
import com.kopchak.worldoftoys.dto.cart.UserCartDetailsDto;
import com.kopchak.worldoftoys.repository.cart.CartItemRepository;
import com.kopchak.worldoftoys.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartItemRepository cartItemRepository;

    @Override
    public void addProductToCart(RequestCartItemDto requestCartItemDto, String email) {
        int cartItemQuantity = requestCartItemDto.quantity() == null ? 1 : requestCartItemDto.quantity();
        cartItemRepository.insertUserCartItem(email, requestCartItemDto.slug(), cartItemQuantity);
    }

    @Override
    public UserCartDetailsDto getUserCartDetails(String email) {
        Set<CartItemDto> content = cartItemRepository.findAllCartItemDtosByUserEmail(email);
        BigDecimal totalPrice = cartItemRepository.calculateUserCartTotalByEmail(email);
        return new UserCartDetailsDto(content, totalPrice);
    }

    @Override
    public void updateUserCartItem(RequestCartItemDto requestCartItemDto, String email) {
        int cartItemQuantity = requestCartItemDto.quantity() == null ? 1 : requestCartItemDto.quantity();
        cartItemRepository.updateUserCartItem(email, requestCartItemDto.slug(), cartItemQuantity);
    }

    @Override
    public void deleteUserCartItem(RequestCartItemDto requestCartItemDto, String email) {
        cartItemRepository.deleteUserCartItem(email, requestCartItemDto.slug());
    }
}
