package com.kopchak.worldoftoys.model.user;

import com.kopchak.worldoftoys.model.token.AuthenticationToken;
import com.kopchak.worldoftoys.model.token.ConfirmationToken;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class AppUser implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 60)
    @NotBlank(message = "Firstname is mandatory")
    @Size(min = 2, max = 60, message = "Firstname must be up to 60 characters long")
    private String firstname;

    @Column(length = 60)
    @NotBlank(message = "Lastname is mandatory")
    @Size(min = 3, max = 60, message = "Lastname must be up to 60 characters long")
    private String lastname;

    @Email
    @Column(length = 320)
    @NotBlank(message = "Email is mandatory")
    @Size(min = 3, max = 320, message = "Email must be up to 320 characters long")
    private String email;

    @Column(length = 60)
    @NotBlank(message = "Password is mandatory")
    @Size(min = 60, max = 60, message = "Encoded password must be 60 characters long")
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Boolean locked = false;

    private Boolean enabled = false;

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL, CascadeType.MERGE})
    @JoinColumn(name = "user_id")
    private List<AuthenticationToken> authenticationTokens;


    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL, CascadeType.MERGE})
    @JoinColumn(name = "user_id")
    private List<ConfirmationToken> confirmationTokens;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
