package mate.academy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import mate.academy.dto.user.UserRegistrationRequest;
import mate.academy.dto.user.UserResponseDto;
import mate.academy.exception.RegistrationException;
import mate.academy.mapper.UserMapper;
import mate.academy.model.Role;
import mate.academy.model.User;
import mate.academy.repository.role.RoleRepository;
import mate.academy.repository.user.UserRepository;
import mate.academy.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Register new user")
    void register_validUserRegistrationRequest_Success() throws RegistrationException {
        UserRegistrationRequest request = new UserRegistrationRequest()
                .setEmail("test@test.com")
                .setPassword("test")
                .setRepeatPassword("test")
                .setFirstName("Alice")
                .setLastName("Cooper");

        Role role = new Role();
        role.setId(1L);
        role.setRoleName(Role.RoleName.USER);

        User user = new User()
                .setEmail(request.getEmail())
                .setPassword(request.getPassword())
                .setFirstName(request.getFirstName())
                .setLastName(request.getLastName())
                .setRoles(Set.of(role));

        UserResponseDto expected = new UserResponseDto()
                .setId(1L)
                .setEmail("test@test.com")
                .setFirstName("Alice")
                .setLastName("Cooper");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName(Role.RoleName.USER)).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedpassword");
        when(userMapper.toDto(user)).thenReturn(expected);

        UserResponseDto actual = userService.register(request);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Register new user when user already exists")
    void register_userAlreadyExists_RegistrationException() throws RegistrationException {
        UserRegistrationRequest request = new UserRegistrationRequest()
                .setEmail("test@test.com")
                .setPassword("test")
                .setRepeatPassword("test")
                .setFirstName("Alice")
                .setLastName("Cooper");

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(new User()));

        RegistrationException registrationException = assertThrows(
                RegistrationException.class,
                () -> userService.register(request));

        assertEquals(RegistrationException.class, registrationException.getClass());
        assertEquals("User already exists", registrationException.getMessage());
    }
}
