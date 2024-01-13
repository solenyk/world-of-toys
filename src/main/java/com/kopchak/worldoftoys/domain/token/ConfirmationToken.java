package com.kopchak.worldoftoys.domain.token;

import com.kopchak.worldoftoys.domain.user.AppUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ConfirmationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Invalid token: token is blank")
    private String token;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Invalid token type: token type is NULL")
    private ConfirmationTokenType tokenType;

    @Column(nullable = false)
    @NotNull(message = "Invalid creation date: creation date is NULL")
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @NotNull(message = "Invalid expiration date: expiration date is NULL")
    private LocalDateTime expiresAt;

    private LocalDateTime confirmedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private AppUser user;
}
