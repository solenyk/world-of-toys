package com.kopchak.worldoftoys.domain.product;

import com.kopchak.worldoftoys.domain.image.Image;
import com.kopchak.worldoftoys.domain.order.details.OrderDetails;
import com.kopchak.worldoftoys.domain.product.category.AgeCategory;
import com.kopchak.worldoftoys.domain.product.category.BrandCategory;
import com.kopchak.worldoftoys.domain.product.category.OriginCategory;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;
import java.math.BigInteger;
import javax.annotation.processing.Generated;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Product.class)
public abstract class Product_ {

	public static volatile SingularAttribute<Product, Boolean> isAvailable;
	public static volatile SingularAttribute<Product, BigInteger> availableQuantity;
	public static volatile SetAttribute<Product, Image> images;
	public static volatile SingularAttribute<Product, String> description;
	public static volatile SetAttribute<Product, AgeCategory> ageCategories;
	public static volatile SingularAttribute<Product, OriginCategory> originCategory;
	public static volatile SetAttribute<Product, OrderDetails> orderDetails;
	public static volatile SingularAttribute<Product, Image> mainImage;
	public static volatile SingularAttribute<Product, BigDecimal> price;
	public static volatile SingularAttribute<Product, BrandCategory> brandCategory;
	public static volatile SingularAttribute<Product, String> name;
	public static volatile SingularAttribute<Product, Integer> id;
	public static volatile SingularAttribute<Product, String> slug;

	public static final String IS_AVAILABLE = "isAvailable";
	public static final String AVAILABLE_QUANTITY = "availableQuantity";
	public static final String IMAGES = "images";
	public static final String DESCRIPTION = "description";
	public static final String AGE_CATEGORIES = "ageCategories";
	public static final String ORIGIN_CATEGORY = "originCategory";
	public static final String ORDER_DETAILS = "orderDetails";
	public static final String MAIN_IMAGE = "mainImage";
	public static final String PRICE = "price";
	public static final String BRAND_CATEGORY = "brandCategory";
	public static final String NAME = "name";
	public static final String ID = "id";
	public static final String SLUG = "slug";

}

