package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.user.UserDto;
import com.kopchak.worldoftoys.dto.user.UserRegistrationDto;

public interface UserService {
    void registerUser(UserRegistrationDto userRegistrationDto);
    boolean isUserRegistered(String email);
    String saveUserAuthToken(String email);
    boolean isUserActivated(String email);
    boolean isNewPasswordMatchOldPassword(String resetPasswordToken, String newPassword);
}
