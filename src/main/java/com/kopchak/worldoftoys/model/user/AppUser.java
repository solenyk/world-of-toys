package com.kopchak.worldoftoys.model.user;

import com.kopchak.worldoftoys.model.token.AuthenticationToken;
import com.kopchak.worldoftoys.model.token.ConfirmationToken;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
    @NotBlank(message = "Invalid firstname: firstname is empty")
    @NotNull(message = "Invalid firstname: firstname is NULL")
    @Size(min = 3, max = 60, message = "Invalid firstname: firstname must be up to 60 characters long")
    private String firstname;

    @Column(length = 60)
    @NotBlank(message = "Invalid lastname: lastname is empty")
    @NotNull(message = "Invalid lastname: lastname is NULL")
    @Size(min = 3, max = 60, message = "Invalid lastname: lastname must be up to 60 characters long")
    private String lastname;


    @Email(message = "Invalid email")
    @NotBlank(message = "Invalid email: email is empty")
    @NotNull(message = "Invalid email: email is NULL")
    @Size(min = 6, max = 320, message = "Invalid email: email must be up to 320 characters long")
    @Column(length = 320)
    private String email;

    @Column(length = 60)
    @NotBlank(message = "Invalid password: password is empty")
    @NotNull(message = "Invalid password: password is NULL")
    @Size(min = 60, max = 60, message = "Invalid password: encoded password must be 60 characters long")
    private String password;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Invalid role: role is NULL")
    private Role role;

    @NotNull(message = "Invalid locked value: locked value is NULL")
    private Boolean locked = false;

    @NotNull(message = "Invalid enabled value: enabled value is NULL")
    private Boolean enabled = false;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    private List<AuthenticationToken> authenticationTokens;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    private List<ConfirmationToken> confirmationTokens;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
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
