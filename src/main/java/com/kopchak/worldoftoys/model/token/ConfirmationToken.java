package com.kopchak.worldoftoys.model.token;

import com.kopchak.worldoftoys.model.user.AppUser;
import jakarta.persistence.*;
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

    @Column(unique = true)
    @NotNull
    private String token;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ConfirmTokenType tokenType;

    @Column(nullable = false)
    @NotNull
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @NotNull
    private LocalDateTime expiresAt;

    private LocalDateTime confirmedAt;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.ALL, CascadeType.MERGE})
    @JoinColumn(name = "user_id")
    @NotNull
    private AppUser user;
}
