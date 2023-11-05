package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.cart.AddCartItemDto;
import com.kopchak.worldoftoys.dto.cart.UserCartDetailsDto;
import com.kopchak.worldoftoys.dto.cart.CartItemDto;
import com.kopchak.worldoftoys.exception.ProductNotFoundException;
import com.kopchak.worldoftoys.exception.UserNotFoundException;
import com.kopchak.worldoftoys.model.cart.CartItem;
import com.kopchak.worldoftoys.model.cart.CartItemId;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.repository.cart.CartItemRepository;
import com.kopchak.worldoftoys.repository.product.ProductRepository;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import com.kopchak.worldoftoys.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    public void addProductToCart(AddCartItemDto addCartItemDto, String email) {
        AppUser user = userRepository.findByEmail(email).orElseThrow(() ->
                new UserNotFoundException(HttpStatus.NOT_FOUND, "User with this username does not exist!"));
        Product product = productRepository.findBySlug(addCartItemDto.slug()).orElseThrow(() ->
                new ProductNotFoundException(HttpStatus.NOT_FOUND, "Product doesn't exist"));
        int cartItemQuantity = addCartItemDto.quantity() == null ? 1 : addCartItemDto.quantity();
        CartItem cartItem = new CartItem(new CartItemId(user, product), cartItemQuantity);
        cartItemRepository.save(cartItem);
    }

    @Override
    public UserCartDetailsDto getUserCartDetails(String email) {
        Set<CartItemDto> content = cartItemRepository.findAllCartItemDtosByUserEmail(email);
        BigDecimal totalPrice = cartItemRepository.calculateUserCartTotalByEmail(email);
        return new UserCartDetailsDto(content, totalPrice);
    }
}
