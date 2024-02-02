package com.kopchak.worldoftoys.domain.order.payment;

import com.kopchak.worldoftoys.domain.order.Order;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Payment.class)
public abstract class Payment_ {

	public static volatile SingularAttribute<Payment, LocalDateTime> dateTime;
	public static volatile SingularAttribute<Payment, BigDecimal> price;
	public static volatile SingularAttribute<Payment, String> id;
	public static volatile SingularAttribute<Payment, PaymentStatus> status;
	public static volatile SingularAttribute<Payment, Order> order;

	public static final String DATE_TIME = "dateTime";
	public static final String PRICE = "price";
	public static final String ID = "id";
	public static final String STATUS = "status";
	public static final String ORDER = "order";

}

