package com.kopchak.worldoftoys.repository.token;

import com.kopchak.worldoftoys.model.token.AuthenticationToken;
import com.kopchak.worldoftoys.model.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthenticationToken, Integer> {
    Optional<AuthenticationToken> findByToken(String authToken);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM AuthenticationToken t " +
            "WHERE t.user.email = :email AND t.expired = false AND t.revoked = false")
    boolean isActiveAccessTokenExists(@Param("email") String email);

    @Modifying
    @Query("UPDATE AuthenticationToken t SET t.expired = true, t.revoked = true WHERE t.user = :user AND " +
            "(t.expired = false OR t.revoked = false)")
    void revokeActiveUserAuthTokens(@Param("user") AppUser user);


}
