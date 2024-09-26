package com.kopchak.worldoftoys.controller;

import com.kopchak.worldoftoys.domain.token.confirm.ConfirmationTokenType;
import com.kopchak.worldoftoys.dto.error.ExceptionDto;
import com.kopchak.worldoftoys.dto.token.AccessAndRefreshTokensDto;
import com.kopchak.worldoftoys.dto.token.AuthTokenDto;
import com.kopchak.worldoftoys.dto.user.ResetPasswordDto;
import com.kopchak.worldoftoys.dto.user.UserAuthDto;
import com.kopchak.worldoftoys.dto.user.UserRegistrationDto;
import com.kopchak.worldoftoys.dto.user.UsernameDto;
import com.kopchak.worldoftoys.service.impl.ConfirmationTokenService;
import com.kopchak.worldoftoys.service.impl.EmailSenderService;
import com.kopchak.worldoftoys.service.impl.JwtTokenService;
import com.kopchak.worldoftoys.service.impl.UserService;
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
@CrossOrigin
@RequiredArgsConstructor
@Tag(name = "authentication-controller", description = "The authentication controller is responsible for handling user " +
        "authentication and registration. It provides endpoints for user registration, account confirmation, " +
        "reset password and login")
public class AuthenticationController {
    private final UserService userService;
    private final ConfirmationTokenService confirmTokenService;
    private final EmailSenderService emailSenderService;
    private final JwtTokenService jwtTokenService;

    @Operation(summary = "User registration")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "The user has been successfully registered",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400",
                    description = "The username already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "503",
                    description = "It is not possible to send a message, the service is not unavailable",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDto.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@Valid @RequestBody UserRegistrationDto userRegistrationDto) {
        String username = userRegistrationDto.email();
        userService.registerUser(userRegistrationDto);
        var confirmToken = confirmTokenService.createConfirmationToken(username, ConfirmationTokenType.ACTIVATION);
        emailSenderService.sendEmail(username, confirmToken.token(), ConfirmationTokenType.ACTIVATION);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Account activation")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "The account is activated",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400",
                    description = "The confirmation token is invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDto.class)))
    })
    @GetMapping(path = "/confirm")
    public ResponseEntity<Void> activateAccount(
            @Parameter(description = "User account activation token", required = true)
            @RequestParam("token") String token) {
        confirmTokenService.activateAccountUsingActivationToken(token);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Resend verification email for account activation")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "The verification email has been successfully sent",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400",
                    description = "The account is already activated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "The user is not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "503",
                    description = "It is not possible to send a message, the service is not unavailable",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDto.class)))
    })
    @PostMapping("/resend-verification-email")
    public ResponseEntity<Void> resendVerificationEmail(@Schema(
            description = "Username to activate the account",
            implementation = UsernameDto.class) @Valid @RequestBody UsernameDto username) {
        String email = username.email();
        var confirmToken = confirmTokenService.createConfirmationToken(email, ConfirmationTokenType.ACTIVATION);
        emailSenderService.sendEmail(email, confirmToken.token(), ConfirmationTokenType.ACTIVATION);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Send an email with a link to reset user password")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "The reset password email has been successfully sent",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400",
                    description = "The valid reset password already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "The user is not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "503",
                    description = "It is not possible to send a message, the service is not unavailable",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDto.class)))
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> sendResetPasswordEmail(@Schema(
            description = "Username to reset the password",
            implementation = UsernameDto.class) @Valid @RequestBody UsernameDto username) {
        String email = username.email();
        var confirmationToken = confirmTokenService.createConfirmationToken(email, ConfirmationTokenType.RESET_PASSWORD);
        emailSenderService.sendEmail(email, confirmationToken.token(), ConfirmationTokenType.RESET_PASSWORD);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Reset password")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "The password changed successfully",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(
                    responseCode = "400",
                    description = "The confirmation token is invalid or new password matches old password",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class
                    )))
    })
    @PostMapping(path = "/reset-password")
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "Token to change the user's password", required = true)
            @Valid @RequestParam("token") String token, @Valid @RequestBody ResetPasswordDto newPassword) {
        confirmTokenService.changePasswordUsingResetToken(token, newPassword);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "User login to the account")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "The user is successfully authenticated",
                    content = @Content(schema = @Schema(implementation = AccessAndRefreshTokensDto.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "The user account is not activated or the login data is invalid",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<AccessAndRefreshTokensDto> authenticate(@Valid @RequestBody UserAuthDto userAuthDto) {
        AccessAndRefreshTokensDto tokensDto = userService.authenticateUser(userAuthDto);
        return ResponseEntity.status(HttpStatus.OK).body(tokensDto);
    }

    @Operation(summary = "Get new access token using refresh token")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "The access token is successfully generated",
                    content = @Content(schema = @Schema(implementation = AccessAndRefreshTokensDto.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "The refresh token is invalid or the valid access token already exists",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class)))
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthTokenDto> refreshToken(@Valid @RequestBody AuthTokenDto refreshTokenDto) {
        AuthTokenDto authTokenDto = jwtTokenService.refreshAccessToken(refreshTokenDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(authTokenDto);
    }
}
