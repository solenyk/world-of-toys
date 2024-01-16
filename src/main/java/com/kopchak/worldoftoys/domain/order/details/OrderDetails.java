package com.kopchak.worldoftoys.domain.order.details;

import com.kopchak.worldoftoys.domain.order.Order;
import com.kopchak.worldoftoys.domain.product.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Product product;

    @Column(columnDefinition = "integer default 1", nullable = false)
    @NotNull(message = "Invalid cart item quantity: quantity is null")
    @Min(value = 1,
            message = "Invalid cart item quantity: quantity '${validatedValue}' must be greater than or equal to {status}")
    private Integer quantity = 1;
}
