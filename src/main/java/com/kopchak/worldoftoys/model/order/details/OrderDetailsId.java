package com.kopchak.worldoftoys.model.order.details;

import com.kopchak.worldoftoys.model.order.Order;
import com.kopchak.worldoftoys.model.product.Product;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class OrderDetailsId implements Serializable {
    private Order order;
    private Product product;
}
