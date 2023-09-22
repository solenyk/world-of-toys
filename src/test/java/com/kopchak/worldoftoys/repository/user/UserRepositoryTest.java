package com.kopchak.worldoftoys.repository.user;

import com.kopchak.worldoftoys.config.TestConfig;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.model.user.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ActiveProfiles("integrationtest")
@Import(TestConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void findByEmail_ExistingUserWithEmail_ReturnsUserWithMatchingEmail() {
        //Arrange
        String userEmail = "user@gmail.com";
        AppUser user = AppUser
                .builder()
                .firstname("Firstname")
                .lastname("Lastname")
                .email(userEmail)
                .password(passwordEncoder.encode("password1234"))
                .role(Role.ROLE_USER)
                .enabled(false)
                .locked(false)
                .build();

        //Act
        userRepository.save(user);
        Optional<AppUser> returnedUser = userRepository.findByEmail(userEmail);

        //Assert
        assertThat(returnedUser).isNotNull();
        assertThat(returnedUser).isPresent();
        assertThat(returnedUser.get()).isEqualTo(user);
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