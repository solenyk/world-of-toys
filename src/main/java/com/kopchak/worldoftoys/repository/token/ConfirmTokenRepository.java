package com.kopchak.worldoftoys.repository.token;

import com.kopchak.worldoftoys.domain.token.ConfirmationToken;
import com.kopchak.worldoftoys.domain.token.ConfirmationTokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ConfirmTokenRepository extends JpaRepository<ConfirmationToken, Long> {
    Optional<ConfirmationToken> findByToken(String token);
    @Query("SELECT CASE WHEN COUNT(c) = 0 THEN true ELSE false END FROM ConfirmationToken c WHERE c.user.email = :email " +
            "AND c.tokenType = :confirmTokenType AND c.expiresAt > :currentDateTime")
    boolean isNoActiveConfirmationToken(@Param("email") String email,
                                        @Param("confirmTokenType") ConfirmationTokenType confirmTokenType,
                                        @Param("currentDateTime") LocalDateTime currentDateTime);
}
