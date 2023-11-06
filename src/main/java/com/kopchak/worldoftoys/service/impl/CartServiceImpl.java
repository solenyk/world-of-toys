package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.cart.AddCartItemDto;
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
    public void addProductToCart(AddCartItemDto addCartItemDto, String email) {
        int cartItemQuantity = addCartItemDto.quantity() == null ? 1 : addCartItemDto.quantity();
        cartItemRepository.insertUserCartItem(email, addCartItemDto.slug(), cartItemQuantity);
    }

    @Override
    public UserCartDetailsDto getUserCartDetails(String email) {
        Set<CartItemDto> content = cartItemRepository.findAllCartItemDtosByUserEmail(email);
        BigDecimal totalPrice = cartItemRepository.calculateUserCartTotalByEmail(email);
        return new UserCartDetailsDto(content, totalPrice);
    }

    @Override
    public void updateUserCartItem(AddCartItemDto addCartItemDto, String email) {
        int cartItemQuantity = addCartItemDto.quantity() == null ? 1 : addCartItemDto.quantity();
        cartItemRepository.updateUserCartItem(email, addCartItemDto.slug(), cartItemQuantity);
    }

    @Override
    public void deleteUserCartItem(AddCartItemDto addCartItemDto, String email) {
        cartItemRepository.deleteUserCartItem(email, addCartItemDto.slug());
    }
}
