package com.kopchak.worldoftoys.domain.cart;

import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigInteger;
import javax.annotation.processing.Generated;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(CartItem.class)
public abstract class CartItem_ {

	public static volatile SingularAttribute<CartItem, BigInteger> quantity;
	public static volatile SingularAttribute<CartItem, CartItemId> id;

	public static final String QUANTITY = "quantity";
	public static final String ID = "id";

}

