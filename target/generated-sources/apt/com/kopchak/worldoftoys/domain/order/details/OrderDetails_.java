package com.kopchak.worldoftoys.domain.order.details;

import com.kopchak.worldoftoys.domain.order.Order;
import com.kopchak.worldoftoys.domain.product.Product;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigInteger;
import javax.annotation.processing.Generated;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(OrderDetails.class)
public abstract class OrderDetails_ {

	public static volatile SingularAttribute<OrderDetails, Product> product;
	public static volatile SingularAttribute<OrderDetails, BigInteger> quantity;
	public static volatile SingularAttribute<OrderDetails, Order> order;

	public static final String PRODUCT = "product";
	public static final String QUANTITY = "quantity";
	public static final String ORDER = "order";

}

