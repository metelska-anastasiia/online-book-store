package mate.academy.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import mate.academy.model.User;
import mate.academy.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {
    private static final String VALID_EMAIL = "john@test.com";
    private static final String INVALID_EMAIL = "invlaid@test.com";
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Find user by valid email")
    @Sql(
            scripts = "classpath:database/repository/user/before/add-user-to-users-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/repository/user/after/remove-from-users.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    void findByEmail_validEmail_Success() {
        User expected = new User()
                .setId(1L)
                .setEmail("john@test.com")
                .setPassword("test")
                .setFirstName("John")
                .setLastName("Doe");

        User actual = userRepository.findByEmail(VALID_EMAIL).get();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Find user by valid email")
    @Sql(
            scripts = "classpath:database/repository/user/before/add-user-to-users-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/repository/user/after/remove-from-users.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    void findByEmail_invalidEmail_EmptyOptional() {
        Optional<User> actual = userRepository.findByEmail(INVALID_EMAIL);
        assertTrue(actual.isEmpty());
    }
}
