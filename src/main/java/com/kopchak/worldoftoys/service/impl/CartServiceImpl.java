package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.cart.CartItemDto;
import com.kopchak.worldoftoys.dto.cart.RequestCartItemDto;
import com.kopchak.worldoftoys.dto.cart.UserCartDetailsDto;
import com.kopchak.worldoftoys.exception.ProductNotFoundException;
import com.kopchak.worldoftoys.model.cart.CartItem;
import com.kopchak.worldoftoys.model.cart.CartItemId;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.repository.cart.CartItemRepository;
import com.kopchak.worldoftoys.repository.product.ProductRepository;
import com.kopchak.worldoftoys.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Override
    public void addProductToCart(RequestCartItemDto requestCartItemDto, AppUser user) throws ProductNotFoundException {
        int cartItemQuantity = requestCartItemDto.quantity() == null ? 1 : requestCartItemDto.quantity();
        String productSlug = requestCartItemDto.slug();
        Product product = getProductBySlug(productSlug);
        CartItemId cartItemId = new CartItemId(user, product);
        Optional<CartItem> optionalCartItem = cartItemRepository.findById(cartItemId);
        CartItem cartItem;
        if (optionalCartItem.isPresent()) {
            cartItem = optionalCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + cartItemQuantity);
        } else {
            cartItem = new CartItem(cartItemId, cartItemQuantity);
        }
        cartItemRepository.save(cartItem);
        log.info("The product with the slug: {} has been added to the cart of a user with username: {} in quantity: {}.",
                productSlug, user.getUsername(), cartItemQuantity);
    }

    @Override
    public UserCartDetailsDto getUserCartDetails(AppUser user) {
        Set<CartItemDto> content = cartItemRepository.findAllUserCartItems(user);
        BigDecimal totalPrice = cartItemRepository.calculateUserCartTotalPrice(user);
        return new UserCartDetailsDto(content, totalPrice);
    }

    @Override
    public void updateUserCartItem(RequestCartItemDto requestCartItemDto, AppUser user) throws ProductNotFoundException {
        int cartItemQuantity = requestCartItemDto.quantity() == null ? 1 : requestCartItemDto.quantity();
        String productSlug = requestCartItemDto.slug();
        Product product = getProductBySlug(requestCartItemDto.slug());
        CartItemId cartItemId = new CartItemId(user, product);
        CartItem cartItem = new CartItem(cartItemId, cartItemQuantity);
        cartItemRepository.save(cartItem);
        log.info("The product with slug: {} has been updated in the cart of a user with username: {} in quantity: {}.",
                productSlug, user.getUsername(), cartItemQuantity);
    }

    @Override
    public void deleteUserCartItem(RequestCartItemDto requestCartItemDto, AppUser user) throws ProductNotFoundException {
        String productSlug = requestCartItemDto.slug();
        Product product = getProductBySlug(productSlug);
        CartItemId cartItemId = new CartItemId(user, product);
        Optional<CartItem> optionalCartItem = cartItemRepository.findById(cartItemId);
        optionalCartItem.ifPresent(cartItemRepository::delete);
        log.info("The product with slug: {} has been deleted from the cart of a user with username: {}.",
                productSlug, user.getUsername());
    }

    private Product getProductBySlug(String slug) throws ProductNotFoundException {
        return productRepository.findBySlug(slug).orElseThrow(() -> {
            String errMsg = String.format("Product with slug: %s doesn't exist.", slug);
            log.error(errMsg);
            return new ProductNotFoundException(errMsg);
        });
    }
}
