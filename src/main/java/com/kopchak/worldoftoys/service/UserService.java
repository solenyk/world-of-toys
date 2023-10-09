package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.user.UserRegistrationDto;
import com.kopchak.worldoftoys.model.user.AppUser;

public interface UserService {
    void registerUser(UserRegistrationDto userRegistrationDto);
    boolean isUserRegistered(String email);
    boolean isUserActivated(String email);
    boolean isNewPasswordMatchOldPassword(String resetPasswordToken, String newPassword);
    boolean isPasswordsMatch(String username, String password);
    void activateUserAccount(AppUser user);
    void changeUserPassword(AppUser user, String newPassword);
}
