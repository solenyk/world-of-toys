package com.kopchak.worldoftoys.repository.user;

import com.kopchak.worldoftoys.model.user.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ActiveProfiles("integrationtest")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_ExistingUserWithEmail_ReturnsUserWithMatchingEmail() {
        //Arrange
        String userEmail = "john.doe@example.com";

        //Act
        Optional<AppUser> returnedUser = userRepository.findByEmail(userEmail);

        //Assert
        assertThat(returnedUser).isNotNull();
        assertThat(returnedUser).isPresent();
        assertThat(returnedUser.get().getEmail()).isEqualTo(userEmail);
    }

    @Test
    void findByEmail_NonExistentUserEmail_ReturnsEmptyOptional() {
        // Arrange
        String userEmail = "non-existent_user@gmail.com";

        // Act
        Optional<AppUser> returnedUser = userRepository.findByEmail(userEmail);

        // Assert
        assertThat(returnedUser).isNotNull();
        assertThat(returnedUser).isEmpty();
    }
}