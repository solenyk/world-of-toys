package com.kopchak.worldoftoys.domain.user;

import com.kopchak.worldoftoys.domain.token.auth.AuthenticationToken;
import com.kopchak.worldoftoys.domain.token.confirm.ConfirmationToken;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import javax.annotation.processing.Generated;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(AppUser.class)
public abstract class AppUser_ {

	public static volatile SingularAttribute<AppUser, String> firstname;
	public static volatile SingularAttribute<AppUser, String> password;
	public static volatile SingularAttribute<AppUser, Role> role;
	public static volatile ListAttribute<AppUser, AuthenticationToken> authenticationTokens;
	public static volatile ListAttribute<AppUser, ConfirmationToken> confirmationTokens;
	public static volatile SingularAttribute<AppUser, Integer> id;
	public static volatile SingularAttribute<AppUser, Boolean> locked;
	public static volatile SingularAttribute<AppUser, String> email;
	public static volatile SingularAttribute<AppUser, Boolean> enabled;
	public static volatile SingularAttribute<AppUser, String> lastname;

	public static final String FIRSTNAME = "firstname";
	public static final String PASSWORD = "password";
	public static final String ROLE = "role";
	public static final String AUTHENTICATION_TOKENS = "authenticationTokens";
	public static final String CONFIRMATION_TOKENS = "confirmationTokens";
	public static final String ID = "id";
	public static final String LOCKED = "locked";
	public static final String EMAIL = "email";
	public static final String ENABLED = "enabled";
	public static final String LASTNAME = "lastname";

}

