package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.cart.CartItemDto;
import com.kopchak.worldoftoys.dto.cart.RequestCartItemDto;
import com.kopchak.worldoftoys.dto.cart.UserCartDetailsDto;
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
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public void addProductToCart(RequestCartItemDto requestCartItemDto, String email) {
        int cartItemQuantity = requestCartItemDto.quantity() == null ? 1 : requestCartItemDto.quantity();
        CartItemId cartItemId = getCartIdByUserEmailAndProductSlug(email, requestCartItemDto.slug());
        Optional<CartItem> optionalCartItem = cartItemRepository.findById(cartItemId);
        CartItem cartItem;
        if(optionalCartItem.isPresent()){
            cartItem = optionalCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + cartItemQuantity);
        } else {
            cartItem = new CartItem(cartItemId, cartItemQuantity);
        }
        cartItemRepository.save(cartItem);
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
        CartItemId cartItemId = getCartIdByUserEmailAndProductSlug(email, requestCartItemDto.slug());
        CartItem cartItem = new CartItem(cartItemId, cartItemQuantity);
        cartItemRepository.save(cartItem);
    }

    @Override
    public void deleteUserCartItem(RequestCartItemDto requestCartItemDto, String email) {
        CartItemId cartItemId = getCartIdByUserEmailAndProductSlug(email, requestCartItemDto.slug());
        Optional<CartItem> optionalCartItem = cartItemRepository.findById(cartItemId);
        optionalCartItem.ifPresent(cartItemRepository::delete);
    }

    private CartItemId getCartIdByUserEmailAndProductSlug(String userEmail, String productSlug){
        AppUser appUser = userRepository.findByEmail(userEmail).orElseThrow(() ->
                new UserNotFoundException(HttpStatus.NOT_FOUND, "User with this username does not exist!"));
        Product product = productRepository.findBySlug(productSlug).orElseThrow(() ->
                new ProductNotFoundException(HttpStatus.NOT_FOUND, "Product doesn't exist"));
        return new CartItemId(appUser, product);
    }
}
