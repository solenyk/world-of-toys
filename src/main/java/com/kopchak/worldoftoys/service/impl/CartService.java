package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.domain.cart.CartItem;
import com.kopchak.worldoftoys.domain.cart.CartItemId;
import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.domain.user.AppUser;
import com.kopchak.worldoftoys.dto.cart.CartItemDto;
import com.kopchak.worldoftoys.dto.cart.RequestCartItemDto;
import com.kopchak.worldoftoys.dto.cart.UserCartDetailsDto;
import com.kopchak.worldoftoys.exception.exception.cart.CartValidationException;
import com.kopchak.worldoftoys.exception.exception.product.ProductNotFoundException;
import com.kopchak.worldoftoys.repository.cart.CartItemRepository;
import com.kopchak.worldoftoys.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public void addProductToCart(RequestCartItemDto requestCartItemDto, AppUser user) {
        BigInteger cartItemQuantity = requestCartItemDto.quantity() == null ? BigInteger.ONE : requestCartItemDto.quantity();
        String productSlug = requestCartItemDto.slug();
        Product product = getProductBySlug(productSlug);
        CartItemId cartItemId = new CartItemId(user, product);
        Optional<CartItem> optionalCartItem = cartItemRepository.findById(cartItemId);
        CartItem cartItem;
        if (optionalCartItem.isPresent()) {
            cartItem = optionalCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity().add(cartItemQuantity));
        } else {
            cartItem = new CartItem(cartItemId, cartItemQuantity);
        }
        cartItemRepository.save(cartItem);
        log.info("The product with the slug: {} has been added to the cart of a user with username: {} in quantity: {}.",
                productSlug, user.getUsername(), cartItemQuantity);
    }

    public void verifyCartBeforeOrderCreation(AppUser user) {
        int deletedRowsAmount = cartItemRepository.deleteUnavailableItems(user);
        int updatedRowsAmount = cartItemRepository.updateCartItems(user);
        if (deletedRowsAmount > 0 || updatedRowsAmount > 0) {
            throw new CartValidationException("Some products in the cart are not available in the selected quantity " +
                    "because one or more products are out of stock");
        }
    }

    public UserCartDetailsDto getUserCartDetails(AppUser user) {
        Set<CartItemDto> content = cartItemRepository.findAllUserCartItems(user);
        BigDecimal totalPrice = cartItemRepository.calculateUserCartTotalPrice(user);
        return new UserCartDetailsDto(content, totalPrice);
    }

    public void updateUserCartItem(RequestCartItemDto requestCartItemDto, AppUser user) {
        BigInteger cartItemQuantity = requestCartItemDto.quantity() == null ? BigInteger.ONE : requestCartItemDto.quantity();
        String productSlug = requestCartItemDto.slug();
        Product product = getProductBySlug(requestCartItemDto.slug());
        CartItemId cartItemId = new CartItemId(user, product);
        CartItem cartItem = new CartItem(cartItemId, cartItemQuantity);
        cartItemRepository.save(cartItem);
        log.info("The product with slug: {} has been updated in the cart of a user with username: {} in quantity: {}.",
                productSlug, user.getUsername(), cartItemQuantity);
    }

    public void deleteUserCartItem(RequestCartItemDto requestCartItemDto, AppUser user) {
        String productSlug = requestCartItemDto.slug();
        Product product = getProductBySlug(productSlug);
        CartItemId cartItemId = new CartItemId(user, product);
        Optional<CartItem> optionalCartItem = cartItemRepository.findById(cartItemId);
        optionalCartItem.ifPresent(cartItemRepository::delete);
        log.info("The product with slug: {} has been deleted from the cart of a user with username: {}.",
                productSlug, user.getUsername());
    }

    private Product getProductBySlug(String slug) {
        return productRepository.findBySlug(slug).orElseThrow(() -> {
            String errMsg = String.format("Product with slug: %s doesn't exist.", slug);
            log.error(errMsg);
            return new ProductNotFoundException(errMsg);
        });
    }
}
