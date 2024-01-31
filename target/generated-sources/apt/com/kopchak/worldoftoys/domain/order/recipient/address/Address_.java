package com.kopchak.worldoftoys.domain.order.recipient.address;

import com.kopchak.worldoftoys.domain.order.recipient.OrderRecipient;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import javax.annotation.processing.Generated;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Address.class)
public abstract class Address_ {

	public static volatile SingularAttribute<Address, String> street;
	public static volatile SetAttribute<Address, OrderRecipient> orderRecipients;
	public static volatile SingularAttribute<Address, Integer> id;
	public static volatile SingularAttribute<Address, String> region;
	public static volatile SingularAttribute<Address, Integer> house;
	public static volatile SingularAttribute<Address, Integer> apartment;
	public static volatile SingularAttribute<Address, String> settlement;

	public static final String STREET = "street";
	public static final String ORDER_RECIPIENTS = "orderRecipients";
	public static final String ID = "id";
	public static final String REGION = "region";
	public static final String HOUSE = "house";
	public static final String APARTMENT = "apartment";
	public static final String SETTLEMENT = "settlement";

}

