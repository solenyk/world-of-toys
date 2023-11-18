package com.kopchak.worldoftoys.model.order.details;

import com.kopchak.worldoftoys.model.order.Order;
import com.kopchak.worldoftoys.model.product.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(OrderDetailsId.class)
public class OrderDetails {
    @Id
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false, referencedColumnName = "id")
    private Order order;

    @Id
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "product_id", nullable = false, referencedColumnName = "id")
    private Product product;

    @Column(columnDefinition = "integer default 1", nullable = false)
    @NotNull(message = "Invalid cart item quantity: quantity is null")
    @Min(value = 1,
            message = "Invalid cart item quantity: quantity '${validatedValue}' must be greater than or equal to {value}")
    private Integer quantity = 1;
}
