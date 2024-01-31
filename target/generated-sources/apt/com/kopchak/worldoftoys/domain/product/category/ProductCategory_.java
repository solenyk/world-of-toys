package com.kopchak.worldoftoys.domain.product.category;

import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import javax.annotation.processing.Generated;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ProductCategory.class)
public abstract class ProductCategory_ {

	public static volatile SingularAttribute<ProductCategory, String> name;
	public static volatile SingularAttribute<ProductCategory, Integer> id;
	public static volatile SingularAttribute<ProductCategory, String> slug;

	public static final String NAME = "name";
	public static final String ID = "id";
	public static final String SLUG = "slug";

}

