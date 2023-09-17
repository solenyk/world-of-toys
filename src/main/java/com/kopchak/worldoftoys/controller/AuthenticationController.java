package com.kopchak.worldoftoys.controller;

import com.kopchak.worldoftoys.dto.token.ConfirmTokenDto;
import com.kopchak.worldoftoys.dto.token.TokenAuthDto;
import com.kopchak.worldoftoys.dto.user.ResetPasswordDto;
import com.kopchak.worldoftoys.dto.user.UserAuthDto;
import com.kopchak.worldoftoys.dto.user.UserRegistrationDto;
import com.kopchak.worldoftoys.dto.user.UsernameDto;
import com.kopchak.worldoftoys.exception.AccountIsAlreadyActivatedException;
import com.kopchak.worldoftoys.exception.IncorrectPasswordException;
import com.kopchak.worldoftoys.exception.UserNotFoundException;
import com.kopchak.worldoftoys.exception.UsernameAlreadyExistException;
import com.kopchak.worldoftoys.model.token.ConfirmationTokenType;
import com.kopchak.worldoftoys.service.AuthenticationService;
import com.kopchak.worldoftoys.service.ConfirmationTokenService;
import com.kopchak.worldoftoys.service.EmailSenderService;
import com.kopchak.worldoftoys.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "authentication-controller", description = "Controller for user registration, account confirmation, " +
        "reset password and login")
public class AuthenticationController {
    private final UserService userService;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSenderService emailSenderService;

    @Operation(summary = "User registration")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User has been successfully registered",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400",
                    description = "Username already exist",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsernameAlreadyExistException.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationDto userRegistrationDto) {
        if (userService.isUserRegistered(userRegistrationDto.getEmail())) {
            throw new UsernameAlreadyExistException(HttpStatus.BAD_REQUEST, "This username already exist!");
        }
        String username = userRegistrationDto.getEmail();
        userService.registerUser(userRegistrationDto);
        ConfirmTokenDto confirmationToken = confirmationTokenService.createConfirmationToken(username,
                ConfirmationTokenType.ACTIVATION);
        emailSenderService.sendEmail(username, userRegistrationDto.getFirstname(),
                confirmationToken.getToken(), ConfirmationTokenType.ACTIVATION);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
