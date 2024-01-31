package com.kopchak.worldoftoys.domain.token.auth;

import com.kopchak.worldoftoys.domain.user.AppUser;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import javax.annotation.processing.Generated;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(AuthenticationToken.class)
public abstract class AuthenticationToken_ {

	public static volatile SingularAttribute<AuthenticationToken, Boolean> expired;
	public static volatile SingularAttribute<AuthenticationToken, Integer> id;
	public static volatile SingularAttribute<AuthenticationToken, AuthTokenType> tokenType;
	public static volatile SingularAttribute<AuthenticationToken, Boolean> revoked;
	public static volatile SingularAttribute<AuthenticationToken, AppUser> user;
	public static volatile SingularAttribute<AuthenticationToken, String> token;

	public static final String EXPIRED = "expired";
	public static final String ID = "id";
	public static final String TOKEN_TYPE = "tokenType";
	public static final String REVOKED = "revoked";
	public static final String USER = "user";
	public static final String TOKEN = "token";

}

