package com.anup.restaurant_backend.controller;

import com.anup.restaurant_backend.dto.AuthResponse;
import com.anup.restaurant_backend.dto.LoginRequest;
import com.anup.restaurant_backend.dto.UserRequestDto;
import com.anup.restaurant_backend.entity.UserEntity;
import com.anup.restaurant_backend.enums.UserRole;
import com.anup.restaurant_backend.repository.UserRepository;
import com.anup.restaurant_backend.security.JwtService;
import com.anup.restaurant_backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ============================================================
 *  AuthControllerTest — Pure Unit Tests (no Spring context)
 * ============================================================
 *
 *  WHY NO @SpringBootTest here?
 *  Loading the full Spring context requires a real DB connection.
 *  These are UNIT tests — we mock every dependency and test
 *  only the controller logic in isolation.
 *
 *  Coverage targets from your spec: 70-80% minimum.
 *  This file covers AuthController fully.
 * ============================================================
 */
class AuthControllerTest {

    // ── Mocks (fake versions of every dependency) ────────────
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    // ── The class we are actually testing ────────────────────
    @InjectMocks
    private AuthController authController;

    // ── Helper for building JSON strings ─────────────────────
    private ObjectMapper objectMapper;

    // ─────────────────────────────────────────────────────────
    //  SETUP — runs before every @Test
    // ─────────────────────────────────────────────────────────
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // initializes @Mock and @InjectMocks
        objectMapper = new ObjectMapper();
    }

    // =========================================================
    //  LOGIN TESTS
    // =========================================================

    @Test
    @DisplayName("Login — success: valid credentials return JWT token")
    void login_validCredentials_returnsToken() {
        // ── ARRANGE ───────────────────────────────────────────
        // Build the request the client sends
        LoginRequest request = new LoginRequest();
        request.setEmail("anup@gmail.com");
        request.setPassword("password123");

        // Build the UserEntity that would be found in DB
        UserEntity mockUser = new UserEntity();
        mockUser.setEmail("anup@gmail.com");
        mockUser.setRole(UserRole.USER);

        // authenticationManager.authenticate() — don't throw = success
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null); // Spring returns Authentication object but controller ignores it

        // userRepository.findByEmail() returns our mock user
        when(userRepository.findByEmail("anup@gmail.com"))
                .thenReturn(Optional.of(mockUser));

        // jwtService generates a fake token
        when(jwtService.generateToken("anup@gmail.com", "USER"))
                .thenReturn("mocked.jwt.token");

        // ── ACT ───────────────────────────────────────────────
        AuthResponse response = authController.login(request);

        // ── ASSERT ────────────────────────────────────────────
        assertNotNull(response, "Response should not be null");
        assertEquals("mocked.jwt.token", response.getToken(),
                "Token in response should match the generated token");

        // Verify authenticate() was actually called once
        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Verify JWT was generated with correct email and role
        verify(jwtService, times(1))
                .generateToken("anup@gmail.com", "USER");
    }

    @Test
    @DisplayName("Login — failure: wrong password throws BadCredentialsException")
    void login_wrongPassword_throwsBadCredentialsException() {
        // ── ARRANGE ───────────────────────────────────────────
        LoginRequest request = new LoginRequest();
        request.setEmail("anup@gmail.com");
        request.setPassword("wrongpassword");

        // authenticationManager throws when credentials are bad
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // ── ACT + ASSERT ──────────────────────────────────────
        // Controller re-throws BadCredentialsException — we verify it propagates
        assertThrows(BadCredentialsException.class, () -> authController.login(request),
                "Should throw BadCredentialsException for wrong password");

        // JWT should NEVER be generated when auth fails
        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    @DisplayName("Login — failure: unknown email throws BadCredentialsException")
    void login_unknownEmail_throwsBadCredentialsException() {
        // ── ARRANGE ───────────────────────────────────────────
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@gmail.com");
        request.setPassword("anypassword");

        // AuthenticationManager throws — user not found internally by Spring Security
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("User not found"));

        // ── ACT + ASSERT ──────────────────────────────────────
        assertThrows(BadCredentialsException.class, () -> authController.login(request));

        verify(userRepository, never()).findByEmail(any());
        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    @DisplayName("Login — RESTAURANT_OWNER role: token generated with correct role")
    void login_restaurantOwner_tokenHasCorrectRole() {
        // ── ARRANGE ───────────────────────────────────────────
        LoginRequest request = new LoginRequest();
        request.setEmail("owner@gmail.com");
        request.setPassword("ownerpass");

        UserEntity ownerUser = new UserEntity();
        ownerUser.setEmail("owner@gmail.com");
        ownerUser.setRole(UserRole.RESTAURANT_OWNER);

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("owner@gmail.com"))
                .thenReturn(Optional.of(ownerUser));
        when(jwtService.generateToken("owner@gmail.com", "RESTAURANT_OWNER"))
                .thenReturn("owner.jwt.token");

        // ── ACT ───────────────────────────────────────────────
        AuthResponse response = authController.login(request);

        // ── ASSERT ────────────────────────────────────────────
        assertEquals("owner.jwt.token", response.getToken());

        // Critical: role must be RESTAURANT_OWNER, not USER
        verify(jwtService, times(1))
                .generateToken("owner@gmail.com", "RESTAURANT_OWNER");
    }

    // =========================================================
    //  REGISTER TESTS
    // =========================================================

    @Test
    @DisplayName("Register — success: returns 'User registered successfully'")
    void register_validRequest_returnsSuccessMessage() {
        // ── ARRANGE ───────────────────────────────────────────
        UserRequestDto dto = buildValidUserDto("neha@gmail.com");

        when(userService.registerUser(any(UserRequestDto.class)))
                .thenReturn("User registered successfully");

        // ── ACT ───────────────────────────────────────────────
        String result = authController.register(dto);

        // ── ASSERT ────────────────────────────────────────────
        assertEquals("User registered successfully", result);

        // Verify userService was called exactly once
        verify(userService, times(1)).registerUser(any(UserRequestDto.class));
    }

    @Test
    @DisplayName("Register — duplicate email: service handles it (throws)")
    void register_duplicateEmail_serviceThrows() {
        // ── ARRANGE ───────────────────────────────────────────
        // This is testing that if userService throws,
        // the controller lets it propagate (GlobalExceptionHandler catches it)
        UserRequestDto dto = buildValidUserDto("existing@gmail.com");

        when(userService.registerUser(any(UserRequestDto.class)))
                .thenThrow(new com.anup.restaurant_backend.exception.ValidationException(
                        "This email is already registered. Please sign in."));

        // ── ACT + ASSERT ──────────────────────────────────────
        com.anup.restaurant_backend.exception.ValidationException ex =
                assertThrows(
                        com.anup.restaurant_backend.exception.ValidationException.class,
                        () -> authController.register(dto)
                );

        assertEquals("This email is already registered. Please sign in.", ex.getMessage());
    }

    @Test
    @DisplayName("Register — userService called with exact DTO values")
    void register_correctDtoPassedToService() {
        // ── ARRANGE ───────────────────────────────────────────
        UserRequestDto dto = buildValidUserDto("check@gmail.com");

        when(userService.registerUser(any(UserRequestDto.class)))
                .thenReturn("User registered successfully");

        // ── ACT ───────────────────────────────────────────────
        authController.register(dto);

        // ── ASSERT ────────────────────────────────────────────
        // Verify service was called — argument captor confirms correct object passed
        verify(userService).registerUser(argThat(d ->
                "check@gmail.com".equals(d.getEmail()) &&
                        "Anup".equals(d.getFirstName()) &&
                        "9876543210".equals(d.getPhone())
        ));
    }

    @Test
    @DisplayName("Register — when service returns message, controller returns same string")
    void register_serviceReturnValue_isReturnedDirectly() {
        // ── ARRANGE ───────────────────────────────────────────
        UserRequestDto dto = buildValidUserDto("test@gmail.com");
        String expectedMessage = "User registered successfully";

        when(userService.registerUser(any())).thenReturn(expectedMessage);

        // ── ACT ───────────────────────────────────────────────
        String actual = authController.register(dto);

        // ── ASSERT ────────────────────────────────────────────
        // Controller must return exactly what service returns — no modification
        assertSame(expectedMessage, actual,
                "Controller must return the exact string from service without modification");
    }

    // =========================================================
    //  HELPER METHODS
    // =========================================================

    /**
     * Builds a valid UserRequestDto for testing.
     * Centralised here so every test uses consistent data.
     */
    private UserRequestDto buildValidUserDto(String email) {
        UserRequestDto dto = new UserRequestDto();
        dto.setFirstName("Anup");
        dto.setLastName("Sharma");
        dto.setEmail(email);
        dto.setPassword("password123");
        dto.setPhone("9876543210");
        dto.setRole(UserRole.USER);
        return dto;
    }
}