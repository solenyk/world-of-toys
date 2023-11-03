package com.kopchak.worldoftoys.controller;

import com.kopchak.worldoftoys.dto.error.ResponseStatusExceptionDto;
import com.kopchak.worldoftoys.dto.token.AccessAndRefreshTokensDto;
import com.kopchak.worldoftoys.dto.token.AuthTokenDto;
import com.kopchak.worldoftoys.dto.token.ConfirmTokenDto;
import com.kopchak.worldoftoys.dto.user.ResetPasswordDto;
import com.kopchak.worldoftoys.dto.user.UserAuthDto;
import com.kopchak.worldoftoys.dto.user.UserRegistrationDto;
import com.kopchak.worldoftoys.dto.user.UsernameDto;
import com.kopchak.worldoftoys.exception.*;
import com.kopchak.worldoftoys.model.token.AuthTokenType;
import com.kopchak.worldoftoys.model.token.ConfirmationTokenType;
import com.kopchak.worldoftoys.service.ConfirmationTokenService;
import com.kopchak.worldoftoys.service.EmailSenderService;
import com.kopchak.worldoftoys.service.JwtTokenService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin
@RequiredArgsConstructor
@Tag(name = "authentication-controller", description = "Controller for user registration, account confirmation, " +
        "reset password and login")
@Slf4j
public class AuthenticationController {
    private final UserService userService;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSenderService emailSenderService;
    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;

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
                            schema = @Schema(implementation = ResponseStatusExceptionDto.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto userRegistrationDto) {
        if (userService.isUserRegistered(userRegistrationDto.getEmail())) {
            log.error("User with username: {} already exist!", userRegistrationDto.getEmail());
            throw new UsernameAlreadyExistException(HttpStatus.BAD_REQUEST, "This username already exist!");
        }
        String username = userRegistrationDto.getEmail();
        userService.registerUser(userRegistrationDto);
        ConfirmTokenDto confirmationToken = confirmationTokenService.createConfirmationToken(username,
                ConfirmationTokenType.ACTIVATION);
        emailSenderService.sendEmail(username, confirmationToken.getToken(), ConfirmationTokenType.ACTIVATION);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Account activation")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Account activated",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400",
                    description = "Confirmation token is invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseStatusExceptionDto.class)))
    })
    @GetMapping(path = "/confirm")
    public ResponseEntity<?> activateAccount(@Parameter(description = "User account activation token",
            required = true) @RequestParam("token") String token) {
        if (confirmationTokenService.isConfirmationTokenInvalid(token, ConfirmationTokenType.ACTIVATION)) {
            log.error("Confirmation token is invalid!");
            throw new InvalidConfirmationTokenException(HttpStatus.BAD_REQUEST, "This confirmation token is invalid!");
        }
        confirmationTokenService.activateAccountUsingActivationToken(token);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Resend verification email for account activation")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Verification email has been successfully sent",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseStatusExceptionDto.class))),
            @ApiResponse(responseCode = "409",
                    description = "Account is already activated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseStatusExceptionDto.class)))
    })
    @PostMapping("/resend-verification-email")
    public ResponseEntity<?> resendVerificationEmail(@Schema(
            description = "Username to activate the account",
            implementation = UsernameDto.class) @Valid @RequestBody UsernameDto username) {
        String email = username.getEmail();
        if (!userService.isUserRegistered(email)) {
            log.error("User with username: {} does not exist!", email);
            throw new UserNotFoundException(HttpStatus.NOT_FOUND, "User with this username does not exist!");
        }
        if (userService.isUserActivated(email)) {
            log.error("Account of user: {} is already activated!", email);
            throw new AccountIsAlreadyActivatedException(HttpStatus.CONFLICT, "Account is already activated!");
        }
        if (confirmationTokenService.isNoActiveConfirmationToken(email, ConfirmationTokenType.ACTIVATION)) {
            var confirmationToken = confirmationTokenService.createConfirmationToken(email,
                    ConfirmationTokenType.ACTIVATION);
            emailSenderService.sendEmail(email, confirmationToken.getToken(), ConfirmationTokenType.ACTIVATION);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Send an email with a link to reset user password")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Reset password email has been successfully sent",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseStatusExceptionDto.class)))
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<?> sendResetPasswordEmail(@Schema(
            description = "Username to reset the password",
            implementation = UsernameDto.class) @Valid @RequestBody UsernameDto username) {
        String email = username.getEmail();
        if (!userService.isUserRegistered(email)) {
            log.error("User with username: {} does not exist!", email);
            throw new UserNotFoundException(HttpStatus.NOT_FOUND, "User with this username does not exist!");
        }
        if (confirmationTokenService.isNoActiveConfirmationToken(email, ConfirmationTokenType.RESET_PASSWORD)) {
            var confirmationToken = confirmationTokenService.createConfirmationToken(email,
                    ConfirmationTokenType.RESET_PASSWORD);
            emailSenderService.sendEmail(email, confirmationToken.getToken(), ConfirmationTokenType.RESET_PASSWORD);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Reset password")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Password changed successfully",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Confirmation token is invalid or new password matches old password",
                    content = @Content(schema = @Schema(implementation = ResponseStatusExceptionDto.class
                    )))
    })
    @PostMapping(path = "/reset-password")
    public ResponseEntity<?> changePassword(@Parameter(description = "Token to change the user's password",
            required = true) @RequestParam("token") String token, @Valid @RequestBody ResetPasswordDto newPassword) {
        if (confirmationTokenService.isConfirmationTokenInvalid(token, ConfirmationTokenType.RESET_PASSWORD)) {
            log.error("Confirmation token is invalid!");
            throw new InvalidConfirmationTokenException(HttpStatus.BAD_REQUEST, "This confirmation token is invalid!");
        }
        if (userService.isNewPasswordMatchOldPassword(token, newPassword.getPassword())) {
            log.error("New password matches old password!");
            throw new InvalidPasswordException(HttpStatus.BAD_REQUEST, "New password matches old password!");
        }
        confirmationTokenService.changePasswordUsingResetToken(token, newPassword);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "User login to the account")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User is successfully authenticated",
                    content = @Content(schema = @Schema(implementation = AccessAndRefreshTokensDto.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad user credentials",
                    content = @Content(schema = @Schema(implementation = ResponseStatusExceptionDto.class))),
            @ApiResponse(
                    responseCode = "403",
                    description = "Account is not activated",
                    content = @Content(schema = @Schema(implementation = ResponseStatusExceptionDto.class))),
    })
    @PostMapping("/login")
    public ResponseEntity<AccessAndRefreshTokensDto> authenticate(@Valid @RequestBody UserAuthDto userAuthDto) {
        String username = userAuthDto.getEmail();
        if (!userService.isUserRegistered(username) || !userService.isPasswordsMatch(username, userAuthDto.getPassword())) {
            log.error("Bad user credentials!");
            throw new UserNotFoundException(HttpStatus.UNAUTHORIZED, "Bad user credentials!");
        }
        if (!userService.isUserActivated(username)) {
            log.error("Account with username: {} is not activated!", username);
            throw new UserNotFoundException(HttpStatus.FORBIDDEN, "Account is not activated!");
        }
        String email = userAuthDto.getEmail();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, userAuthDto.getPassword()));
        jwtTokenService.revokeAllUserAuthTokens(email);
        return ResponseEntity.status(HttpStatus.OK).body(jwtTokenService.generateAuthTokens(email));
    }

    @Operation(summary = "Get new access token using refresh token")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Access token is successfully generated",
                    content = @Content(schema = @Schema(implementation = AccessAndRefreshTokensDto.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Refresh token is invalid or valid access token already exists",
                    content = @Content(schema = @Schema(implementation = ResponseStatusExceptionDto.class)))
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthTokenDto> refreshToken(@Valid @RequestBody AuthTokenDto refreshTokenDto) {
        String refreshToken = refreshTokenDto.getToken();
        if (!jwtTokenService.isAuthTokenValid(refreshToken, AuthTokenType.REFRESH)) {
            log.error("Refresh token is invalid!");
            throw new InvalidRefreshTokenException(HttpStatus.BAD_REQUEST, "This refresh token is invalid!");
        }
        if (jwtTokenService.isActiveAuthTokenExists(refreshToken, AuthTokenType.ACCESS)) {
            log.error("There is valid access token!");
            throw new AccessTokenAlreadyExistsException(HttpStatus.BAD_REQUEST, "There is valid access token!");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(jwtTokenService.refreshAccessToken(refreshTokenDto));
    }
}
