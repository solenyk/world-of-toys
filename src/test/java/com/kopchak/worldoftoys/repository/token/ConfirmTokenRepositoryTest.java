package com.kopchak.worldoftoys.repository.token;

import com.kopchak.worldoftoys.model.token.ConfirmationToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ActiveProfiles("integrationtest")
class ConfirmTokenRepositoryTest {

    @Autowired
    private ConfirmTokenRepository confirmTokenRepository;

    @Test
    void findByToken_ExistingConfirmToken_ReturnsConfirmToken() {
        //Arrange
        String confirmToken = "8e5648d7-9b4e-4724-83a1-be7e64603e48";

        //Act
        Optional<ConfirmationToken> returnedToken = confirmTokenRepository.findByToken(confirmToken);

        //Assert
        assertThat(returnedToken).isNotNull();
        assertThat(returnedToken).isPresent();
        assertThat(returnedToken.get().getToken()).isEqualTo(confirmToken);
    }

    @Test
    void findByToken_NonExistentConfirmToken_ReturnsEmptyOptional() {
        //Arrange
        String confirmToken = "8e5648d7-9b4e-4724-83a1-be7e64603e41";

        //Act
        Optional<ConfirmationToken> returnedToken = confirmTokenRepository.findByToken(confirmToken);

        //Assert
        assertThat(returnedToken).isNotNull();
        assertThat(returnedToken).isEmpty();
    }
}