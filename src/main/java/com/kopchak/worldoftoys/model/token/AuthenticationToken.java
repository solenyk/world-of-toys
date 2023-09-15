package com.kopchak.worldoftoys.model.token;

import com.kopchak.worldoftoys.model.user.AppUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class AuthenticationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(unique = true)
    @NotNull
    public String token;

    @Enumerated(EnumType.STRING)
    @NotNull
    private AuthTokenType tokenType;

    private boolean revoked;

    private boolean expired;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.ALL, CascadeType.MERGE})
    @JoinColumn(name = "user_id")
    @NotNull
    public AppUser user;
}
