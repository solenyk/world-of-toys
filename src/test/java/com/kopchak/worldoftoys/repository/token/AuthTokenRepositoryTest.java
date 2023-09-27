package com.kopchak.worldoftoys.repository.token;

import com.kopchak.worldoftoys.model.token.AuthTokenType;
import com.kopchak.worldoftoys.model.token.AuthenticationToken;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ActiveProfiles("integrationtest")
class AuthTokenRepositoryTest {

    @Autowired
    private AuthTokenRepository authTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void findByToken_ExistingAuthToken_ReturnsAuthenticationToken(){
        //Arrange
        String jwtToken = "eyJhbGciOiJIUzI1NiJ9." +
                "eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NTM3NjA3NiwiZXhwIjoxNzk1Mzc3NTE2fQ." +
                "7z-SJjVtAamFjOo0qLd0ehtO59ODHw2B7j1dm4nynE4";

        //Act
        Optional<AuthenticationToken> returnedToken = authTokenRepository.findByToken(jwtToken);

        //Assert
        assertThat(returnedToken).isNotNull();
        assertThat(returnedToken).isPresent();
        assertThat(returnedToken.get().getToken()).isEqualTo(jwtToken);
    }

    @Test
    public void findByToken_NonExistentAuthToken_ReturnsEmptyOptional(){
        //Arrange
        String jwtToken = "non-existent-token";

        //Act
        Optional<AuthenticationToken> returnedToken = authTokenRepository.findByToken(jwtToken);

        //Assert
        assertThat(returnedToken).isNotNull();
        assertThat(returnedToken).isEmpty();
    }

    @Test
    public void isActiveAuthTokenExists_UsernameOfUserWithActiveAccessTokens_ReturnsTrue(){
        //Arrange
        String username = "john.doe@example.com";

        //Act
        boolean isActiveAuthTokenExists = authTokenRepository.isActiveAuthTokenExists(username, AuthTokenType.ACCESS);

        //Assert
        assertThat(isActiveAuthTokenExists).isEqualTo(true);
    }

    @Test
    public void isActiveAuthTokenExists_UsernameOfUserWithoutActiveAccessTokens_ReturnsTrue(){
        //Arrange
        String username = "jane.smith@example.com";

        //Act
        boolean isActiveAuthTokenExists = authTokenRepository.isActiveAuthTokenExists(username, AuthTokenType.ACCESS);

        //Assert
        assertThat(isActiveAuthTokenExists).isEqualTo( false);
    }

    @Test
    public void revokeActiveUserAuthTokens_UsernameOfUserWithActiveAccessTokens_ReturnsTrue(){
        //Arrange
        String email = "jane.smith@example.com";
        AppUser user = userRepository.findByEmail(email).orElseThrow();

        //Act
        authTokenRepository.revokeActiveUserAuthTokens(user);
        boolean isActiveAccessAuthTokenExists = authTokenRepository.isActiveAuthTokenExists(email, AuthTokenType.ACCESS);
        boolean isActiveRefreshAuthTokenExists = authTokenRepository.isActiveAuthTokenExists(email, AuthTokenType.REFRESH);
        boolean isActiveAuthTokenExists = isActiveAccessAuthTokenExists || isActiveRefreshAuthTokenExists;

        //Assert
        assertThat(isActiveAuthTokenExists).isEqualTo( false);
    }
}