package com.kopchak.worldoftoys.domain.order;

import com.kopchak.worldoftoys.domain.order.details.OrderDetails;
import com.kopchak.worldoftoys.domain.order.payment.Payment;
import com.kopchak.worldoftoys.domain.order.recipient.OrderRecipient;
import com.kopchak.worldoftoys.domain.user.AppUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "app_order")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull(message = "Invalid data and time: data and time is mandatory")
    private LocalDateTime dateTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "recipient_id", referencedColumnName = "id", nullable = false)
    private OrderRecipient orderRecipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private AppUser user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private Set<Payment> payments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private Set<OrderDetails> orderDetails;

    @PrePersist
    private void setDateTime() {
        this.dateTime = LocalDateTime.now();
        this.orderStatus = OrderStatus.AWAITING_PAYMENT;
    }
}
