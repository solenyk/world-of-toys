package com.kopchak.worldoftoys.domain.image;

import com.kopchak.worldoftoys.domain.product.Product;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import javax.annotation.processing.Generated;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Image.class)
public abstract class Image_ {

	public static volatile SingularAttribute<Image, byte[]> image;
	public static volatile SingularAttribute<Image, Product> product;
	public static volatile SingularAttribute<Image, String> name;
	public static volatile SingularAttribute<Image, Integer> id;
	public static volatile SingularAttribute<Image, String> type;

	public static final String IMAGE = "image";
	public static final String PRODUCT = "product";
	public static final String NAME = "name";
	public static final String ID = "id";
	public static final String TYPE = "type";

}

