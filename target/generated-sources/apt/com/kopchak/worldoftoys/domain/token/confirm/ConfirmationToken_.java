package com.kopchak.worldoftoys.domain.token.confirm;

import com.kopchak.worldoftoys.domain.user.AppUser;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ConfirmationToken.class)
public abstract class ConfirmationToken_ {

	public static volatile SingularAttribute<ConfirmationToken, LocalDateTime> createdAt;
	public static volatile SingularAttribute<ConfirmationToken, Integer> id;
	public static volatile SingularAttribute<ConfirmationToken, ConfirmationTokenType> tokenType;
	public static volatile SingularAttribute<ConfirmationToken, LocalDateTime> confirmedAt;
	public static volatile SingularAttribute<ConfirmationToken, AppUser> user;
	public static volatile SingularAttribute<ConfirmationToken, LocalDateTime> expiresAt;
	public static volatile SingularAttribute<ConfirmationToken, String> token;

	public static final String CREATED_AT = "createdAt";
	public static final String ID = "id";
	public static final String TOKEN_TYPE = "tokenType";
	public static final String CONFIRMED_AT = "confirmedAt";
	public static final String USER = "user";
	public static final String EXPIRES_AT = "expiresAt";
	public static final String TOKEN = "token";

}

