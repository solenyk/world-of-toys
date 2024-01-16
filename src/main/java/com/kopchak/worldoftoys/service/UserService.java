package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.token.AccessAndRefreshTokensDto;
import com.kopchak.worldoftoys.dto.user.UserAuthDto;
import com.kopchak.worldoftoys.dto.user.UserRegistrationDto;
import com.kopchak.worldoftoys.exception.AccountActivationException;
import com.kopchak.worldoftoys.exception.InvalidPasswordException;
import com.kopchak.worldoftoys.exception.UserNotFoundException;
import com.kopchak.worldoftoys.exception.UsernameAlreadyExistException;
import com.kopchak.worldoftoys.domain.user.AppUser;

public interface UserService {
    void registerUser(UserRegistrationDto userRegistrationDto) throws UsernameAlreadyExistException;
    void changeUserPassword(AppUser user, String newPassword) throws InvalidPasswordException;
    AccessAndRefreshTokensDto authenticateUser(UserAuthDto userAuthDto)
            throws UserNotFoundException, AccountActivationException;
}
