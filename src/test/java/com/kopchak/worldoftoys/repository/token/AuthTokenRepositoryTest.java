package com.kopchak.worldoftoys.repository.token;

import com.kopchak.worldoftoys.config.TestConfig;
import com.kopchak.worldoftoys.model.token.AuthenticationToken;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ActiveProfiles("integrationtest")
@Import(TestConfig.class)
class AuthTokenRepositoryTest {

    @Autowired
    private AuthTokenRepository authTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void findByToken_ExistingAuthToken_ReturnsAuthenticationToken(){
        //Arrange
        String jwtToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NTM3NjA3NiwiZXhwIjoxNzk1Mzc3NTE2fQ.7z-SJjVtAamFjOo0qLd0ehtO59ODHw2B7j1dm4nynE4";

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
        String jwtToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkb2VAZXhhbXBsZS5jb20iLCJpYXQiOjE2OTUzNzYwNzYsImV4cCI6MTc5NTM5NjIzNn0.RUOstiDumUGzPk43bBLKGFQmRzEtAPuXZrHi8yjaa9I";

        //Act
        Optional<AuthenticationToken> returnedToken = authTokenRepository.findByToken(jwtToken);

        //Assert
        assertThat(returnedToken).isNotNull();
        assertThat(returnedToken).isEmpty();
    }

    @Test
    public void findAllByUser_UserWithExistingAuthTokens_ReturnsListOfAuthTokens(){
        //Arrange
        AppUser user = userRepository.findByEmail("john.doe@example.com").get();

        //Act
        List<AuthenticationToken> returnedTokens = authTokenRepository.findAllByUser(user);

        //Assert
        assertThat(returnedTokens).isNotNull();
        assertThat(returnedTokens.isEmpty()).isEqualTo(false);
        assertThat(returnedTokens.size()).isEqualTo(2);
    }

    @Test
    public void findAllByUser_UserWithoutExistingAuthTokens_ReturnsEmptyList(){
        //Arrange
        AppUser user = userRepository.findByEmail("jane.smith@example.com").get();

        //Act
        List<AuthenticationToken> returnedTokens = authTokenRepository.findAllByUser(user);

        //Assert
        assertThat(returnedTokens).isNotNull();
        assertThat(returnedTokens.isEmpty()).isEqualTo(true);
    }
}