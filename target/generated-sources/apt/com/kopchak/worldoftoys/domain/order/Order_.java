package com.kopchak.worldoftoys.domain.order;

import com.kopchak.worldoftoys.domain.order.details.OrderDetails;
import com.kopchak.worldoftoys.domain.order.payment.Payment;
import com.kopchak.worldoftoys.domain.order.recipient.OrderRecipient;
import com.kopchak.worldoftoys.domain.user.AppUser;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Order.class)
public abstract class Order_ {

	public static volatile SingularAttribute<Order, LocalDateTime> dateTime;
	public static volatile SingularAttribute<Order, OrderRecipient> orderRecipient;
	public static volatile SetAttribute<Order, OrderDetails> orderDetails;
	public static volatile SingularAttribute<Order, BigDecimal> totalPrice;
	public static volatile SetAttribute<Order, Payment> payments;
	public static volatile SingularAttribute<Order, OrderStatus> orderStatus;
	public static volatile SingularAttribute<Order, String> id;
	public static volatile SingularAttribute<Order, AppUser> user;

	public static final String DATE_TIME = "dateTime";
	public static final String ORDER_RECIPIENT = "orderRecipient";
	public static final String ORDER_DETAILS = "orderDetails";
	public static final String TOTAL_PRICE = "totalPrice";
	public static final String PAYMENTS = "payments";
	public static final String ORDER_STATUS = "orderStatus";
	public static final String ID = "id";
	public static final String USER = "user";

}

