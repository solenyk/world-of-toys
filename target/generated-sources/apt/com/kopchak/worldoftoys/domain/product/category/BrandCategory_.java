package com.kopchak.worldoftoys.domain.product.category;

import com.kopchak.worldoftoys.domain.product.Product;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import javax.annotation.processing.Generated;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(BrandCategory.class)
public abstract class BrandCategory_ extends com.kopchak.worldoftoys.domain.product.category.ProductCategory_ {

	public static volatile SetAttribute<BrandCategory, Product> products;

	public static final String PRODUCTS = "products";

}

