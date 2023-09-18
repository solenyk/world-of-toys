package com.kopchak.worldoftoys.model.token;

import com.kopchak.worldoftoys.model.user.AppUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class AuthenticationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    @NotBlank(message = "Invalid token: token is empty")
    @NotNull(message = "Invalid token: token is NULL")
    private String token;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Invalid token type: token type is NULL")
    private AuthTokenType tokenType;

    private boolean revoked;

    private boolean expired;

    @NotNull(message = "Invalid user id: user is NULL")
    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;
}
