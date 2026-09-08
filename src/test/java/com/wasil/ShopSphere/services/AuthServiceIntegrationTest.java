package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.auth.LoginRequest;
import com.wasil.ShopSphere.model.Role;
import com.wasil.ShopSphere.model.User;
import com.wasil.ShopSphere.repositories.UserRepository;
import com.wasil.ShopSphere.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    // =========================================================
    // LOGIN SUCCESS
    // =========================================================

    @Test
    void login_shouldReturnJwtToken_whenCredentialsAreValid() {

        User user = new User();

        user.setUserEmail("test@example.com");
        user.setUserPassword(
                passwordEncoder.encode("password123")
        );
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);

        // Set any other mandatory User fields
        // required by your entity here.

        userRepository.save(user);

        LoginRequest request = new LoginRequest();

        request.setUserEmail("test@example.com");
        request.setPassword("password123");

        String token = authService.login(request);

        assertNotNull(token);
        assertFalse(token.isBlank());

        assertEquals(
                "test@example.com",
                jwtService.extractUsername(token)
        );
    }

    // =========================================================
    // WRONG PASSWORD
    // =========================================================

    @Test
    void login_shouldThrowException_whenPasswordIsIncorrect() {

        User user = new User();

        user.setUserEmail("test@example.com");
        user.setUserPassword(
                passwordEncoder.encode("password123")
        );

        // Set any other mandatory User fields
        // required by your entity here.

        userRepository.save(user);

        LoginRequest request = new LoginRequest();

        request.setUserEmail("test@example.com");
        request.setPassword("wrongPassword");

        assertThrows(
                AuthenticationException.class,
                () -> authService.login(request)
        );
    }

    // =========================================================
    // USER DOES NOT EXIST
    // =========================================================

    @Test
    void login_shouldThrowException_whenUserDoesNotExist() {

        LoginRequest request = new LoginRequest();

        request.setUserEmail("doesnotexist@example.com");
        request.setPassword("password123");

        assertThrows(
                AuthenticationException.class,
                () -> authService.login(request)
        );
    }
}
