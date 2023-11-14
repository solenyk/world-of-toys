package com.kopchak.worldoftoys.config;

import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.model.user.Role;
import com.kopchak.worldoftoys.service.JwtTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

@TestConfiguration
public class UserDetailsTestConfig {
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            UserDetails user = AppUser
                    .builder()
                    .email("user@example.com")
                    .role(Role.ROLE_USER)
                    .build();
            UserDetails admin = AppUser
                    .builder()
                    .email("admin@example.com")
                    .role(Role.ROLE_ADMIN)
                    .build();
            List<UserDetails> users = List.of(user, admin);
            return users
                    .stream()
                    .filter(userDetails -> userDetails.getUsername().equals(username))
                    .findAny()
                    .orElseThrow();
        };
    }
}
