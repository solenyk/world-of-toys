package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.user.UserRegistrationDto;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.model.user.Role;
import com.kopchak.worldoftoys.repository.token.AuthTokenRepository;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import com.kopchak.worldoftoys.service.JwtTokenService;
import com.kopchak.worldoftoys.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public void registerUser(UserRegistrationDto userRegistrationDto) {
        AppUser user = AppUser.builder()
                .firstname(userRegistrationDto.getFirstname())
                .lastname(userRegistrationDto.getLastname())
                .email(userRegistrationDto.getEmail())
                .password(passwordEncoder.encode(userRegistrationDto.getPassword()))
                .role(Role.ROLE_USER)
                .enabled(false)
                .locked(false)
                .build();
        userRepository.save(user);
    }

    public boolean isUserRegistered(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
