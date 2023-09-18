package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.token.AuthTokenDto;
import com.kopchak.worldoftoys.dto.user.UserAuthDto;
import com.kopchak.worldoftoys.dto.user.UserRegistrationDto;

public interface UserService {
    void registerUser(UserRegistrationDto userRegistrationDto);
    boolean isUserRegistered(String email);
    boolean isUserActivated(String email);
    boolean isNewPasswordMatchOldPassword(String resetPasswordToken, String newPassword);
    boolean isPasswordsMatch(String username, String password);
    AuthTokenDto authenticate(UserAuthDto userAuthDto);
}
