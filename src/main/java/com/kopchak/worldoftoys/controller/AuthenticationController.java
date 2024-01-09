package com.kopchak.worldoftoys.controller;

import com.kopchak.worldoftoys.dto.error.ResponseStatusExceptionDto;
import com.kopchak.worldoftoys.dto.token.AccessAndRefreshTokensDto;
import com.kopchak.worldoftoys.dto.token.AuthTokenDto;
import com.kopchak.worldoftoys.dto.user.ResetPasswordDto;
import com.kopchak.worldoftoys.dto.user.UserAuthDto;
import com.kopchak.worldoftoys.dto.user.UserRegistrationDto;
import com.kopchak.worldoftoys.dto.user.UsernameDto;
import com.kopchak.worldoftoys.exception.exception.*;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin
@RequiredArgsConstructor
@Tag(name = "authentication-controller", description = "The authentication controller is responsible for handling user " +
        "authentication and registration. It provides endpoints for user registration, account confirmation, " +
        "reset password and login")
@Slf4j
public class AuthenticationController {
    private final UserService userService;
    private final ConfirmationTokenService confirmTokenService;
    private final EmailSenderService emailSenderService;
    private final JwtTokenService jwtTokenService;

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
                            schema = @Schema(implementation = ResponseStatusExceptionDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "User is not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseStatusExceptionDto.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@Valid @RequestBody UserRegistrationDto userRegistrationDto) {
        String username = userRegistrationDto.email();
        try {
            userService.registerUser(userRegistrationDto);
            var confirmToken = confirmTokenService.createConfirmationToken(username, ConfirmationTokenType.ACTIVATION);
            emailSenderService.sendEmail(username, confirmToken.token(), ConfirmationTokenType.ACTIVATION);
        } catch (UsernameAlreadyExistException | TokenAlreadyExistException | AccountActivationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (MessageSendingException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
        }
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
    public ResponseEntity<Void> activateAccount(@Parameter(description = "User account activation token",
            required = true) @RequestParam("token") String token) {
        try {
            confirmTokenService.activateAccountUsingActivationToken(token);
        } catch (InvalidConfirmationTokenException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Resend verification email for account activation")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Verification email has been successfully sent",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400",
                    description = "Account is already activated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseStatusExceptionDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseStatusExceptionDto.class)))
    })
    @PostMapping("/resend-verification-email")
    public ResponseEntity<Void> resendVerificationEmail(@Schema(
            description = "Username to activate the account",
            implementation = UsernameDto.class) @Valid @RequestBody UsernameDto username) {
        String email = username.email();
        try {
            var confirmToken = confirmTokenService.createConfirmationToken(email, ConfirmationTokenType.ACTIVATION);
            emailSenderService.sendEmail(email, confirmToken.token(), ConfirmationTokenType.ACTIVATION);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (TokenAlreadyExistException | AccountActivationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (MessageSendingException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
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
    public ResponseEntity<Void> sendResetPasswordEmail(@Schema(
            description = "Username to reset the password",
            implementation = UsernameDto.class) @Valid @RequestBody UsernameDto username) {
        String email = username.email();
        try {
            var confirmationToken = confirmTokenService.createConfirmationToken(email, ConfirmationTokenType.RESET_PASSWORD);
            emailSenderService.sendEmail(email, confirmationToken.token(), ConfirmationTokenType.RESET_PASSWORD);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (TokenAlreadyExistException | AccountActivationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (MessageSendingException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
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
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "Token to change the user's password", required = true)
            @Valid @RequestParam("token") String token, @Valid @RequestBody ResetPasswordDto newPassword) {
        try {
            confirmTokenService.changePasswordUsingResetToken(token, newPassword);
        } catch (InvalidConfirmationTokenException | InvalidPasswordException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
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
        try {
            AccessAndRefreshTokensDto tokensDto = userService.authenticateUser(userAuthDto);
            return ResponseEntity.status(HttpStatus.OK).body(tokensDto);
        } catch (UserNotFoundException | AccountActivationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
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
        try {
            AuthTokenDto authTokenDto = jwtTokenService.refreshAccessToken(refreshTokenDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(authTokenDto);
        } catch (TokenAlreadyExistException | JwtTokenException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
