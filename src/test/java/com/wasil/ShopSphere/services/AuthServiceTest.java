package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.auth.LoginRequest;
import com.wasil.ShopSphere.dto.auth.LoginResponse;
import com.wasil.ShopSphere.exceptions.UnauthorizedException;
import com.wasil.ShopSphere.model.Role;
import com.wasil.ShopSphere.model.User;
import com.wasil.ShopSphere.repositories.UserRepository;
import com.wasil.ShopSphere.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldLoginSuccessfully() {

        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUserEmail("test@gmail.com");
        request.setPassword("password123");

        String expectedToken = "jwt-token";

        User user = new User();
        user.setUserEmail("test@gmail.com");
        user.setRole(Role.CUSTOMER);

        when(userRepository.findByUserEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken("test@gmail.com"))
                .thenReturn(expectedToken);

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertEquals(expectedToken, response.getToken());
        assertEquals(Role.CUSTOMER, response.getRole());

        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        verify(jwtService)
                .generateToken("test@gmail.com");
    }

    @Test
    void shouldNotGenerateTokenWhenAuthenticationFails() {

        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUserEmail("test@gmail.com");
        request.setPassword("wrongpassword");

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(new UnauthorizedException("Invalid credentials"));

        // Act + Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid credentials", exception.getMessage());

        verify(jwtService, never())
                .generateToken(anyString());
    }
}
