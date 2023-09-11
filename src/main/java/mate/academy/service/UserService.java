package mate.academy.service;

import mate.academy.dto.user.UserRegistrationRequest;
import mate.academy.dto.user.UserResponseDto;
import mate.academy.exception.RegistrationException;

public interface UserService {

    UserResponseDto register(UserRegistrationRequest request) throws RegistrationException;
}
