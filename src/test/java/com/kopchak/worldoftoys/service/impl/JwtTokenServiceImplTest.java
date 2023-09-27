package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.repository.token.AuthTokenRepository;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
@Slf4j
class JwtTokenServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthTokenRepository authTokenRepository;

    @InjectMocks
    @Spy
    private JwtTokenServiceImpl jwtTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtTokenService, "SECRET_KEY", "testsecretkey".repeat(20));
    }

    @Test
    void extractUsername_ValidToken_ReturnsOptionalString() {
        // Arrange
        String expectedUsername = "user@example.com";
        String token = "eyJhbGciOiJIUzI1NiJ9" +
                ".eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjk1NzM4MzMwLCJleHAiOjk1Nzk3MzgzMzB9" +
                ".EFuKx_EUPx8pEpVGk0wIIck1nxXB8prHj7noH8Nb3QI";

        long expTokenTimeInSeconds = 9579738330L;
        Instant instant = Instant.ofEpochSecond(expTokenTimeInSeconds);
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        log.warn("Token expiration date is {}", localDateTime);


        //Act
        Optional<String> actualUsername = jwtTokenService.extractUsername(token);

        //Assert
        assertThat(actualUsername).isPresent();
        assertThat(actualUsername.get()).isEqualTo(expectedUsername);
    }

    @Test
    void extractUsername_InvalidToken_ReturnsEmptyOptional() {
        // Arrange
        String token = "invalid-token";

        //Act
        Optional<String> actualUsername = jwtTokenService.extractUsername(token);

        //Assert
        assertThat(actualUsername).isEmpty();
    }
}