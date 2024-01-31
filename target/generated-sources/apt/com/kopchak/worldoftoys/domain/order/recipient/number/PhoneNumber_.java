package com.kopchak.worldoftoys.domain.order.recipient.number;

import com.kopchak.worldoftoys.domain.order.recipient.OrderRecipient;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import javax.annotation.processing.Generated;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(PhoneNumber.class)
public abstract class PhoneNumber_ {

	public static volatile SingularAttribute<PhoneNumber, String> number;
	public static volatile SingularAttribute<PhoneNumber, CountryCode> countryCode;
	public static volatile SingularAttribute<PhoneNumber, OrderRecipient> orderRecipients;
	public static volatile SingularAttribute<PhoneNumber, Integer> id;
	public static volatile SingularAttribute<PhoneNumber, String> operatorCode;

	public static final String NUMBER = "number";
	public static final String COUNTRY_CODE = "countryCode";
	public static final String ORDER_RECIPIENTS = "orderRecipients";
	public static final String ID = "id";
	public static final String OPERATOR_CODE = "operatorCode";

}

