package com.kopchak.worldoftoys.domain.cart;

import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.domain.user.AppUser;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import javax.annotation.processing.Generated;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(CartItemId.class)
public abstract class CartItemId_ {

	public static volatile SingularAttribute<CartItemId, Product> product;
	public static volatile SingularAttribute<CartItemId, AppUser> user;

	public static final String PRODUCT = "product";
	public static final String USER = "user";

}

