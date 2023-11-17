package com.kopchak.worldoftoys.model.order.payment;

import com.kopchak.worldoftoys.model.order.OrderDetails;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Payment {
    @Id
    private String id;

    @Column(nullable = false, scale = 2)
    @NotNull(message = "Invalid price: price is mandatory")
    @DecimalMin(value = "0.0", inclusive = false,
            message = "Invalid price: price '${formatter.format('%1$.2f', validatedValue)}' must not be greater than {value}")
    private BigDecimal price;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(nullable = false)
    @NotNull(message = "Invalid data and time: data and time is mandatory")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime dateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "id", nullable = false)
    private OrderDetails order;
}
