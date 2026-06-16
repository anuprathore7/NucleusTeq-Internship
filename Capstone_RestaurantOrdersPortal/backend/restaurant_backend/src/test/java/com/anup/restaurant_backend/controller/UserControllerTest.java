package com.anup.restaurant_backend.controller;

import com.anup.restaurant_backend.entity.UserEntity;
import com.anup.restaurant_backend.enums.UserRole;
import com.anup.restaurant_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserController}.
 * Verifies that the profile endpoint correctly reads the authenticated
 * user from the security context and returns all expected fields.
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController userController;

    private UserEntity testUser;

    /**
     * Builds a sample user entity that all tests can reuse
     * so each test stays focused on what it is actually checking.
     */
    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setId(1L);
        testUser.setFirstName("Anup");
        testUser.setLastName("Sharma");
        testUser.setEmail("anup@test.com");
        testUser.setPhone("9876543210");
        testUser.setRole(UserRole.USER);
        testUser.setWalletBalance(1000.0);
    }

    /**
     * Verifies that the profile endpoint returns all expected fields
     * when the authenticated user exists in the database.
     */
    @Test
    void getProfile_shouldReturnAllUserFields_whenUserIsAuthenticated() {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("anup@test.com");
        when(userRepository.findByEmail("anup@test.com")).thenReturn(Optional.of(testUser));

        try (MockedStatic<SecurityContextHolder> mockedHolder =
                     mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            Map<String, Object> result = userController.getProfile();

            assertNotNull(result);
            assertEquals(1L, result.get("id"));
            assertEquals("Anup", result.get("firstName"));
            assertEquals("Sharma", result.get("lastName"));
            assertEquals("anup@test.com", result.get("email"));
            assertEquals("9876543210", result.get("phone"));
            assertEquals("USER", result.get("role"));
            assertEquals(1000.0, result.get("walletBalance"));
        }
    }

    /**
     * Verifies that a {@link RuntimeException} is thrown
     * when the authenticated email does not match any user in the database.
     */
    @Test
    void getProfile_shouldThrow_whenUserNotFoundInDatabase() {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("ghost@test.com");
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        try (MockedStatic<SecurityContextHolder> mockedHolder =
                     mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            assertThrows(RuntimeException.class, () -> userController.getProfile());
        }
    }

    /**
     * Verifies that the wallet balance returned in the profile
     * matches exactly what is stored in the database.
     */
    @Test
    void getProfile_shouldReturnCorrectWalletBalance() {
        testUser.setWalletBalance(2500.0);

        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("anup@test.com");
        when(userRepository.findByEmail("anup@test.com")).thenReturn(Optional.of(testUser));

        try (MockedStatic<SecurityContextHolder> mockedHolder =
                     mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            Map<String, Object> result = userController.getProfile();

            assertEquals(2500.0, result.get("walletBalance"));
        }
    }

    /**
     * Verifies that the role returned in the profile is a string value
     * not an enum object, since the controller calls role.name() explicitly.
     */
    @Test
    void getProfile_shouldReturnRoleAsString_notEnumObject() {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("anup@test.com");
        when(userRepository.findByEmail("anup@test.com")).thenReturn(Optional.of(testUser));

        try (MockedStatic<SecurityContextHolder> mockedHolder =
                     mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            Map<String, Object> result = userController.getProfile();

            assertInstanceOf(String.class, result.get("role"));
            assertEquals("USER", result.get("role"));
        }
    }
}