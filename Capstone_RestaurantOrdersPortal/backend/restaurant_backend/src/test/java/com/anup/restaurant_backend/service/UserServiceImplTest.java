package com.anup.restaurant_backend.service;

import com.anup.restaurant_backend.dto.LoginRequest;
import com.anup.restaurant_backend.dto.UserRequestDto;
import com.anup.restaurant_backend.entity.UserEntity;
import com.anup.restaurant_backend.enums.UserRole;
import com.anup.restaurant_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for UserServiceImpl.
 *
 * This class verifies all major user-related operations such as:
 * - Registering a new user
 * - Preventing duplicate registrations
 * - Logging in users
 * - Handling invalid login attempts
 *
 * All dependencies are mocked so that only business logic is tested.
 */
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests successful user registration.
     * <p>
     * Scenario:
     * A new user provides valid details and email does not already exist.
     * <p>
     * Expected:
     * - User is saved in database
     * - Password is encoded
     * - Success message is returned
     */
    @Test
    void registerUser_success() {

        UserRequestDto dto = new UserRequestDto();
        dto.setFirstName("Anup");
        dto.setLastName("Rathore");
        dto.setEmail("test@mail.com");
        dto.setPhone("1234567890");
        dto.setPassword("pass123");
        dto.setRole(UserRole.USER);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        String result = userService.registerUser(dto);

        assertEquals("User registered successfully", result);
        verify(userRepository).save(any(UserEntity.class));
    }

    /**
     * Tests registration failure when email already exists.
     * <p>
     * Scenario:
     * User tries to register with an email that is already in the system.
     * <p>
     * Expected:
     * - Exception is thrown
     * - User is NOT saved again
     */
    @Test
    void registerUser_duplicateEmail() {

        UserRequestDto dto = new UserRequestDto();
        dto.setFirstName("Anup");
        dto.setLastName("Rathore");
        dto.setEmail("test@mail.com");
        dto.setPhone("1234567890");
        dto.setPassword("pass123");
        dto.setRole(UserRole.USER);

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(new UserEntity()));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.registerUser(dto));

        assertTrue(exception.getMessage().contains("already registered"));
        verify(userRepository, never()).save(any());
    }

    /**
     * Tests validation failure when required fields are missing.
     * <p>
     * Scenario:
     * User submits empty or invalid data.
     * <p>
     * Expected:
     * - Validation exception is thrown
     * - No database interaction happens
     */
    @Test
    void registerUser_validationFailure() {

        UserRequestDto dto = new UserRequestDto(); // empty DTO

        assertThrows(RuntimeException.class,
                () -> userService.registerUser(dto));

        verify(userRepository, never()).save(any());
    }

    /**
     * Tests successful login.
     * <p>
     * Scenario:
     * User provides correct email and password.
     * <p>
     * Expected:
     * - User is authenticated successfully
     * - "Login successful" message is returned
     */
    @Test
    void loginUser_success() {

        UserEntity user = new UserEntity();
        user.setEmail("test@mail.com");
        user.setPassword("hashedPassword");

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass123", "hashedPassword"))
                .thenReturn(true);

        // ✅ FIX: use setters instead of constructor
        LoginRequest request = new LoginRequest();
        request.setEmail("test@mail.com");
        request.setPassword("pass123");

        String result = userService.loginUser(request);

        assertEquals("Login successful", result);
    }

    /**
     * Tests login failure due to incorrect password.
     * <p>
     * Scenario:
     * User enters wrong password.
     * <p>
     * Expected:
     * - Exception is thrown
     * - Login is denied
     */
    @Test
    void loginUser_wrongPassword() {

        UserEntity user = new UserEntity();
        user.setEmail("test@mail.com");
        user.setPassword("hashedPassword");

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);

        // ✅ FIX here also
        LoginRequest request = new LoginRequest();
        request.setEmail("test@mail.com");
        request.setPassword("wrongPassword");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.loginUser(request));

        assertTrue(exception.getMessage().contains("Incorrect password"));
    }

    /**
     * Tests login failure when user does not exist.
     * <p>
     * Scenario:
     * Email is not registered in system.
     * <p>
     * Expected:
     * - Exception is thrown
     * - Proper error message is returned
     */
    @Test
    void loginUser_userNotFound() {

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        // ✅ FIX here also
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@mail.com");
        request.setPassword("pass123");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.loginUser(request));

        assertTrue(exception.getMessage().contains("No account found"));
    }

}