package com.kopchak.worldoftoys.model.token;

import com.kopchak.worldoftoys.model.user.AppUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class ConfirmationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(unique = true)
    @NotNull
    public String token;

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
    public AppUser user;
}
