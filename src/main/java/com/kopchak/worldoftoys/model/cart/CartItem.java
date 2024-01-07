package com.kopchak.worldoftoys.model.cart;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CartItem {
    @EmbeddedId
    private CartItemId id;

    @Column(columnDefinition = "integer default 1", nullable = false)
    @NotNull(message = "Invalid cart item quantity: quantity is null")
    @Min(value = 1,
            message = "Invalid cart item quantity: quantity '${validatedValue}' must be greater than or equal to {status}")
    private Integer quantity = 1;
}
