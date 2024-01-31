package com.kopchak.worldoftoys.domain.order.recipient;

import com.kopchak.worldoftoys.domain.order.Order;
import com.kopchak.worldoftoys.domain.order.recipient.address.Address;
import com.kopchak.worldoftoys.domain.order.recipient.number.PhoneNumber;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import javax.annotation.processing.Generated;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(OrderRecipient.class)
public abstract class OrderRecipient_ {

	public static volatile SingularAttribute<OrderRecipient, String> firstname;
	public static volatile SingularAttribute<OrderRecipient, String> patronymic;
	public static volatile SingularAttribute<OrderRecipient, PhoneNumber> phoneNumber;
	public static volatile SingularAttribute<OrderRecipient, Address> address;
	public static volatile SetAttribute<OrderRecipient, Order> orders;
	public static volatile SingularAttribute<OrderRecipient, Integer> id;
	public static volatile SingularAttribute<OrderRecipient, String> lastname;

	public static final String FIRSTNAME = "firstname";
	public static final String PATRONYMIC = "patronymic";
	public static final String PHONE_NUMBER = "phoneNumber";
	public static final String ADDRESS = "address";
	public static final String ORDERS = "orders";
	public static final String ID = "id";
	public static final String LASTNAME = "lastname";

}

